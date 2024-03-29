package org.argeo.app.ui.people;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.argeo.api.acr.Content;
import org.argeo.api.acr.QNamed;
import org.argeo.api.acr.ldap.LdapAttr;
import org.argeo.api.acr.ldap.LdapObj;
import org.argeo.api.app.SuiteRole;
import org.argeo.api.cms.auth.RoleNameUtils;
import org.argeo.api.cms.auth.SystemRole;
import org.argeo.api.cms.directory.CmsGroup;
import org.argeo.api.cms.directory.CmsUser;
import org.argeo.api.cms.directory.CmsUserManager;
import org.argeo.api.cms.directory.HierarchyUnit;
import org.argeo.api.cms.directory.HierarchyUnit.Type;
import org.argeo.app.swt.ux.SuiteSwtUtils;
import org.argeo.app.ux.SuiteMsg;
import org.argeo.app.ux.SuiteStyle;
import org.argeo.cms.CmsMsg;
import org.argeo.cms.CurrentUser;
import org.argeo.cms.Localized;
import org.argeo.cms.auth.CmsSystemRole;
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

/** Edit a suite user. */
public class PersonUiProvider implements SwtUiProvider {
	private CmsUserManager cmsUserManager;

	@Override
	public Control createUiPart(Composite parent, Content context) {
		SwtSection main = new SwtSection(parent, SWT.NONE, context);
		main.setLayoutData(CmsSwtUtils.fillAll());

		main.setLayout(new GridLayout(2, false));

		CmsUser user = context.adapt(CmsUser.class);

		Content hierarchyUnitContent = context.getParent().getParent();
		HierarchyUnit hierarchyUnit = hierarchyUnitContent.adapt(HierarchyUnit.class);

		String roleContext = RoleNameUtils.getContext(user.getName());

		if (context.hasContentClass(LdapObj.person.qName())) {

			addFormLine(main, SuiteMsg.firstName, context, LdapAttr.givenName);
			addFormLine(main, SuiteMsg.lastName, context, LdapAttr.sn);
			addFormLine(main, SuiteMsg.email, context, LdapAttr.mail);
		}

		if (context.hasContentClass(LdapObj.posixAccount.qName())) {
			if (hierarchyUnitContent.hasContentClass(LdapObj.organization)) {
				SwtSection rolesSection = new SwtSection(main, SWT.NONE);
				rolesSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
				rolesSection.setLayout(new GridLayout(2, false));
				List<String> roles = Arrays.asList(cmsUserManager.getUserRoles(user.getName()));
				addRoleCheckBox(rolesSection, hierarchyUnit, user, SuiteMsg.coworkerRole, SuiteRole.coworker,
						roleContext, roles);
				addRoleCheckBox(rolesSection, hierarchyUnit, user, SuiteMsg.publisherRole, SuiteRole.publisher,
						roleContext, roles);
				addRoleCheckBox(rolesSection, hierarchyUnit, user, SuiteMsg.userAdminRole, CmsSystemRole.userAdmin,
						roleContext, roles);
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
			if (CurrentUser.implies(CmsSystemRole.userAdmin, roleContext)) {
				SwtSection changePasswordSection = new SwtSection(main, SWT.BORDER);
				changePasswordSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
				changePasswordSection.setLayout(new GridLayout(2, false));
//				SuiteUiUtils.addFormLabel(changePasswordSection, CmsMsg.changePassword)
//						.setLayoutData(new GridData(SWT.LEAD, SWT.CENTER, false, false, 2, 1));
				SuiteSwtUtils.addFormLabel(changePasswordSection, CmsMsg.newPassword);
				Text newPasswordT = SuiteSwtUtils.addFormTextField(changePasswordSection, null, null,
						SWT.PASSWORD | SWT.BORDER);
				newPasswordT.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
				SuiteSwtUtils.addFormLabel(changePasswordSection, CmsMsg.repeatNewPassword);
				Text repeatNewPasswordT = SuiteSwtUtils.addFormTextField(changePasswordSection, null, null,
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
		SuiteSwtUtils.addFormLabel(parent, msg.lead());
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
						content.put(attr, editedTxt);
						text.stopEditing();
						text.setText(editedTxt);
						text.getParent().layout(new Control[] { text.getControl() });
					}
				});
			}

		});
	}

	private void addRoleCheckBox(SwtSection parent, HierarchyUnit hierarchyUnit, CmsUser user, Localized msg,
			SystemRole systemRole, String roleContext, List<String> roles) {
		Button radio = new Button(parent, SWT.CHECK);
		radio.setSelection(false);
		roles: for (String dn : roles) {
			if (systemRole.implied(dn, roleContext)) {
				radio.setSelection(true);
				break roles;
			}
		}

		if (systemRole.equals(CmsSystemRole.userAdmin)) {
			if (!CurrentUser.isUserContext(roleContext) && CurrentUser.implies(CmsSystemRole.userAdmin, roleContext)) {
				// a user admin cannot modify the user admins of their own context
				radio.setEnabled(true);
			} else {
				radio.setEnabled(false);
			}
		} else {
			radio.setEnabled(CurrentUser.implies(CmsSystemRole.userAdmin, roleContext));
		}

		radio.addSelectionListener((Selected) (e) -> {
			HierarchyUnit rolesHu = hierarchyUnit.getDirectChild(Type.ROLES);
			CmsGroup roleGroup = cmsUserManager.getOrCreateSystemRole(rolesHu, systemRole.qName());
			if (radio.getSelection())
				cmsUserManager.addMember(roleGroup, user);
			else
				cmsUserManager.removeMember(roleGroup, user);
		});

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
