package org.argeo.suite.ui;

import java.io.IOException;
import java.io.InputStream;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.cms.LocaleUtils;
import org.argeo.cms.Localized;
import org.argeo.cms.auth.CurrentUser;
import org.argeo.cms.ui.CmsEditable;
import org.argeo.cms.ui.CmsTheme;
import org.argeo.cms.ui.CmsView;
import org.argeo.cms.ui.dialogs.LightweightDialog;
import org.argeo.cms.ui.util.CmsEvent;
import org.argeo.cms.ui.util.CmsIcon;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.entity.EntityNames;
import org.argeo.entity.EntityType;
import org.argeo.jcr.Jcr;
import org.argeo.jcr.JcrUtils;
import org.argeo.suite.SuiteRole;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.osgi.service.event.Event;

/** UI utilities related to the APAF project. */
public class SuiteUiUtils {

	/** Singleton. */
	private SuiteUiUtils() {
	}

	/** creates a title bar composite with label and optional button */
	public static void addTitleBar(Composite parent, String title, Boolean isEditable) {
		Composite titleBar = new Composite(parent, SWT.NONE);
		titleBar.setLayoutData(CmsUiUtils.fillWidth());
		CmsUiUtils.style(titleBar, SuiteStyle.titleContainer);

		titleBar.setLayout(CmsUiUtils.noSpaceGridLayout(new GridLayout(2, false)));
		Label titleLbl = new Label(titleBar, SWT.NONE);
		titleLbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		CmsUiUtils.style(titleLbl, SuiteStyle.titleLabel);
		titleLbl.setText(title);

		if (isEditable) {
			Button editBtn = new Button(titleBar, SWT.PUSH);
			editBtn.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
			CmsUiUtils.style(editBtn, SuiteStyle.inlineButton);
			editBtn.setText("Edit");
		}
	}

	public static Label addFormLabel(Composite parent, String label) {
		Label lbl = new Label(parent, SWT.WRAP);
		lbl.setText(label);
		// lbl.setLayoutData(new GridData(SWT.LEAD, SWT.CENTER, true, true));
		CmsUiUtils.style(lbl, SuiteStyle.simpleLabel);
		return lbl;
	}

	public static Text addFormTextField(Composite parent, String text, String message) {
		return addFormTextField(parent, text, message, SWT.NONE);
	}

	public static Text addFormTextField(Composite parent, String text, String message, int style) {
		Text txt = new Text(parent, style);
		if (text != null)
			txt.setText(text);
		if (message != null)
			txt.setMessage(message);
		txt.setLayoutData(new GridData(SWT.LEAD, SWT.CENTER, true, true));
		CmsUiUtils.style(txt, SuiteStyle.simpleText);
		return txt;
	}

	public static Text addFormInputField(Composite parent, String placeholder) {
		Text txt = new Text(parent, SWT.BORDER);

		GridData gridData = CmsUiUtils.fillWidth();
		txt.setLayoutData(gridData);

		if (placeholder != null)
			txt.setText(placeholder);

		CmsUiUtils.style(txt, SuiteStyle.simpleInput);
		return txt;
	}

	/** creates a single horizontal-block composite for key:value display */
	public static Text addFormLine(Composite parent, String label, String text) {
		Composite lineComposite = new Composite(parent, SWT.NONE);
		lineComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		lineComposite.setLayout(new GridLayout(2, false));
		CmsUiUtils.style(lineComposite, SuiteStyle.formLine);
		addFormLabel(lineComposite, label);
		Text txt = addFormTextField(lineComposite, text, null);
		txt.setEditable(false);
		txt.setLayoutData(CmsUiUtils.fillWidth());
		return txt;
	}

	public static Text addFormLine(Composite parent, String label, Node node, String property,
			CmsEditable cmsEditable) {
		Composite lineComposite = new Composite(parent, SWT.NONE);
		lineComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		lineComposite.setLayout(new GridLayout(2, false));
		CmsUiUtils.style(lineComposite, SuiteStyle.formLine);
		addFormLabel(lineComposite, label);
		String text = Jcr.get(node, property);
//		int style = cmsEditable.isEditing() ? SWT.WRAP : SWT.WRAP;
		Text txt = addFormTextField(lineComposite, text, null, SWT.WRAP);
		if (cmsEditable != null && cmsEditable.isEditing()) {
			txt.addModifyListener((e) -> {
				Jcr.set(node, property, txt.getText());
				Jcr.save(node);
			});
		} else {
			txt.setEditable(false);
		}
		txt.setLayoutData(CmsUiUtils.fillWidth());
		return txt;
	}

