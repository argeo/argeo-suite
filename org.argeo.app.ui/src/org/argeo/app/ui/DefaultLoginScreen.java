package org.argeo.app.ui;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.api.cms.CmsView;
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

	@Override
	public Control createUi(Composite parent, Node context) throws RepositoryException {
		CmsView cmsView = CmsSwtUtils.getCmsView(parent);
		if (!cmsView.isAnonymous())
			throw new IllegalStateException(CurrentUser.getUsername() + " is already logged in");

		parent.setLayout(new GridLayout());
		Composite loginArea = new Composite(parent, SWT.NONE);
		loginArea.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		
		CmsLogin cmsLogin = new CmsLogin(cmsView);
		cmsLogin.createUi(loginArea);
		return cmsLogin.getCredentialsBlock();
	}

}
