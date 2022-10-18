package org.argeo.app.ui;

import org.argeo.cms.Localized;

/** Localized messages. */
public enum SuiteMsg implements Localized {
	dashboard, people, documents, locations, recentItems,
	// NewPersonWizard
	firstName, lastName, salutation, email, personWizardWindowTitle, personWizardPageTitle,
	// NewOrgWizard
	orgWizardWindowTitle, orgWizardPageTitle, legalName, legalForm, vatId,
	// ContextAddressComposite
	chooseAnOrganisation, street, streetComplement, zipCode, city, state, country, geopoint,
	// FilteredOrderableEntityTable
	filterHelp,
	// BankAccountComposite
	accountHolder, bankName, currency, accountNumber, bankNumber, BIC, IBAN,
	// EditJobDialog
	position, chosenItem, department, isPrimary, searchAndChooseEntity,
	// ContactListCTab (e4)
	notes, addAContact, contactValue, linkedCompany,
	// OrgAdminInfoCTab (e4)
	paymentAccount,
	// OrgEditor (e4)
	orgDetails, orgActivityLog, team, orgAdmin,
	// PersonEditor (e4)
	personDetails, personActivityLog, personOrgs, personSecurity,
	// PersonSecurityCTab (e4)
	resetPassword,
	// Generic
	label, aCustomLabel, description, value, name, primary, add, save, pickup,
	// Tag
	confirmNewTag, cannotCreateTag,
	// Feddback messages
	allFieldsMustBeSet,
	//
	;
}
