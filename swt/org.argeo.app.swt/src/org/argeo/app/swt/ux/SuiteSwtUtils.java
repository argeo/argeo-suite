package org.argeo.app.swt.ux;

import java.util.function.Predicate;

import javax.xml.namespace.QName;

import org.argeo.api.acr.Content;
import org.argeo.api.acr.QNamed;
import org.argeo.api.cms.ux.Cms2DSize;
import org.argeo.api.cms.ux.CmsEditable;
import org.argeo.api.cms.ux.CmsStyle;
import org.argeo.app.ux.SuiteStyle;
import org.argeo.cms.Localized;
import org.argeo.cms.acr.ContentUtils;
import org.argeo.cms.swt.CmsSwtUtils;
import org.argeo.cms.swt.acr.Img;
import org.argeo.cms.swt.dialogs.CmsFeedback;
import org.argeo.cms.swt.dialogs.LightweightDialog;
import org.argeo.cms.swt.widgets.CmsLink;
import org.argeo.cms.swt.widgets.EditableText;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/** Static utilities implementing the look and feel of Argeo Suite with SWT. */
public class SuiteSwtUtils {
	/** creates a title bar composite with label and optional button */
	public static Composite addTitleBar(Composite parent, Localized title) {
		return addTitleBar(parent, title.lead());
	}

	/** creates a title bar composite with label and optional button */
	public static Composite addTitleBar(Composite parent, String title) {
		Composite titleBar = new Composite(parent, SWT.NONE);
		titleBar.setLayoutData(CmsSwtUtils.fillWidth());
		CmsSwtUtils.style(titleBar, SuiteStyle.titleContainer);

		titleBar.setLayout(CmsSwtUtils.noSpaceGridLayout(new GridLayout(2, false)));
		Label titleLbl = new Label(titleBar, SWT.NONE);
		titleLbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		CmsSwtUtils.style(titleLbl, SuiteStyle.titleLabel);
		titleLbl.setText(title);

//		if (isEditable) {
//			Button editBtn = new Button(titleBar, SWT.PUSH);
//			editBtn.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
//			CmsSwtUtils.style(editBtn, SuiteStyle.inlineButton);
//			editBtn.setText("Edit");
//		}
		return titleBar;
	}

	public static Label addFormLabel(Composite parent, String label) {
		Label lbl = new Label(parent, SWT.WRAP);
		lbl.setText(label);
		lbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		CmsSwtUtils.style(lbl, SuiteStyle.simpleLabel);
		return lbl;
	}

	public static Label addFormLabel(Composite parent, Localized msg) {
		return addFormLabel(parent, msg.lead());
	}

	public static Text addFormTextField(Composite parent, String text, String message, int style) {
		Text txt = new Text(parent, style);
		if (text != null)
			txt.setText(text);
		if (message != null)
			txt.setMessage(message);
		txt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		CmsSwtUtils.style(txt, SuiteStyle.simpleInput);
		return txt;
	}

	public static Text addFormTextField(Composite parent, String text, String message) {
		return addFormTextField(parent, text, message, SWT.NONE);
	}

//	public static Text addFormInputField(Composite parent, String placeholder) {
//		Text txt = new Text(parent, SWT.BORDER);
//
//		GridData gridData = CmsSwtUtils.fillWidth();
//		txt.setLayoutData(gridData);
//
//		if (placeholder != null)
//			txt.setText(placeholder);
//
//		CmsSwtUtils.style(txt, SuiteStyle.simpleInput);
//		return txt;
//	}

	public static Composite addLineComposite(Composite parent, int columns) {
		Composite lineComposite = new Composite(parent, SWT.NONE);
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		lineComposite.setLayoutData(gd);
		lineComposite.setLayout(new GridLayout(columns, false));
		CmsSwtUtils.style(lineComposite, SuiteStyle.formLine);
		return lineComposite;
	}

	/** creates a single horizontal-block composite for key:value display */
	public static Text addFormLine(Composite parent, Localized label, String text) {
		return addFormLine(parent, label.lead(), text);
	}

	/** creates a single horizontal-block composite for key:value display */
	public static Text addFormLine(Composite parent, String label, String text) {
		Composite lineComposite = addLineComposite(parent, 2);
		CmsSwtUtils.style(lineComposite, SuiteStyle.formLine);
		addFormLabel(lineComposite, label);
		Text txt = addFormTextField(lineComposite, text, null);
		txt.setEditable(false);
		txt.setLayoutData(CmsSwtUtils.fillWidth());
		return txt;
	}

//	public static Text addFormInput(Composite parent, String label, String placeholder) {
//		Composite lineComposite = addLineComposite(parent, 2);
//		addFormLabel(lineComposite, label);
//		Text txt = addFormInputField(lineComposite, placeholder);
//		txt.setLayoutData(CmsSwtUtils.fillWidth());
//		return txt;
//	}

