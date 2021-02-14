package org.argeo.suite.ui;

import org.argeo.cms.ui.util.CmsStyle;

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
