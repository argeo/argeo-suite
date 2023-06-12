package org.argeo.app.ui.people;

import static org.argeo.eclipse.ui.EclipseUiUtils.isEmpty;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.argeo.api.acr.Content;
import org.argeo.api.acr.ldap.LdapAttr;
import org.argeo.api.acr.ldap.LdapObj;
import org.argeo.api.cms.directory.CmsGroup;
import org.argeo.api.cms.directory.CmsUserManager;
import org.argeo.api.cms.directory.HierarchyUnit;
import org.argeo.app.ui.SuiteUiUtils;
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

/** Form to create a new organisation. */
public class NewOrgForm extends AbstractGuidedForm {
	private Content hierarchyUnit;
	private CmsUserManager cmsUserManager;

	protected Text orgNameT;

	public NewOrgForm(CmsUserManager cmsUserManager, Content hierarchyUnit) {
		this.hierarchyUnit = hierarchyUnit;
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
		setFormTitle(SuiteMsg.orgWizardWindowTitle.lead());
	}

	@Override
	public boolean performFinish() {
		String orgName = orgNameT.getText();
		if (EclipseUiUtils.isEmpty(orgName)) {
			CmsFeedback.show(SuiteMsg.allFieldsMustBeSet.lead());
			return false;
		} else {
			HierarchyUnit hu = hierarchyUnit.adapt(HierarchyUnit.class);
			String dn = "cn=" + orgName + ",ou=Groups," + hu.getBase();

			CmsGroup user = cmsUserManager.createGroup(dn);

			Map<String, Object> additionalProperties = new HashMap<>();
			additionalProperties.put(LdapAttr.o.name(), orgName);

			Set<String> objectClasses = new HashSet<>();
			objectClasses.add(LdapObj.organization.name());
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
		String firstName = orgNameT.getText();
		if (isEmpty(firstName)) {
			return false;
		} else
			return true;
	}

	protected class MainInfoPage extends SwtGuidedFormPage {

		public MainInfoPage(String pageName) {
			super(pageName);
			setTitle(SuiteMsg.orgWizardPageTitle.lead());
		}

		public void createControl(Composite parent) {
			parent.setLayout(new GridLayout(2, false));

			// FirstName
			SuiteUiUtils.createBoldLabel(parent, SuiteMsg.org);
			orgNameT = new Text(parent, SWT.BORDER);
			// firstNameTxt.setMessage("a first name");
			orgNameT.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			ModifyListener ml = new ModifyListener() {
				private static final long serialVersionUID = 1939491923843870844L;

				@Override
				public void modifyText(ModifyEvent event) {
					getView().updateButtons();
				}
			};

			orgNameT.addModifyListener(ml);

			orgNameT.setFocus();
		}
	}
}
