package org.argeo.app.ui.publish;

import org.argeo.api.cms.CmsStyle;

/** Publishing styles. */
public enum PublishingStyle implements CmsStyle {
	// general
	page, coverTitle, coverSubTitle, coverTagline, bannerLine1, bannerLine2,
	// meta data
	tag, menu,
	// text style
	title, subTitle, chapo, para, sectionTitle, subSectionTitle,
	// links
	internalLink,
	// composite style
	framed, line;

	@Override
	public String getClassPrefix() {
		return "argeo-publishing";
	}

}
