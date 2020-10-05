package org.argeo.suite.ui;

import org.argeo.cms.ui.util.CmsStyle;

/** Styles used by Argeo Suite work UI. */
public enum SuiteStyle implements CmsStyle {
	// Header
	header,headerTitle,
	// Lead pane
	leadPane;

	@Override
	public String getClassPrefix() {
		return "argeo-suite";
	}

}
