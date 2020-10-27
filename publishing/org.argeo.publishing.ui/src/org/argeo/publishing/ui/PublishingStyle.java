package org.argeo.publishing.ui;

import org.argeo.cms.ui.util.CmsStyle;

/** Publishing styles. */
public enum PublishingStyle implements CmsStyle {
	page;

	@Override
	public String getClassPrefix() {
		return "argeo-publishing";
	}

}
