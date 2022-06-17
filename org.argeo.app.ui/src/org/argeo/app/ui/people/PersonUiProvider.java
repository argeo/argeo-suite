package org.argeo.app.ui.people;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.argeo.api.acr.Content;
import org.argeo.app.ui.SuiteMsg;
import org.argeo.app.ui.SuiteUiUtils;
import org.argeo.cms.CmsUserManager;
import org.argeo.cms.swt.CmsSwtUtils;
import org.argeo.cms.swt.widgets.SwtSection;
import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.util.naming.LdapAttrs;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.User;

/** Edit a suite user. */
public class PersonUiProvider implements CmsUiProvider {
	private String[] availableRoles;
	private CmsUserManager cmsUserManager;

	@Override
	public Control createUiPart(Composite parent, Content context) {
		SwtSection main = new SwtSection(parent, SWT.NONE, context);
		main.setLayoutData(CmsSwtUtils.fillAll());

		User user = context.adapt(User.class);

		if (user instanceof Group) {
			String cn = context.getName().getLocalPart().split("=")[1];
			Text cnT = SuiteUiUtils.addFormLine(main, "uid", getUserProperty(user, LdapAttrs.uid.name()));
			cnT.setText(cn);

		} else {
			String uid = context.getName().getLocalPart().split("=")[1];

//		Text givenName = new Text(main, SWT.SINGLE);
//		givenName.setText(getUserProperty(user, LdapAttrs.givenName.name()));
			Text givenName = SuiteUiUtils.addFormInput(main, SuiteMsg.firstName.lead(),
					getUserProperty(user, LdapAttrs.givenName.name()));

			Text sn = SuiteUiUtils.addFormInput(main, SuiteMsg.lastName.lead(),
					getUserProperty(user, LdapAttrs.sn.name()));
			// sn.setText(getUserProperty(user, LdapAttrs.sn.name()));

			Text email = SuiteUiUtils.addFormInput(main, SuiteMsg.email.lead(),
					getUserProperty(user, LdapAttrs.mail.name()));
			// email.setText(getUserProperty(user, LdapAttrs.mail.name()));

			Text uidT = SuiteUiUtils.addFormLine(main, "uid", getUserProperty(user, LdapAttrs.uid.name()));
			uidT.setText(uid);

//		Label dnL = new Label(main, SWT.NONE);
//		dnL.setText(user.getName());

			// roles
			// Section rolesSection = new Section(main, SWT.NONE, context);
			Composite rolesSection = new Composite(main, SWT.NONE);
			// rolesSection.setText("Roles");
			rolesSection.setLayoutData(CmsSwtUtils.fillWidth());
			rolesSection.setLayout(new GridLayout());
			// new Label(rolesSection, SWT.NONE).setText("Roles:");
			List<String> roles = Arrays.asList(cmsUserManager.getUserRoles(user.getName()));
			for (String role : availableRoles) {
				// new Label(rolesSection, SWT.NONE).setText(role);
				Button radio = new Button(rolesSection, SWT.CHECK);
				radio.setText(role);
				if (roles.contains(role))
					radio.setSelection(true);
			}
		}

		return main;
	}

	public void setCmsUserManager(CmsUserManager cmsUserManager) {
		this.cmsUserManager = cmsUserManager;
	}

	private String getUserProperty(Object element, String key) {
		Object value = ((User) element).getProperties().get(key);
		return value != null ? value.toString() : null;
	}

	public void init(Map<String, Object> properties) {
		availableRoles = (String[]) properties.get("availableRoles");
	}
}
