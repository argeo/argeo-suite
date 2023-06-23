package org.argeo.app.swt.forms;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.argeo.api.acr.Content;
import org.argeo.api.cms.CmsLog;
import org.argeo.api.cms.ux.Cms2DSize;
import org.argeo.api.cms.ux.CmsEditable;
import org.argeo.api.cms.ux.CmsImageManager;
import org.argeo.cms.swt.CmsSwtUtils;
import org.argeo.cms.swt.SwtEditablePart;
import org.argeo.cms.swt.acr.AbstractPageViewer;
import org.argeo.cms.swt.acr.Img;
import org.argeo.cms.swt.acr.SwtSection;
import org.argeo.cms.swt.acr.SwtSectionPart;
import org.argeo.cms.swt.widgets.EditableImage;
import org.argeo.cms.swt.widgets.StyledControl;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.rap.fileupload.FileDetails;
import org.eclipse.rap.fileupload.FileUploadEvent;
import org.eclipse.rap.fileupload.FileUploadHandler;
import org.eclipse.rap.fileupload.FileUploadListener;
import org.eclipse.rap.fileupload.FileUploadReceiver;
import org.eclipse.rap.rwt.service.ServerPushSession;
import org.eclipse.rap.rwt.widgets.FileUpload;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/** Manage life cycle of a form page that is linked to a given node */
public class FormPageViewer extends AbstractPageViewer {
	private final static CmsLog log = CmsLog.getLog(FormPageViewer.class);

	private final SwtSection mainSection;

	// TODO manage within the CSS
	private Integer labelColWidth = null;
	private int rowLayoutHSpacing = 8;

	// Context cached in the viewer
	// The reference to translate from text to calendar and reverse
	private DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(FormUtils.DEFAULT_SHORT_DATE_FORMAT);
	// new SimpleDateFormat(FormUtils.DEFAULT_SHORT_DATE_FORMAT);
	private CmsImageManager<Control, Content> imageManager;
	private FileUploadListener fileUploadListener;

	public FormPageViewer(SwtSection mainSection, int style, CmsEditable cmsEditable) {
		super(mainSection, style, cmsEditable);
		this.mainSection = mainSection;

		if (getCmsEditable().canEdit()) {
			fileUploadListener = new FUL();
		}
	}

	@Override
	protected void prepare(SwtEditablePart part, Object caretPosition) {
		if (part instanceof Img) {
			// ((Img) part).setFileUploadListener(fileUploadListener);
		}
	}

	/** To be overridden.Save the edited part. */
	protected void save(SwtEditablePart part) {
		Content node = null;
		if (part instanceof EditableMultiStringProperty ept) {
			List<String> values = ept.getValues();
			node = ept.getContent();
			QName propName = ept.getPropertyName();
			if (values.isEmpty()) {
				if (node.containsKey(propName))
					node.remove(propName);
			} else {
				node.put(propName, values);
//				node.setProperty(propName, values.toArray(new String[0]));
			}
			// => Viewer : Controller
		} else if (part instanceof EditablePropertyString ept) {
			String txt = ((Text) ept.getControl()).getText();
			node = ept.getContent();
			QName propName = ept.getPropertyName();
			if (EclipseUiUtils.isEmpty(txt)) {
				node.remove(propName);
			} else {
				setPropertySilently(node, propName, txt);
				// node.setProperty(propName, txt);
			}
			// node.getSession().save();
			// => Viewer : Controller
		} else if (part instanceof EditablePropertyDate) {
			EditablePropertyDate ept = (EditablePropertyDate) part;
			// FIXME deal with no value set
			TemporalAccessor cal = FormUtils.parseDate(dateFormat, ((Text) ept.getControl()).getText());
			node = ept.getContent();
			QName propName = ept.getPropertyName();
			if (cal == null) {
				node.remove(propName);
			} else {
				node.put(propName, cal);
			}
			// node.getSession().save();
			// => Viewer : Controller
		}
		// TODO: make this configurable, sometimes we do not want to save the
		// current session at this stage
//		if (node != null && node.getSession().hasPendingChanges()) {
//			JcrUtils.updateLastModified(node, true);
//			node.getSession().save();
//		}
	}

