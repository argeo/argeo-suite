package org.argeo.suite.web;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.auth.CurrentUser;
import org.argeo.cms.ui.CmsUiProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Choose between possible headers depending on the client logged-in status and
 * display type.
 */
public class DynamicHeader implements CmsUiProvider {

	private CmsUiProvider publicHeaderProvider;
	private CmsUiProvider privateHeaderProvider;

	@Override
	public Control createUi(Composite parent, Node context) throws RepositoryException {
		if (CurrentUser.isRegistered())
			return privateHeaderProvider.createUi(parent, context);
		else
			return publicHeaderProvider.createUi(parent, context);
	}

	public void setPrivateHeaderProvider(CmsUiProvider privateHeaderProvider) {
		this.privateHeaderProvider = privateHeaderProvider;
	}

	public void setPublicHeaderProvider(CmsUiProvider publicHeaderProvider) {
		this.publicHeaderProvider = publicHeaderProvider;
	}
}
