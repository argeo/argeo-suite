package org.argeo.app.ui.people;

import static org.argeo.eclipse.ui.EclipseUiUtils.isEmpty;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.argeo.api.acr.Content;
import org.argeo.api.acr.ldap.LdapAttr;
import org.argeo.api.acr.ldap.LdapObj;
import org.argeo.api.cms.directory.CmsUser;
import org.argeo.api.cms.directory.CmsUserManager;
import org.argeo.api.cms.directory.HierarchyUnit;
import org.argeo.app.core.SuiteUtils;
import org.argeo.app.swt.ux.SuiteSwtUtils;
import org.argeo.app.ux.SuiteMsg;
import org.argeo.cms.swt.dialogs.CmsFeedback;
import org.argeo.cms.swt.widgets.SwtGuidedFormPage;
import org.argeo.cms.ux.widgets.AbstractGuidedForm;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/** Ask first & last name. Update the passed node on finish */
public class NewUserForm extends AbstractGuidedForm {
	private Content hierarchyUnit;
	private CmsUserManager cmsUserManager;

	protected Text lastNameT;
	protected Text firstNameT;
	protected Text emailT;

	public NewUserForm(CmsUserManager cmsUserManager, Content hierarchyUnit) {
		this.hierarchyUnit = hierarchyUnit;
		if (!hierarchyUnit.hasContentClass(LdapObj.posixGroup.qName()))
			throw new IllegalArgumentException(hierarchyUnit + " is not a POSIX group");
		this.cmsUserManager = cmsUserManager;
	}

	@Override
	public void addPages() {
		try {
			MainInfoPage page = new MainInfoPage("main");
			addPage(page);
		} catch (Exception e) {
			throw new RuntimeException("Cannot add page to wizard", e);
		}
		setFormTitle(SuiteMsg.personWizardWindowTitle.lead());
	}

	/**
	 * Called when the user click on 'Finish' in the wizard. The task is then
	 * created and the corresponding session saved.
	 */
	@Override
	public boolean performFinish() {
		String lastName = lastNameT.getText();
		String firstName = firstNameT.getText();
		String email = emailT.getText();
		if (EclipseUiUtils.isEmpty(lastName) || EclipseUiUtils.isEmpty(firstName) || EclipseUiUtils.isEmpty(email)) {
			CmsFeedback.show(SuiteMsg.allFieldsMustBeSet.lead());
			return false;
		} else {
			UUID uuid = UUID.randomUUID();
			String shortId = uuid.toString().split("-")[0];
			String uid = "u" + shortId;
			HierarchyUnit hu = hierarchyUnit.adapt(HierarchyUnit.class);
			String username = "uid=" + uid + ",ou=People," + hu.getBase();

			Map<String, Object> properties = new HashMap<>();
			properties.put(LdapAttr.givenName.name(), firstName);
			properties.put(LdapAttr.sn.name(), lastName);
			properties.put(LdapAttr.mail.name(), email);
			properties.put(LdapAttr.cn.name(), firstName + " " + lastName);
			properties.put(LdapAttr.employeeNumber.name(), uuid.toString());

			Map<String, Object> credentials = new HashMap<>();
			CmsUser user = cmsUserManager.createUser(username, properties, credentials);

			Long huGidNumber = hierarchyUnit.get(LdapAttr.gidNumber.qName(), Long.class).orElseThrow();
			Long nextUserId = SuiteUtils.findNextId(hierarchyUnit, LdapObj.posixAccount.qName());
			String homeDirectory = "/home/" + uid;
			Map<String, Object> additionalProperties = new HashMap<>();
			additionalProperties.put(LdapAttr.uidNumber.name(), nextUserId.toString());
			additionalProperties.put(LdapAttr.gidNumber.name(), huGidNumber.toString());
			additionalProperties.put(LdapAttr.homeDirectory.name(), homeDirectory);

			Set<String> objectClasses = new HashSet<>();
			objectClasses.add(LdapObj.posixAccount.name());
			cmsUserManager.addObjectClasses(user, objectClasses, additionalProperties);
			return true;
		}
	}

	@Override
	public boolean performCancel() {
		return true;
	}

	@Override
	public boolean canFinish() {
		String lastName = lastNameT.getText();
		String firstName = firstNameT.getText();
		String email = emailT.getText();
		if (isEmpty(lastName) || isEmpty(firstName) || isEmpty(email)) {
			return false;
		} else
			return true;
	}

	protected class MainInfoPage extends SwtGuidedFormPage {

		public MainInfoPage(String pageName) {
			super(pageName);
			setTitle(SuiteMsg.personWizardPageTitle.lead());
		}

		public void createControl(Composite parent) {
			parent.setLayout(new GridLayout(2, false));

			// FirstName
			SuiteSwtUtils.createBoldLabel(parent, SuiteMsg.firstName);
			firstNameT = new Text(parent, SWT.BORDER);
			// firstNameTxt.setMessage("a first name");
			firstNameT.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			// LastName
			SuiteSwtUtils.createBoldLabel(parent, SuiteMsg.lastName);
			lastNameT = new Text(parent, SWT.BORDER);
			// lastNameTxt.setMessage("a last name");
			lastNameT.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			SuiteSwtUtils.createBoldLabel(parent, SuiteMsg.email);
			emailT = new Text(parent, SWT.BORDER);
			emailT.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			ModifyListener ml = new ModifyListener() {
				private static final long serialVersionUID = 1939491923843870844L;

				@Override
				public void modifyText(ModifyEvent event) {
					getView().updateButtons();
				}
			};

			firstNameT.addModifyListener(ml);
			lastNameT.addModifyListener(ml);
			emailT.addModifyListener(ml);

			firstNameT.setFocus();
		}
	}
}
