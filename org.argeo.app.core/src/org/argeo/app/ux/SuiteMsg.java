package org.argeo.app.ux;

import org.argeo.cms.Localized;

/** Localized messages. */
public enum SuiteMsg implements Localized {
	// Entities
	user, org, person, group,
	// UI parts
	dashboard, people, documents, locations, recentItems,
	// NewPersonWizard
	firstName, lastName, salutation, email, personWizardWindowTitle, personWizardPageTitle, personWizardFeedback,
	// NewOrgWizard
	orgWizardWindowTitle, orgWizardPageTitle, orgWizardFeedback, legalName, legalForm, vatId,
	// Roles
	userAdminRole, groupAdminRole, publisherRole, coworkerRole,
	// Group
	chooseAMember,
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
	// Feedback messages
	allFieldsMustBeSet,
	//
	;
}
