package org.argeo.app.swt.forms;

import org.argeo.api.cms.ux.CmsStyle;

/** Syles used */
public enum FormStyle implements CmsStyle {
	// Main
	form, title,
	// main part
	header, headerBtn, headerCombo, section, sectionHeader,
	// Property fields
	propertyLabel, propertyText, propertyMessage, errorMessage,
	// Date
	popupCalendar,
	// Buttons
	starred, unstarred, starOverlay, editOverlay, deleteOverlay, updateOverlay, deleteOverlaySmall, calendar, delete,
	// Contacts
	email, address, phone, website,
	// Social Media
	facebook, twitter, linkedIn, instagram;

	@Override
	public String getClassPrefix() {
		return "argeo-form";
	}

	// TODO clean button style management
	public final static String BUTTON_SUFFIX = "_btn";
}
