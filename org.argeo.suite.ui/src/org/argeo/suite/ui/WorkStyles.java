package org.argeo.suite.ui;

import org.argeo.cms.ui.util.CmsStyle;

/** Styles used by Argeo Suite work UI. */
public enum WorkStyles implements CmsStyle {
	header, leadPane;

	@Override
	public String getClassPrefix() {
		return "argeo-work";
	}

}
