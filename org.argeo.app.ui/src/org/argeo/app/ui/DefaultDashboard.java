package org.argeo.app.ui;

import org.argeo.api.acr.Content;
import org.argeo.api.cms.ux.CmsView;
import org.argeo.cms.auth.CurrentUser;
import org.argeo.cms.swt.CmsSwtUtils;
import org.argeo.cms.ui.CmsUiProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/** Provides a dashboard. */
public class DefaultDashboard implements CmsUiProvider {

	@Override
	public Control createUiPart(Composite parent, Content context) {
		parent.setLayout(new GridLayout());
		CmsView cmsView = CmsSwtUtils.getCmsView(parent);
		if (cmsView.isAnonymous())
			throw new IllegalStateException("No user is not logged in");

		Label lbl = new Label(parent, SWT.NONE);
		lbl.setText("Welcome " + CurrentUser.getDisplayName() + "!");

		return lbl;
	}

}
