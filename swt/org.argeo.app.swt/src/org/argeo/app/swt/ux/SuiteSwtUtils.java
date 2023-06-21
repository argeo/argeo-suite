package org.argeo.app.swt.ux;

import org.argeo.api.acr.Content;
import org.argeo.app.ux.SuiteStyle;
import org.argeo.cms.Localized;
import org.argeo.cms.swt.CmsSwtUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/** Static utilities implementing the look and feel of Argeo Suite with SWT. */
public class SuiteSwtUtils {
	/** creates a title bar composite with label and optional button */
	public static void addTitleBar(Composite parent, String title, Boolean isEditable) {
		Composite titleBar = new Composite(parent, SWT.NONE);
		titleBar.setLayoutData(CmsSwtUtils.fillWidth());
		CmsSwtUtils.style(titleBar, SuiteStyle.titleContainer);

		titleBar.setLayout(CmsSwtUtils.noSpaceGridLayout(new GridLayout(2, false)));
		Label titleLbl = new Label(titleBar, SWT.NONE);
		titleLbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		CmsSwtUtils.style(titleLbl, SuiteStyle.titleLabel);
		titleLbl.setText(title);

		if (isEditable) {
			Button editBtn = new Button(titleBar, SWT.PUSH);
			editBtn.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
			CmsSwtUtils.style(editBtn, SuiteStyle.inlineButton);
			editBtn.setText("Edit");
		}
	}

	public static Label addFormLabel(Composite parent, String label) {
		Label lbl = new Label(parent, SWT.WRAP);
		lbl.setText(label);
		// lbl.setLayoutData(new GridData(SWT.LEAD, SWT.CENTER, true, true));
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
		txt.setLayoutData(new GridData(SWT.LEAD, SWT.CENTER, true, true));
		CmsSwtUtils.style(txt, SuiteStyle.simpleText);
		return txt;
	}

	public static Text addFormTextField(Composite parent, String text, String message) {
		return addFormTextField(parent, text, message, SWT.NONE);
	}

	public static Text addFormInputField(Composite parent, String placeholder) {
		Text txt = new Text(parent, SWT.BORDER);

		GridData gridData = CmsSwtUtils.fillWidth();
		txt.setLayoutData(gridData);

		if (placeholder != null)
			txt.setText(placeholder);

		CmsSwtUtils.style(txt, SuiteStyle.simpleInput);
		return txt;
	}

	public static Composite addLineComposite(Composite parent, int columns) {
		Composite lineComposite = new Composite(parent, SWT.NONE);
		lineComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		lineComposite.setLayout(new GridLayout(columns, false));
		CmsSwtUtils.style(lineComposite, SuiteStyle.formLine);
		return lineComposite;
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

	public static Text addFormInput(Composite parent, String label, String placeholder) {
		Composite lineComposite = addLineComposite(parent, 2);
		addFormLabel(lineComposite, label);
		Text txt = addFormInputField(lineComposite, placeholder);
		txt.setLayoutData(CmsSwtUtils.fillWidth());
		return txt;
	}

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

	public static String toLink(Content node) {
		return node != null ? "#" + CmsSwtUtils.cleanPathForUrl(SwtArgeoApp.nodeToState(node)) : null;
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

	/** singleton */
	private SuiteSwtUtils() {
	}

}
