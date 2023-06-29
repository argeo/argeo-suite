package org.argeo.app.ux;

import org.argeo.api.cms.ux.CmsStyle;

/** Styles used by Argeo Suite work UI. */
public enum SuiteStyle implements CmsStyle {
	// header
	header, headerTitle, headerMenu, headerMenuItem,
	// footer
	footer,
	// recent items
	recentItems,
	// lead pane
	leadPane, leadPaneItem, leadPaneSectionTitle, leadPaneSubSectionTitle,
	// entry area
	entryArea,
	// group composite
	titleContainer, titleLabel, subTitleLabel, formLine, formColumn, navigationBar, navigationTitle, navigationButton,
	// forms elements
	simpleLabel, simpleText, simpleInput,
	// table
	titleCell,
	// layers
	workArea,
	// tabbed area
	mainTabBody, mainTabSelected, mainTab,
	// buttons
	inlineButton;

	@Override
	public String getClassPrefix() {
		return "argeo-suite";
	}

}