	@Override
	protected void updateContent(SwtEditablePart part) {
		if (part instanceof EditableMultiStringProperty ept) {
			Content node = ept.getContent();
			QName propName = ept.getPropertyName();
			List<String> valStrings = new ArrayList<String>();
			if (node.containsKey(propName)) {
				for (String val : node.getMultiple(propName, String.class))
					valStrings.add(val);
			}
			ept.setValues(valStrings);
		} else if (part instanceof EditablePropertyString ept) {
			// || part instanceof EditableLink
			Content node = ept.getContent();
			QName propName = ept.getPropertyName();
			ept.setText(node.get(propName, String.class).orElse(""));
		} else if (part instanceof EditablePropertyDate ept) {
			Content node = ept.getContent();
			QName propName = ept.getPropertyName();
			if (node.containsKey(propName))
				ept.setText(dateFormat.format(node.get(propName, Instant.class).get()));
			else
				ept.setText("");
		} else if (part instanceof SwtSectionPart sectionPart) {
			Content partNode = sectionPart.getContent();
			// use control AFTER setting style, since it may have been reset
			if (part instanceof EditableImage) {
				EditableImage editableImage = (EditableImage) part;
				imageManager().load(partNode, part.getControl(), editableImage.getPreferredImageSize());
			}
		}
	}

	// FILE UPLOAD LISTENER
	protected class FUL implements FileUploadListener {

		public FUL() {
		}

		public void uploadProgress(FileUploadEvent event) {
			// TODO Monitor upload progress
		}

		public void uploadFailed(FileUploadEvent event) {
			throw new IllegalStateException("Upload failed " + event, event.getException());
		}

