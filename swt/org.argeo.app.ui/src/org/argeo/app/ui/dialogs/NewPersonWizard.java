package org.argeo.app.ui.dialogs;

import static org.argeo.eclipse.ui.EclipseUiUtils.isEmpty;

import javax.jcr.Node;

import org.argeo.app.ui.SuiteUiUtils;
import org.argeo.app.ux.SuiteMsg;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/** Ask first & last name. Update the passed node on finish */
public class NewPersonWizard extends Wizard {
	// private final static Log log = LogFactory.getLog(NewPersonWizard.class);

	// Context
	private Node person;

	// This page widgets
	protected Text lastNameTxt;
	protected Text firstNameTxt;
	// private Button useDistinctDisplayNameBtn;
	// private Text displayNameTxt;

	public NewPersonWizard(Node person) {
		this.person = person;
	}

	@Override
	public void addPages() {
		try {
			MainInfoPage page = new MainInfoPage("Main page");
			addPage(page);
		} catch (Exception e) {
			throw new RuntimeException("Cannot add page to wizard", e);
		}
		setWindowTitle(SuiteMsg.personWizardWindowTitle.lead());
	}

	/**
	 * Called when the user click on 'Finish' in the wizard. The task is then
	 * created and the corresponding session saved.
	 */
	@Override
	public boolean performFinish() {
		String lastName = lastNameTxt.getText();
		String firstName = firstNameTxt.getText();
		// String displayName = displayNameTxt.getText();
		// boolean useDistinct = useDistinctDisplayNameBtn.getSelection();
		if (EclipseUiUtils.isEmpty(lastName) && EclipseUiUtils.isEmpty(firstName)) {
			MessageDialog.openError(getShell(), "Non-valid information",
					"Please enter at least a name that is not empty.");
			return false;
		} else {
//			ConnectJcrUtils.setJcrProperty(person, PEOPLE_LAST_NAME, PropertyType.STRING, lastName);
//			ConnectJcrUtils.setJcrProperty(person, PEOPLE_FIRST_NAME, PropertyType.STRING, firstName);
//			String fullName = firstName + " " + lastName;
//			ConnectJcrUtils.setJcrProperty(person, PEOPLE_DISPLAY_NAME, PropertyType.STRING, fullName);
			return true;
		}
	}

	@Override
	public boolean performCancel() {
		return true;
	}

	@Override
	public boolean canFinish() {
		String lastName = lastNameTxt.getText();
		String firstName = firstNameTxt.getText();
		if (isEmpty(lastName) && isEmpty(firstName)) {
			return false;
		} else
			return true;
	}

	protected class MainInfoPage extends WizardPage {
		private static final long serialVersionUID = 1L;

		public MainInfoPage(String pageName) {
			super(pageName);
			setTitle(SuiteMsg.personWizardPageTitle.lead());
			// setMessage("Please enter a last name and/or a first name.");
		}

		public void createControl(Composite parent) {
			parent.setLayout(new GridLayout(2, false));

			// FirstName
			SuiteUiUtils.createBoldLabel(parent, SuiteMsg.firstName);
			firstNameTxt = new Text(parent, SWT.BORDER);
			// firstNameTxt.setMessage("a first name");
			firstNameTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			// LastName
			SuiteUiUtils.createBoldLabel(parent, SuiteMsg.lastName);
			lastNameTxt = new Text(parent, SWT.BORDER);
			// lastNameTxt.setMessage("a last name");
			lastNameTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			// Display Name
			// useDistinctDisplayNameBtn = new Button(parent, SWT.CHECK);
			// useDistinctDisplayNameBtn.setText("Define a disting display name");
			// useDistinctDisplayNameBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
			// true, false, 2, 1));
			//
			// ConnectWorkbenchUtils.createBoldLabel(parent, "Display Name");
			// displayNameTxt = new Text(parent, SWT.BORDER);
			// displayNameTxt.setMessage("an optional display name");
			// displayNameTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
			// false));
			// displayNameTxt.setEnabled(false);
			//
			// useDistinctDisplayNameBtn.addSelectionListener(new SelectionAdapter() {
			// private static final long serialVersionUID = 1L;
			//
			// @Override
			// public void widgetSelected(SelectionEvent e) {
			// displayNameTxt.setEnabled(useDistinctDisplayNameBtn.getSelection());
			// }
			// });

			ModifyListener ml = new ModifyListener() {
				private static final long serialVersionUID = -1628130380128946886L;

				@Override
				public void modifyText(ModifyEvent event) {
					getContainer().updateButtons();
				}
			};

			firstNameTxt.addModifyListener(ml);
			lastNameTxt.addModifyListener(ml);
			// displayNameTxt.addModifyListener(ml);

			// Don't forget this.
			setControl(firstNameTxt);
			firstNameTxt.setFocus();
		}
	}
}
