package org.argeo.app.ui.people;

import static org.argeo.eclipse.ui.EclipseUiUtils.isEmpty;

import org.argeo.api.acr.Content;
import org.argeo.api.cms.directory.CmsUserManager;
import org.argeo.app.ui.SuiteMsg;
import org.argeo.app.ui.SuiteUiUtils;
import org.argeo.cms.swt.widgets.SwtGuidedFormPage;
import org.argeo.cms.ux.widgets.AbstractGuidedForm;
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

	protected Text firstNameT;

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

	/**
	 * Called when the user click on 'Finish' in the wizard. The task is then
	 * created and the corresponding session saved.
	 */
	@Override
	public boolean performFinish() {
		return false;
	}

	@Override
	public boolean performCancel() {
		return true;
	}

	@Override
	public boolean canFinish() {
		String firstName = firstNameT.getText();
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
			firstNameT = new Text(parent, SWT.BORDER);
			// firstNameTxt.setMessage("a first name");
			firstNameT.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			ModifyListener ml = new ModifyListener() {
				private static final long serialVersionUID = 1939491923843870844L;

				@Override
				public void modifyText(ModifyEvent event) {
					getView().updateButtons();
				}
			};

			firstNameT.addModifyListener(ml);

			firstNameT.setFocus();
		}
	}
}