	public static Text addFormInput(Composite parent, String label, String placeholder) {
		Composite lineComposite = new Composite(parent, SWT.NONE);
		lineComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		lineComposite.setLayout(new GridLayout(2, false));
		CmsUiUtils.style(lineComposite, SuiteStyle.formLine);
		addFormLabel(lineComposite, label);
		Text txt = addFormInputField(lineComposite, placeholder);
		txt.setLayoutData(CmsUiUtils.fillWidth());
		return txt;
	}

	/**
	 * creates a single horizontal-block composite for key:value display, with
	 * offset value
	 */
	public static Text addFormLine(Composite parent, String label, String text, Integer offset) {
		Composite lineComposite = new Composite(parent, SWT.NONE);
		lineComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		lineComposite.setLayout(new GridLayout(3, false));
		CmsUiUtils.style(lineComposite, SuiteStyle.formLine);
		Label offsetLbl = new Label(lineComposite, SWT.NONE);
		GridData gridData = new GridData();
		gridData.widthHint = offset;
		offsetLbl.setLayoutData(gridData);
		addFormLabel(lineComposite, label);
		Text txt = addFormTextField(lineComposite, text, null);
		txt.setLayoutData(CmsUiUtils.fillWidth());
		return txt;
	}

	/** creates a single vertical-block composite for key:value display */
	public static Text addFormColumn(Composite parent, String label, String text) {
//		Composite columnComposite = new Composite(parent, SWT.NONE);
//		columnComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
//		columnComposite.setLayout(new GridLayout(1, false));
		addFormLabel(parent, label);
		Text txt = addFormTextField(parent, text, null);
		txt.setEditable(false);
		txt.setLayoutData(CmsUiUtils.fillWidth());
		return txt;
	}

	public static Text addFormColumn(Composite parent, String label, Node node, String property,
			CmsEditable cmsEditable) {
//		Composite columnComposite = new Composite(parent, SWT.NONE);
//		columnComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
//		columnComposite.setLayout(new GridLayout(1, false));
		addFormLabel(parent, label);
		String text = Jcr.get(node, property);
//		int style = cmsEditable.isEditing() ? SWT.WRAP : SWT.WRAP;
		Text txt = addFormTextField(parent, text, null, SWT.WRAP);
		if (cmsEditable != null && cmsEditable.isEditing()) {
			txt.addModifyListener((e) -> {
				Jcr.set(node, property, txt.getText());
				Jcr.save(node);
			});
		} else {
			txt.setEditable(false);
		}
		txt.setLayoutData(CmsUiUtils.fillWidth());
		return txt;
	}

	public static Label createBoldLabel(Composite parent, Localized localized) {
		Label label = new Label(parent, SWT.LEAD);
		label.setText(localized.lead());
		label.setFont(EclipseUiUtils.getBoldFont(parent));
		label.setLayoutData(new GridData(SWT.LEAD, SWT.CENTER, false, false));
		return label;
	}

	public static Label addFormPicture(Composite parent, String label, Node fileNode) throws RepositoryException {
		Composite lineComposite = new Composite(parent, SWT.NONE);
		lineComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		lineComposite.setLayout(new GridLayout(2, true));
		CmsUiUtils.style(lineComposite, SuiteStyle.formLine);
		addFormLabel(lineComposite, label);

		return addPicture(lineComposite, fileNode);
	}

	public static Label addPicture(Composite parent, Node fileNode) throws RepositoryException {
		return addPicture(parent, fileNode, null);
	}

