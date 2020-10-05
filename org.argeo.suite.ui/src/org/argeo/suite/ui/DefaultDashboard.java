package org.argeo.suite.ui;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.auth.CurrentUser;
import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.cms.ui.CmsView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/** Provides a dashboard. */
public class DefaultDashboard implements CmsUiProvider {

	@Override
	public Control createUi(Composite parent, Node context) throws RepositoryException {
		parent.setLayout(new GridLayout());
		CmsView cmsView = CmsView.getCmsView(parent);
		if (cmsView.isAnonymous())
			throw new IllegalStateException("No user is not logged in");

		Label lbl = new Label(parent, SWT.NONE);
		lbl.setText("Welcome " + CurrentUser.getDisplayName() + "!");

		return lbl;
	}

}