	/**
	 * creates a single horizontal-block composite for key:value display, with
	 * offset value
	 */
	public static Text addFormLine(Composite parent, String label, String text, Integer offset) {
		Composite lineComposite = addLineComposite(parent, 3);
		Label offsetLbl = new Label(lineComposite, SWT.NONE);
		GridData gridData = new GridData();
		gridData.widthHint = offset;
		offsetLbl.setLayoutData(gridData);
		addFormLabel(lineComposite, label);
		Text txt = addFormTextField(lineComposite, text, null);
		txt.setLayoutData(CmsSwtUtils.fillWidth());
		return txt;
	}

	/** creates a single vertical-block composite for key:value display */
	public static Text addFormColumn(Composite parent, Localized label, String text) {
		return addFormColumn(parent, label.lead(), text);
	}

	/** creates a single vertical-block composite for key:value display */
	public static Text addFormColumn(Composite parent, String label, String text) {
		// Composite columnComposite = new Composite(parent, SWT.NONE);
		// columnComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
		// false));
		// columnComposite.setLayout(new GridLayout(1, false));
		addFormLabel(parent, label);
		Text txt = addFormTextField(parent, text, null);
		txt.setEditable(false);
		txt.setLayoutData(CmsSwtUtils.fillWidth());
		return txt;
	}

	public static Label createBoldLabel(Composite parent, Localized localized) {
		Label label = new Label(parent, SWT.LEAD);
		label.setText(localized.lead());
		label.setFont(EclipseUiUtils.getBoldFont(parent));
		label.setLayoutData(new GridData(SWT.LEAD, SWT.CENTER, false, false));
		return label;
	}

	/*
	 * CONTENT
	 */
	public static String toLink(Content content) {
		return content != null ? "#" + ContentUtils.cleanPathForUrl(content.getPath()) : null;
	}

	public static Text addFormLine(Composite parent, Localized label, Content content, QNamed property,
			CmsEditable cmsEditable) {
		return addFormLine(parent, label.lead(), content, property.qName(), cmsEditable);
	}

	public static EditableText addTextLine(Composite parent, int style, Localized msg, Content content, QNamed attr,
			CmsEditable cmsEditable, boolean line, Predicate<String> validator) {
		Composite parentToUse = line ? SuiteSwtUtils.addLineComposite(parent, 2) : parent;
		SuiteSwtUtils.addFormLabel(parentToUse, msg.lead());
		EditableText text = createFormText(parentToUse, style, msg, content, attr, cmsEditable, validator);
		return text;
	}

	public static EditableText createFormText(Composite parent, int style, Localized msg, Content content, QNamed attr,
			CmsEditable cmsEditable, Predicate<String> validator) {
		EditableText text = new EditableText(parent, style | SWT.FLAT | (cmsEditable.isEditing() ? 0 : SWT.READ_ONLY));
		text.setMessage("-");
		text.setLayoutData(CmsSwtUtils.fillWidth());
		text.setStyle(SuiteStyle.simpleInput);
		String txt = content.attr(attr);
		if (txt == null)
			txt = "";
		text.setText(txt);
		if (cmsEditable.isEditing())
			text.setMouseListener(new MouseAdapter() {

				private static final long serialVersionUID = 1L;

				@Override
				public void mouseDoubleClick(MouseEvent e) {
					String currentTxt = text.getText();
					text.startEditing();
					text.setText(currentTxt);

					Runnable save = () -> {
						String editedTxt = text.getText();
						if (validator != null) {
							if (!validator.test(editedTxt)) {
								text.stopEditing();
								text.setText(currentTxt);
								CmsFeedback.show(editedTxt + " is not properly formatted");
								return;
								// throw new IllegalArgumentException(editedTxt + " is not properly formatted");
							}
						}
						content.put(attr, editedTxt);
						text.stopEditing();
						text.setText(editedTxt);
						text.getParent().layout(new Control[] { text.getControl() });
					};
					((Text) text.getControl()).addSelectionListener(new SelectionListener() {

						private static final long serialVersionUID = 1L;

						@Override
						public void widgetSelected(SelectionEvent e) {
						}

						@Override
						public void widgetDefaultSelected(SelectionEvent e) {
							save.run();
						}
					});
					((Text) text.getControl()).addFocusListener(new FocusListener() {

						private static final long serialVersionUID = 333838002411959302L;

						@Override
						public void focusLost(FocusEvent event) {
							save.run();
						}

						@Override
						public void focusGained(FocusEvent event) {
						}
					});

				}

			});
		return text;
	}

	public static Text addFormLine(Composite parent, String label, Content content, QName property,
			CmsEditable cmsEditable) {
		Composite lineComposite = SuiteSwtUtils.addLineComposite(parent, 2);
		SuiteSwtUtils.addFormLabel(lineComposite, label);
		String text = content.attr(property);
		Text txt = SuiteSwtUtils.addFormTextField(lineComposite, text, null, SWT.WRAP);
		if (cmsEditable != null && cmsEditable.isEditing()) {
			txt.addModifyListener((e) -> {
				content.put(property, txt.getText());
			});
		} else {
			txt.setEditable(false);
		}
		txt.setLayoutData(CmsSwtUtils.fillWidth());
		return txt;
	}

