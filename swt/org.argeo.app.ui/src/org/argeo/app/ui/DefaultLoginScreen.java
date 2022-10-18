package org.argeo.app.ui;

import org.argeo.api.acr.Content;
import org.argeo.api.cms.CmsContext;
import org.argeo.api.cms.ux.CmsView;
import org.argeo.cms.auth.CurrentUser;
import org.argeo.cms.swt.CmsSwtUtils;
import org.argeo.cms.swt.auth.CmsLogin;
import org.argeo.cms.ui.CmsUiProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/** Provides a login screen. */
public class DefaultLoginScreen implements CmsUiProvider {
	private CmsContext cmsContext;

	@Override
	public Control createUiPart(Composite parent, Content context) {
		CmsView cmsView = CmsSwtUtils.getCmsView(parent);
		if (!cmsView.isAnonymous())
			throw new IllegalStateException(CurrentUser.getUsername() + " is already logged in");

		parent.setLayout(new GridLayout());
		Composite loginArea = new Composite(parent, SWT.NONE);
		loginArea.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));

		CmsLogin cmsLogin = new CmsLogin(cmsView, cmsContext);
		cmsLogin.createUi(loginArea);
		return cmsLogin.getCredentialsBlock();
	}

	public void setCmsContext(CmsContext cmsContext) {
		this.cmsContext = cmsContext;
	}

}
