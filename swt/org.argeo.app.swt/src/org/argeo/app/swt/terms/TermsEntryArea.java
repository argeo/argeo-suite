package org.argeo.app.swt.terms;

import org.argeo.api.acr.Content;
import org.argeo.cms.swt.acr.SwtUiProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/** Entry area for managing the typologies. */
public class TermsEntryArea implements SwtUiProvider {

	@Override
	public Control createUiPart(Composite parent, Content content) {
		parent.setLayout(new GridLayout());
		Label lbl = new Label(parent, SWT.NONE);
		lbl.setText("Typologies");
		return lbl;
	}

}