		public void uploadFinished(FileUploadEvent event) {
			for (FileDetails file : event.getFileDetails()) {
				if (log.isDebugEnabled())
					log.debug("Received: " + file.getFileName());
			}
			mainSection.getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					saveEdit();
				}
			});
			FileUploadHandler uploadHandler = (FileUploadHandler) event.getSource();
			uploadHandler.dispose();
		}
	}

	// FOCUS OUT LISTENER
	protected FocusListener createFocusListener() {
		return new FocusOutListener();
	}

	private class FocusOutListener implements FocusListener {
		private static final long serialVersionUID = -6069205786732354186L;

		@Override
		public void focusLost(FocusEvent event) {
			saveEdit();
		}

		@Override
		public void focusGained(FocusEvent event) {
			// does nothing;
		}
	}

	// MOUSE LISTENER
	@Override
	protected MouseListener createMouseListener() {
		return new ML();
	}

	private class ML extends MouseAdapter {
		private static final long serialVersionUID = 8526890859876770905L;

		@Override
		public void mouseDoubleClick(MouseEvent e) {
			if (e.button == 1) {
				Control source = (Control) e.getSource();
				if (getCmsEditable().canEdit()) {
					if (getCmsEditable().isEditing() && !(getEdited() instanceof Img)) {
						if (source == mainSection)
							return;
						SwtEditablePart part = findDataParent(source);
						upload(part);
					} else {
						getCmsEditable().startEditing();
					}
				}
			}
		}

		@Override
		public void mouseDown(MouseEvent e) {
			if (getCmsEditable().isEditing()) {
				if (e.button == 1) {
					Control source = (Control) e.getSource();
					SwtEditablePart composite = findDataParent(source);
					Point point = new Point(e.x, e.y);
					if (!(composite instanceof Img))
						edit(composite, source.toDisplay(point));
				} else if (e.button == 3) {
					// EditablePart composite = findDataParent((Control) e
					// .getSource());
					// if (styledTools != null)
					// styledTools.show(composite, new Point(e.x, e.y));
				}
			}
		}

		protected synchronized void upload(SwtEditablePart part) {
			if (part instanceof SwtSectionPart) {
				if (part instanceof Img) {
					if (getEdited() == part)
						return;
					edit(part, null);
					layout(part.getControl());
				}
			}
		}
	}

	@Override
	public Control getControl() {
		return mainSection;
	}

	protected CmsImageManager<Control, Content> imageManager() {
		if (imageManager == null)
			imageManager = CmsSwtUtils.getCmsView(mainSection).getImageManager();
		return imageManager;
	}

	// LOCAL UI HELPERS
	protected SwtSection createSectionIfNeeded(Composite body, Content node) {
		SwtSection section = null;
		if (node != null) {
			section = new SwtSection(body, SWT.NO_FOCUS, node);
			section.setLayoutData(CmsSwtUtils.fillWidth());
			section.setLayout(CmsSwtUtils.noSpaceGridLayout());
		}
		return section;
	}

	protected void createSimpleLT(Composite bodyRow, Content node, QName propName, String label, String msg) {
		if (getCmsEditable().canEdit() || node.containsKey(propName)) {
			createPropertyLbl(bodyRow, label);
			EditablePropertyString eps = new EditablePropertyString(bodyRow, SWT.WRAP | SWT.LEFT, node, propName, msg);
			eps.setMouseListener(getMouseListener());
			eps.setFocusListener(getFocusListener());
			eps.setLayoutData(CmsSwtUtils.fillWidth());
		}
	}

	protected void createMultiStringLT(Composite bodyRow, Content node, QName propName, String label, String msg) {
		boolean canEdit = getCmsEditable().canEdit();
		if (canEdit || node.containsKey(propName)) {
			createPropertyLbl(bodyRow, label);

			List<String> valueStrings = new ArrayList<String>();

			if (node.containsKey(propName)) {
				for (String value : node.getMultiple(propName, String.class))
					valueStrings.add(value);
			}

			// TODO use a drop down to display possible values to the end user
			EditableMultiStringProperty emsp = new EditableMultiStringProperty(bodyRow, SWT.SINGLE | SWT.LEAD, node,
					propName, valueStrings, new String[] { "Implement this" }, msg,
					canEdit ? getRemoveValueSelListener() : null);
			addListeners(emsp);
			// emsp.setMouseListener(getMouseListener());
			emsp.setStyle(FormStyle.propertyMessage.style());
			emsp.setLayoutData(CmsSwtUtils.fillWidth());
		}
	}

	protected Label createPropertyLbl(Composite parent, String value) {
		return createPropertyLbl(parent, value, SWT.NONE);
	}

	protected Label createPropertyLbl(Composite parent, String value, int vAlign) {
		// boolean isSmall = CmsView.getCmsView(parent).getUxContext().isSmall();
		Label label = new Label(parent, SWT.LEAD | SWT.WRAP);
		label.setText(value + " ");
		CmsSwtUtils.style(label, FormStyle.propertyLabel.style());
		GridData gd = new GridData(SWT.LEAD, vAlign, false, false);
		if (labelColWidth != null)
			gd.widthHint = labelColWidth;
		label.setLayoutData(gd);
		return label;
	}

	protected Label newStyledLabel(Composite parent, String style, String value) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(value);
		CmsSwtUtils.style(label, style);
		return label;
	}

	protected Composite createRowLayoutComposite(Composite parent) {
		Composite bodyRow = new Composite(parent, SWT.NO_FOCUS);
		bodyRow.setLayoutData(CmsSwtUtils.fillWidth());
		RowLayout rl = new RowLayout(SWT.WRAP);
		rl.type = SWT.HORIZONTAL;
		rl.spacing = rowLayoutHSpacing;
		rl.marginHeight = rl.marginWidth = 0;
		rl.marginTop = rl.marginBottom = rl.marginLeft = rl.marginRight = 0;
		bodyRow.setLayout(rl);
		return bodyRow;
	}

	protected Composite createAddImgComposite(SwtSection section, Composite parent, Content parentNode) {

		Composite body = new Composite(parent, SWT.NO_FOCUS);
		body.setLayout(new GridLayout());

		FormFileUploadReceiver receiver = new FormFileUploadReceiver(section, parentNode, null);
		final FileUploadHandler currentUploadHandler = new FileUploadHandler(receiver);
		if (fileUploadListener != null)
			currentUploadHandler.addUploadListener(fileUploadListener);

		// Button creation
		final FileUpload fileUpload = new FileUpload(body, SWT.BORDER);
		fileUpload.setText("Import an image");
		fileUpload.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		fileUpload.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 4869523412991968759L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				ServerPushSession pushSession = new ServerPushSession();
				pushSession.start();
				String uploadURL = currentUploadHandler.getUploadUrl();
				fileUpload.submit(uploadURL);
			}
		});

		return body;
	}

	protected class FormFileUploadReceiver extends FileUploadReceiver {

		private Content context;
		private SwtSection section;
		private String name;

		public FormFileUploadReceiver(SwtSection section, Content context, String name) {
			this.context = context;
			this.section = section;
			this.name = name;
		}

		@Override
		public void receive(InputStream stream, FileDetails details) throws IOException {

			if (name == null)
				name = details.getFileName();

			// TODO clean image name more carefully
			String cleanedName = name.replaceAll("[^a-zA-Z0-9-.]", "");
			// We add a unique prefix to workaround the cache issue: when
			// deleting and re-adding a new image with same name, the end user
			// browser will use the cache and the image will remain unchanged
			// for a while
			cleanedName = System.currentTimeMillis() % 100000 + "_" + cleanedName;

			imageManager().uploadImage(context, context, cleanedName, stream, details.getContentType());
			// TODO clean refresh strategy
			section.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					FormPageViewer.this.refresh(section);
					section.layout();
					section.getParent().layout();
				}
			});
		}
	}

	protected void addListeners(StyledControl control) {
		control.setMouseListener(getMouseListener());
		control.setFocusListener(getFocusListener());
	}

	protected Img createImgComposite(Composite parent, Content node, Point preferredSize) {
		Img img = new Img(parent, SWT.NONE, node, new Cms2DSize(preferredSize.x, preferredSize.y)) {
			private static final long serialVersionUID = 1297900641952417540L;

			@Override
			protected void setContainerLayoutData(Composite composite) {
				composite.setLayoutData(CmsSwtUtils.grabWidth(SWT.CENTER, SWT.DEFAULT));
			}

			@Override
			protected void setControlLayoutData(Control control) {
				control.setLayoutData(CmsSwtUtils.grabWidth(SWT.CENTER, SWT.DEFAULT));
			}
		};
		img.setLayoutData(CmsSwtUtils.grabWidth(SWT.CENTER, SWT.DEFAULT));
		updateContent(img);
		addListeners(img);
		return img;
	}

	protected Composite addDeleteAbility(final SwtSection section, Content sessionNode, int topWeight,
			int rightWeight) {
		Composite comp = new Composite(section, SWT.NONE);
		comp.setLayoutData(CmsSwtUtils.fillAll());
		comp.setLayout(new FormLayout());

		// The body to be populated
		Composite body = new Composite(comp, SWT.NO_FOCUS);
		body.setLayoutData(EclipseUiUtils.fillFormData());

		if (getCmsEditable().canEdit()) {
			// the delete button
			Button deleteBtn = new Button(comp, SWT.FLAT);
			CmsSwtUtils.style(deleteBtn, FormStyle.deleteOverlay.style());
			FormData formData = new FormData();
			formData.right = new FormAttachment(rightWeight, 0);
			formData.top = new FormAttachment(topWeight, 0);
			deleteBtn.setLayoutData(formData);
			deleteBtn.moveAbove(body);

			deleteBtn.addSelectionListener(new SelectionAdapter() {
				private static final long serialVersionUID = 4304223543657238462L;

				@Override
				public void widgetSelected(SelectionEvent e) {
					super.widgetSelected(e);
//					if (MessageDialog.openConfirm(section.getShell(), "Confirm deletion",
//							"Are you really you want to remove this?")) {
//						Session session;
//						try {
//							session = sessionNode.getSession();
//							SwtSection parSection = section.getParentSection();
//							sessionNode.remove();
//							session.save();
//							refresh(parSection);
//							layout(parSection);
//						} catch (RepositoryException re) {
//							throw new JcrException("Unable to delete " + sessionNode, re);
//						}
//
//					}

				}
			});
		}
		return body;
	}

