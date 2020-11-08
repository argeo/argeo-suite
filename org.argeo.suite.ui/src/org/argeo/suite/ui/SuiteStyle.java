package org.argeo.suite.ui;

import org.argeo.cms.ui.util.CmsStyle;

/** Styles used by Argeo Suite work UI. */
public enum SuiteStyle implements CmsStyle {
	// Header
	header, headerTitle, headerMenu, headerMenuItem,
	// Recent items
	recentItems,
	// Lead pane
	leadPane, leadPaneItem,
	// Group composite
	titleContainer, titleLabel, subTitleLabel,formLine,formColumn,navigationBar,navigationTitle,navigationButton,
	// Forms elements
	simpleLabel, simpleText,
	// table
	titleCell,
	// tabbed area
	mainTabBody,mainTabSelected, mainTab,
	// Buttons
	inlineButton;

	@Override
	public String getClassPrefix() {
		return "argeo-suite";
	}

}
