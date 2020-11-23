package org.argeo.suite.ui;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.CmsUserManager;
import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.cms.ui.viewers.Section;
import org.argeo.naming.LdapAttrs;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.osgi.service.useradmin.User;

/** Edit a suite user. */
public class SuiteUserUiProvider implements CmsUiProvider {
	private CmsUserManager cmsUserManager;

	@Override
	public Control createUi(Composite parent, Node context) throws RepositoryException {
		Section main = new Section(parent, SWT.NONE, context);
		main.setLayoutData(CmsUiUtils.fillAll());

		String uid = context.getName();
		User user = cmsUserManager.getUserFromLocalId(uid);

		Text givenName = new Text(main, SWT.SINGLE);
		givenName.setText(getUserProperty(user, LdapAttrs.givenName.name()));

		Text sn = new Text(main, SWT.SINGLE);
		sn.setText(getUserProperty(user, LdapAttrs.sn.name()));

		Text email = new Text(main, SWT.SINGLE);
		email.setText(getUserProperty(user, LdapAttrs.mail.name()));

		Label lbl = new Label(main, SWT.NONE);
		lbl.setText(uid);

		Label dnL = new Label(main, SWT.NONE);
		dnL.setText(user.getName());

		// roles
		Section rolesSection = new Section(main, SWT.NONE, context);
		new Label(rolesSection, SWT.NONE).setText("Roles:");
		String[] roles = cmsUserManager.getUserRoles(user.getName());
		for (String role : roles) {
			new Label(rolesSection, SWT.NONE).setText(role);
		}

		return lbl;
	}

	public void setCmsUserManager(CmsUserManager cmsUserManager) {
		this.cmsUserManager = cmsUserManager;
	}

	private String getUserProperty(Object element, String key) {
		Object value = ((User) element).getProperties().get(key);
		return value != null ? value.toString() : null;
	}

}