//	// LOCAL HELPERS FOR NODE MANAGEMENT
//	private Node getOrCreateNode(Node parent, String nodeName, String nodeType) throws RepositoryException {
//		Node node = null;
//		if (getCmsEditable().canEdit() && !parent.hasNode(nodeName)) {
//			node = JcrUtils.mkdirs(parent, nodeName, nodeType);
//			parent.getSession().save();
//		}
//
//		if (getCmsEditable().canEdit() || parent.hasNode(nodeName))
//			node = parent.getNode(nodeName);
//
//		return node;
//	}

	private SelectionListener getRemoveValueSelListener() {
		return new SelectionAdapter() {
			private static final long serialVersionUID = 9022259089907445195L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				Object source = e.getSource();
				if (source instanceof Button) {
					Button btn = (Button) source;
					Object obj = btn.getData(FormConstants.LINKED_VALUE);
					SwtEditablePart ep = findDataParent(btn);
					if (ep != null && ep instanceof EditableMultiStringProperty) {
						EditableMultiStringProperty emsp = (EditableMultiStringProperty) ep;
						List<String> values = emsp.getValues();
						if (values.contains(obj)) {
							values.remove(values.indexOf(obj));
							emsp.setValues(values);
							save(emsp);
							// TODO workaround to force refresh
							edit(emsp, 0);
							cancelEdit();
							layout(emsp);
						}
					}
				}
			}
		};
	}

	protected void setPropertySilently(Content node, QName propName, String value) {
		// TODO Clean this:
		// Format strings to replace \n
		value = value.replaceAll("\n", "<br/>");
		// Do not make the update if validation fails
//			try {
//				MarkupValidatorCopy.getInstance().validate(value);
//			} catch (Exception e) {
//				log.warn("Cannot set [" + value + "] on prop " + propName + "of " + node
//						+ ", String cannot be validated - " + e.getMessage());
//				return;
//			}
		// TODO check if the newly created property is of the correct type,
		// otherwise the property will be silently created with a STRING
		// property type.
		node.put(propName, value);
	}
}