	public static Label addPicture(Composite parent, Node fileNode, Integer maxWidth) throws RepositoryException {
		Node content = fileNode.getNode(Node.JCR_CONTENT);
		// TODO move it deeper in the middleware.
		if (!content.isNodeType(EntityType.box.get())) {
			if (content.getSession().hasPermission(content.getPath(), Session.ACTION_SET_PROPERTY)) {
				try (InputStream in = JcrUtils.getFileAsStream(fileNode)) {
					ImageData imageData = new ImageData(in);
					content.addMixin(EntityType.box.get());
					content.setProperty(EntityNames.SVG_WIDTH, imageData.width);
					content.setProperty(EntityNames.SVG_HEIGHT, imageData.height);
					content.getSession().save();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}

		// TODO optimise
		Long width;
		Long height;
		if (content.isNodeType(EntityType.box.get())) {
			width = content.getProperty(EntityNames.SVG_WIDTH).getLong();
			height = content.getProperty(EntityNames.SVG_HEIGHT).getLong();
		} else {
			try (InputStream in = JcrUtils.getFileAsStream(fileNode)) {
				ImageData imageData = new ImageData(in);
				width = Long.valueOf(imageData.width);
				height = Long.valueOf(imageData.height);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		if (maxWidth != null && width > maxWidth) {
			Double ratio = maxWidth.doubleValue() / width.doubleValue();
			width = maxWidth.longValue();
			height = Math.round(ratio * height);
		}
		Label img = new Label(parent, SWT.NONE);
		CmsUiUtils.markup(img);
		img.setText(CmsUiUtils.img(fileNode, width.toString(), height.toString()));
		if (parent.getLayout() instanceof GridLayout) {
			GridData gd = new GridData(SWT.CENTER, SWT.CENTER, false, false);
			gd.widthHint = width.intValue();
			gd.heightHint = height.intValue();
			img.setLayoutData(gd);
		}
		img.addMouseListener(new MouseListener() {
			private static final long serialVersionUID = -1362242049325206168L;

			@Override
			public void mouseUp(MouseEvent e) {
			}

			@Override
			public void mouseDown(MouseEvent e) {
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				LightweightDialog dialog = new LightweightDialog(img.getShell()) {

					@Override
					protected Control createDialogArea(Composite parent) {
						parent.setLayout(new GridLayout());
						ScrolledComposite scroll = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
						scroll.setLayoutData(CmsUiUtils.fillAll());
						scroll.setLayout(CmsUiUtils.noSpaceGridLayout());
						scroll.setExpandHorizontal(true);
						scroll.setExpandVertical(true);
						// scroll.setAlwaysShowScrollBars(true);

						Composite c = new Composite(scroll, SWT.NONE);
						scroll.setContent(c);
						c.setLayout(new GridLayout());
						c.setLayoutData(CmsUiUtils.fillAll());
						Label bigImg = new Label(c, SWT.NONE);
						CmsUiUtils.markup(bigImg);
						bigImg.setText(CmsUiUtils.img(fileNode, Jcr.get(content, EntityNames.SVG_WIDTH),
								Jcr.get(content, EntityNames.SVG_HEIGHT)));
						bigImg.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
						return bigImg;
					}

					@Override
					protected Point getInitialSize() {
						Point shellSize = img.getShell().getSize();
						return new Point(shellSize.x - 100, shellSize.y - 100);
					}

				};
				dialog.open();
			}
		});
		return img;
	}

	public static boolean isCoworker(CmsView cmsView) {
		boolean coworker = cmsView.doAs(() -> CurrentUser.isInRole(SuiteRole.coworker.dn()));
		return coworker;
	}

	public static boolean isTopic(Event event, CmsEvent cmsEvent) {
		return event.getTopic().equals(cmsEvent.topic());
	}

	public static Button createLayerButton(Composite parent, String layer, Localized msg, CmsIcon icon,
			ClassLoader l10nClassLoader) {
		CmsTheme theme = CmsTheme.getCmsTheme(parent);
		Button button = new Button(parent, SWT.PUSH);
		CmsUiUtils.style(button, SuiteStyle.leadPane);
		if (icon != null)
			button.setImage(icon.getBigIcon(theme));
		button.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, true, false));
		// button.setToolTipText(msg.lead());
		if (msg != null) {
			Label lbl = new Label(parent, SWT.CENTER);
			CmsUiUtils.style(lbl, SuiteStyle.leadPane);
			String txt = LocaleUtils.lead(msg, l10nClassLoader);
//			String txt = msg.lead();
			lbl.setText(txt);
			lbl.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, true, false));
		}
		CmsUiUtils.sendEventOnSelect(button, SuiteEvent.switchLayer.topic(), SuiteEvent.LAYER, layer);
		return button;
	}

//	public static String createAndConfigureEntity(Shell shell, Session referenceSession, String mainMixin,
//			String... additionnalProps) {
//
//		Session tmpSession = null;
//		Session mainSession = null;
//		try {
//			// FIXME would not work if home is another physical workspace
//			tmpSession = referenceSession.getRepository().login(NodeConstants.HOME_WORKSPACE);
//			Node draftNode = null;
//			for (int i = 0; i < additionnalProps.length - 1; i += 2) {
//				draftNode.setProperty(additionnalProps[i], additionnalProps[i + 1]);
//			}
//			Wizard wizard = null;
//			CmsWizardDialog dialog = new CmsWizardDialog(shell, wizard);
//			// WizardDialog dialog = new WizardDialog(shell, wizard);
//			if (dialog.open() == Window.OK) {
//				String parentPath = null;// "/" + appService.getBaseRelPath(mainMixin);
//				// FIXME it should be possible to specify the workspace
//				mainSession = referenceSession.getRepository().login();
//				Node parent = mainSession.getNode(parentPath);
//				Node task = null;// appService.publishEntity(parent, mainMixin, draftNode);
////				task = appService.saveEntity(task, false);
//				referenceSession.refresh(true);
//				return task.getPath();
//			}
//			return null;
//		} catch (RepositoryException e1) {
//			throw new JcrException(
//					"Unable to create " + mainMixin + " entity with session " + referenceSession.toString(), e1);
//		} finally {
//			JcrUtils.logoutQuietly(tmpSession);
//			JcrUtils.logoutQuietly(mainSession);
//		}
//	}

}
