package org.argeo.suite.ui;

import org.argeo.cms.ui.util.CmsStyle;

/** Styles used by Argeo Suite work UI. */
public enum SuiteStyle implements CmsStyle {
	// Header
	header,headerTitle,headerMenu,headerMenuItem,
	// Recent items
	recentItems,
	// Lead pane
	leadPane,leadPaneItem,
	// Groups composite
	titleLabel,subTitleLabel,
	// Forms elements
	simpleLabel, simpleText;

	@Override
	public String getClassPrefix() {
		return "argeo-suite";
	}

}