	public static Text addFormColumn(Composite parent, Localized label, Content content, QNamed property,
			CmsEditable cmsEditable) {
		return addFormColumn(parent, label.lead(), content, property.qName(), cmsEditable);
	}

	public static Text addFormColumn(Composite parent, String label, Content content, QName property,
			CmsEditable cmsEditable) {
		SuiteSwtUtils.addFormLabel(parent, label);
		String text = content.attr(property);
		Text txt = SuiteSwtUtils.addFormTextField(parent, text, null, 0);
		if (cmsEditable != null && cmsEditable.isEditing()) {
			txt.addModifyListener((e) -> {
				content.put(property, txt.getText());
			});
		} else {
			txt.setEditable(false);
		}
		txt.setLayoutData(CmsSwtUtils.fillWidth());
		return txt;
	}

	/*
	 * LINKS
	 */

	/** Add a link to an internal content. */
	public static Control addLink(Composite parent, String label, Content node, CmsStyle style) {
		String target = toLink(node);
		CmsLink link = new CmsLink(label, target, style);
		return link.createUi(parent);
	}

	public static Control addExternalLink(Composite parent, String label, String url, String plainCssAnchorClass,
			boolean newWindow) {
		Label lbl = new Label(parent, SWT.NONE);
		CmsSwtUtils.markup(lbl);
		StringBuilder txt = new StringBuilder();
		txt.append("<a");
		if (plainCssAnchorClass != null)
			txt.append(" class='" + plainCssAnchorClass + "'");
		txt.append(" href='").append(url).append("'");
		if (newWindow) {
			txt.append(" target='blank_'");
		}
		txt.append(">");
		txt.append(label);
		txt.append("</a>");
		lbl.setText(txt.toString());
		return lbl;
	}

	/*
	 * IMAGES
	 */

	public static Img addPicture(Composite parent, Content file) {
		return addPicture(parent, file, null);
	}

	public static Img addPicture(Composite parent, Content file, Integer maxWidth) {
		return addPicture(parent, file, maxWidth, null);
	}

	public static Img addPicture(Composite parent, Content file, Integer maxWidth, Content link) {
		// TODO optimise
//		Integer width;
//		Integer height;
//		if (file.hasContentClass(EntityType.box)) {
//			width = file.get(SvgAttrs.width, Integer.class).get();
//			height = file.get(SvgAttrs.height, Integer.class).get();
//		} else {
//			try (InputStream in = file.open(InputStream.class)) {
//				ImageData imageData = new ImageData(in);
//				width = imageData.width;
//				height = imageData.height;
//			} catch (IOException e) {
//				throw new RuntimeException(e);
//			}
//		}
//
//		if (maxWidth != null && width > maxWidth) {
//			Double ratio = maxWidth.doubleValue() / width.doubleValue();
//			width = maxWidth;
//			height = (int) Math.rint(ratio * height);
//		}
//		Label img = new Label(parent, SWT.NONE);
//		CmsSwtUtils.markup(img);
//		StringBuffer txt = new StringBuffer();
//		String target = toLink(link);
//		if (target != null)
//			txt.append("<a href='").append(target).append("'>");
//		txt.append(CmsUiUtils.img(fileNode, width.toString(), height.toString()));
//		if (target != null)
//			txt.append("</a>");
//		img.setText(txt.toString());
//		if (parent.getLayout() instanceof GridLayout) {
//			GridData gd = new GridData(SWT.CENTER, SWT.CENTER, false, false);
//			gd.widthHint = width.intValue();
//			gd.heightHint = height.intValue();
//			img.setLayoutData(gd);
//		}

		Img img = new Img(parent, 0, file, new Cms2DSize(maxWidth != null ? maxWidth : 0, 0));
		if (link != null)
			img.setLink(link);

//		String target = toLink(link);
		if (link == null)
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
							scroll.setLayoutData(CmsSwtUtils.fillAll());
							scroll.setLayout(CmsSwtUtils.noSpaceGridLayout());
							scroll.setExpandHorizontal(true);
							scroll.setExpandVertical(true);
							// scroll.setAlwaysShowScrollBars(true);

							Composite c = new Composite(scroll, SWT.NONE);
							scroll.setContent(c);
							c.setLayout(new GridLayout());
							c.setLayoutData(CmsSwtUtils.fillAll());
							Img bigImg = new Img(c, 0, file);
//							Label bigImg = new Label(c, SWT.NONE);
//							CmsSwtUtils.markup(bigImg);
//							bigImg.setText(CmsUiUtils.img(fileNode, Jcr.get(content, EntityNames.SVG_WIDTH),
//									Jcr.get(content, EntityNames.SVG_HEIGHT)));
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
		img.initControl();
		return img;
	}

	/** singleton */
	private SuiteSwtUtils() {
	}

}
