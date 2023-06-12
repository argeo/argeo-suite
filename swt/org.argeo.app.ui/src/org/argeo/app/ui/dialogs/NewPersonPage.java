package org.argeo.app.ui.dialogs;

import org.argeo.app.ui.SuiteUiUtils;
import org.argeo.app.ux.SuiteMsg;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class NewPersonPage extends WizardPage {
	private static final long serialVersionUID = -944349994177526468L;
	protected Text lastNameTxt;
	protected Text firstNameTxt;
	protected Text emailTxt;

	protected NewPersonPage(String pageName) {
		super(pageName);
		setTitle(SuiteMsg.personWizardPageTitle.lead());
	}

	@Override
	public void createControl(Composite parent) {
		parent.setLayout(new GridLayout(2, false));

		// FirstName
		SuiteUiUtils.createBoldLabel(parent, SuiteMsg.firstName);
		firstNameTxt = new Text(parent, SWT.BORDER);
		firstNameTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		// LastName
		SuiteUiUtils.createBoldLabel(parent, SuiteMsg.lastName);
		lastNameTxt = new Text(parent, SWT.BORDER);
		lastNameTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		SuiteUiUtils.createBoldLabel(parent, SuiteMsg.email);
		emailTxt = new Text(parent, SWT.BORDER);
		emailTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		ModifyListener ml = new ModifyListener() {
			private static final long serialVersionUID = -1628130380128946886L;

			@Override
			public void modifyText(ModifyEvent event) {
				getContainer().updateButtons();
			}
		};

		firstNameTxt.addModifyListener(ml);
		lastNameTxt.addModifyListener(ml);
		emailTxt.addModifyListener(ml);

		// Don't forget this.
		setControl(firstNameTxt);
		firstNameTxt.setFocus();

	}

//	public void updateNode(Node node, PeopleService peopleService, ResourcesService resourcesService) {
//		ConnectJcrUtils.setJcrProperty(node, PeopleNames.PEOPLE_LAST_NAME, PropertyType.STRING, lastNameTxt.getText());
//		ConnectJcrUtils.setJcrProperty(node, PeopleNames.PEOPLE_FIRST_NAME, PropertyType.STRING,
//				firstNameTxt.getText());
//		ConnectJcrUtils.setJcrProperty(node, PeopleNames.PEOPLE_DISPLAY_NAME, PropertyType.STRING,
//				firstNameTxt.getText() + " " + lastNameTxt.getText());
//		String email = emailTxt.getText();
//		ConnectJcrUtils.setJcrProperty(node, PeopleNames.PEOPLE_PRIMARY_EMAIL, PropertyType.STRING, email);
//		PeopleJcrUtils.createEmail(resourcesService, peopleService, node, email, true, null, null);
//	}
}
