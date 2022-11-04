package org.argeo.app.ui.people;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.argeo.api.acr.Content;
import org.argeo.app.ui.SuiteMsg;
import org.argeo.app.ui.SuiteStyle;
import org.argeo.app.ui.SuiteUiUtils;
import org.argeo.cms.CmsUserManager;
import org.argeo.cms.Localized;
import org.argeo.cms.swt.CmsSwtUtils;
import org.argeo.cms.swt.acr.SwtSection;
import org.argeo.cms.swt.acr.SwtUiProvider;
import org.argeo.cms.swt.widgets.EditableText;
import org.argeo.util.naming.LdapAttrs;
import org.argeo.util.naming.LdapObjs;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.osgi.service.useradmin.User;

/** Edit a suite user. */
public class PersonUiProvider implements SwtUiProvider {
	private String[] availableRoles;
	private CmsUserManager cmsUserManager;

	@Override
	public Control createUiPart(Composite parent, Content context) {
		SwtSection main = new SwtSection(parent, SWT.NONE, context);
		main.setLayoutData(CmsSwtUtils.fillAll());

		main.setLayout(new GridLayout(2, false));

		User user = context.adapt(User.class);

		if (context.hasContentClass(LdapObjs.person.qName())) {
			addFormLine(main, SuiteMsg.firstName, context, LdapAttrs.givenName);
			addFormLine(main, SuiteMsg.lastName, context, LdapAttrs.sn);
			addFormLine(main, SuiteMsg.email, context, LdapAttrs.mail);

			Composite rolesSection = new Composite(main, SWT.NONE);
			rolesSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
			rolesSection.setLayout(new GridLayout());
			List<String> roles = Arrays.asList(cmsUserManager.getUserRoles(user.getName()));
			for (String role : roles) {
				// new Label(rolesSection, SWT.NONE).setText(role);
				Button radio = new Button(rolesSection, SWT.CHECK);
				radio.setText(role);
				if (roles.contains(role))
					radio.setSelection(true);
			}

//			Composite facetsSection = new Composite(main, SWT.NONE);
//			facetsSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
//			facetsSection.setLayout(new GridLayout());
//			if (context.hasContentClass(LdapObjs.groupOfNames.qName())) {
//				String[] members = context.attr(LdapAttrs.member.qName()).split("\n");
//				for (String member : members) {
//					new Label(facetsSection, SWT.NONE).setText(member);
//				}
//			}
		}

//		if (user instanceof Group) {
//			String cn = context.getName().getLocalPart();
//			Text cnT = SuiteUiUtils.addFormLine(main, "uid", getUserProperty(user, LdapAttrs.uid.name()));
//			cnT.setText(cn);
//
//		} else {
//			String uid = context.getName().getLocalPart();
//
////		Text givenName = new Text(main, SWT.SINGLE);
////		givenName.setText(getUserProperty(user, LdapAttrs.givenName.name()));
//			Text givenName = SuiteUiUtils.addFormInput(main, SuiteMsg.firstName.lead(),
//					getUserProperty(user, LdapAttrs.givenName.name()));
//
//			Text sn = SuiteUiUtils.addFormInput(main, SuiteMsg.lastName.lead(),
//					getUserProperty(user, LdapAttrs.sn.name()));
//			// sn.setText(getUserProperty(user, LdapAttrs.sn.name()));
//
//			Text email = SuiteUiUtils.addFormInput(main, SuiteMsg.email.lead(),
//					getUserProperty(user, LdapAttrs.mail.name()));
//			// email.setText(getUserProperty(user, LdapAttrs.mail.name()));
//
//			Text uidT = SuiteUiUtils.addFormLine(main, "uid", getUserProperty(user, LdapAttrs.uid.name()));
//			uidT.setText(uid);
//
////		Label dnL = new Label(main, SWT.NONE);
////		dnL.setText(user.getName());
//
//			// roles
//			// Section rolesSection = new Section(main, SWT.NONE, context);
//			Composite rolesSection = new Composite(main, SWT.NONE);
//			// rolesSection.setText("Roles");
//			rolesSection.setLayoutData(CmsSwtUtils.fillWidth());
//			rolesSection.setLayout(new GridLayout());
//			// new Label(rolesSection, SWT.NONE).setText("Roles:");
//			List<String> roles = Arrays.asList(cmsUserManager.getUserRoles(user.getName()));
//			for (String role : availableRoles) {
//				// new Label(rolesSection, SWT.NONE).setText(role);
//				Button radio = new Button(rolesSection, SWT.CHECK);
//				radio.setText(role);
//				if (roles.contains(role))
//					radio.setSelection(true);
//			}
//		}

		return main;
	}

	private void addFormLine(SwtSection parent, Localized msg, Content context, LdapAttrs attr) {
		SuiteUiUtils.addFormLabel(parent, msg.lead());
		EditableText text = new EditableText(parent, SWT.SINGLE | SWT.FLAT);
		text.setLayoutData(CmsSwtUtils.fillWidth());
		text.setStyle(SuiteStyle.simpleInput);
		String txt = context.attr(attr.qName());
		if (txt == null) // FIXME understand why email is not found in IPA
			txt = "";
		text.setText(txt);
		text.setMouseListener(new MouseAdapter() {

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				String currentTxt = text.getText();
				text.startEditing();
				text.setText(currentTxt);
				((Text) text.getControl()).addSelectionListener(new SelectionListener() {

					@Override
					public void widgetSelected(SelectionEvent e) {
					}

					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
						String editedTxt = text.getText();
						text.stopEditing();
						text.setText(editedTxt);
						text.getParent().layout(new Control[] { text.getControl() });
					}
				});
			}

		});
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
		// cmsUserManager.getRoles(null);
	}
}
