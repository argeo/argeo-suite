package org.argeo.app.ui.people;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.argeo.api.acr.Content;
import org.argeo.api.acr.QNamed;
import org.argeo.api.acr.ldap.LdapAttrs;
import org.argeo.api.acr.ldap.LdapObjs;
import org.argeo.api.cms.directory.CmsUserManager;
import org.argeo.app.api.SuiteRole;
import org.argeo.app.ui.SuiteMsg;
import org.argeo.app.ui.SuiteStyle;
import org.argeo.app.ui.SuiteUiUtils;
import org.argeo.cms.CmsMsg;
import org.argeo.cms.CurrentUser;
import org.argeo.cms.Localized;
import org.argeo.cms.RoleNameUtils;
import org.argeo.cms.SystemRole;
import org.argeo.cms.auth.CmsRole;
import org.argeo.cms.swt.CmsSwtUtils;
import org.argeo.cms.swt.Selected;
import org.argeo.cms.swt.acr.SwtSection;
import org.argeo.cms.swt.acr.SwtUiProvider;
import org.argeo.cms.swt.dialogs.CmsFeedback;
import org.argeo.cms.swt.widgets.EditableText;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.osgi.service.useradmin.User;

/** Edit a suite user. */
public class PersonUiProvider implements SwtUiProvider {
	private CmsUserManager cmsUserManager;

	@Override
	public Control createUiPart(Composite parent, Content context) {
		SwtSection main = new SwtSection(parent, SWT.NONE, context);
		main.setLayoutData(CmsSwtUtils.fillAll());

		main.setLayout(new GridLayout(2, false));

		User user = context.adapt(User.class);

		String roleContext = RoleNameUtils.getContext(user.getName());

		if (context.hasContentClass(LdapObjs.person.qName())) {

			addFormLine(main, SuiteMsg.firstName, context, LdapAttrs.givenName);
			addFormLine(main, SuiteMsg.lastName, context, LdapAttrs.sn);
			addFormLine(main, SuiteMsg.email, context, LdapAttrs.mail);
		}

		if (context.hasContentClass(LdapObjs.posixAccount.qName())) {

			SwtSection rolesSection = new SwtSection(main, SWT.NONE);
			rolesSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
			rolesSection.setLayout(new GridLayout(2, false));
			List<String> roles = Arrays.asList(cmsUserManager.getUserRoles(user.getName()));
			addRoleCheckBox(rolesSection, SuiteMsg.coworkerRole, SuiteRole.coworker, roleContext, roles);
			addRoleCheckBox(rolesSection, SuiteMsg.publisherRole, SuiteRole.publisher, roleContext, roles);
			addRoleCheckBox(rolesSection, SuiteMsg.userAdminRole, CmsRole.userAdmin, roleContext, roles);

//			Composite facetsSection = new Composite(main, SWT.NONE);
//			facetsSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
//			facetsSection.setLayout(new GridLayout());
//			if (context.hasContentClass(LdapObjs.groupOfNames.qName())) {
//				String[] members = context.attr(LdapAttrs.member.qName()).split("\n");
//				for (String member : members) {
//					new Label(facetsSection, SWT.NONE).setText(member);
//				}
//			}
			if (CurrentUser.implies(CmsRole.userAdmin, roleContext)) {
				SwtSection changePasswordSection = new SwtSection(main, SWT.BORDER);
				changePasswordSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
				changePasswordSection.setLayout(new GridLayout(2, false));
//				SuiteUiUtils.addFormLabel(changePasswordSection, CmsMsg.changePassword)
//						.setLayoutData(new GridData(SWT.LEAD, SWT.CENTER, false, false, 2, 1));
				SuiteUiUtils.addFormLabel(changePasswordSection, CmsMsg.newPassword);
				Text newPasswordT = SuiteUiUtils.addFormTextField(changePasswordSection, null, null,
						SWT.PASSWORD | SWT.BORDER);
				newPasswordT.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
				SuiteUiUtils.addFormLabel(changePasswordSection, CmsMsg.repeatNewPassword);
				Text repeatNewPasswordT = SuiteUiUtils.addFormTextField(changePasswordSection, null, null,
						SWT.PASSWORD | SWT.BORDER);
				repeatNewPasswordT.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
				Button apply = new Button(changePasswordSection, SWT.FLAT);
				apply.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false, 2, 1));
				apply.setText(CmsMsg.changePassword.lead());
				apply.addSelectionListener((Selected) (e) -> {
					try {
						char[] newPassword = newPasswordT.getTextChars();
						char[] repeatNewPassword = repeatNewPasswordT.getTextChars();
						if (newPassword.length > 0 && Arrays.equals(newPassword, repeatNewPassword)) {
							cmsUserManager.resetPassword(user.getName(), newPassword);
							CmsFeedback.show(CmsMsg.passwordChanged.lead());
						} else {
							CmsFeedback.error(CmsMsg.invalidPassword.lead(), null);
						}
					} catch (Exception e1) {
						CmsFeedback.error(CmsMsg.invalidPassword.lead(), e1);
					}
				});
			}
		}

		return main;
	}

	private void addFormLine(SwtSection parent, Localized msg, Content content, QNamed attr) {
		SuiteUiUtils.addFormLabel(parent, msg.lead());
		EditableText text = new EditableText(parent, SWT.SINGLE | SWT.FLAT);
		text.setLayoutData(CmsSwtUtils.fillWidth());
		text.setStyle(SuiteStyle.simpleInput);
		String txt = content.attr(attr);
		if (txt == null) // FIXME understand why email is not found in IPA
			txt = "";
		text.setText(txt);
		text.setMouseListener(new MouseAdapter() {

			private static final long serialVersionUID = 1L;

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				String currentTxt = text.getText();
				text.startEditing();
				text.setText(currentTxt);
				((Text) text.getControl()).addSelectionListener(new SelectionListener() {

					private static final long serialVersionUID = 1L;

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

	private void addRoleCheckBox(SwtSection parent, Localized msg, SystemRole systemRole, String roleContext,
			List<String> roles) {
		Button radio = new Button(parent, SWT.CHECK);
		radio.setSelection(false);
		roles: for (String dn : roles) {
			if (systemRole.implied(dn, roleContext)) {
				radio.setSelection(true);
				break roles;
			}
		}

		if (systemRole.equals(CmsRole.userAdmin)) {
			if (!CurrentUser.isUserContext(roleContext) && CurrentUser.implies(CmsRole.userAdmin, roleContext)) {
				// a user admin cannot modify the user admins of their own context
				radio.setEnabled(true);
			} else {
				radio.setEnabled(false);
			}
		} else {
			radio.setEnabled(CurrentUser.implies(CmsRole.userAdmin, roleContext));
		}
		new Label(parent, 0).setText(msg.lead());

	}

	public void setCmsUserManager(CmsUserManager cmsUserManager) {
		this.cmsUserManager = cmsUserManager;
	}

//	private String getUserProperty(Object element, String key) {
//		Object value = ((User) element).getProperties().get(key);
//		return value != null ? value.toString() : null;
//	}

	public void init(Map<String, Object> properties) {
	}
}
