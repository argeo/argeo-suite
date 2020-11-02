package org.argeo.publishing.ui;

import org.argeo.cms.ui.util.CmsStyle;

/** Publishing styles. */
public enum PublishingStyle implements CmsStyle {
	
	page,coverTitle,coverSubTitle,coverTagline,bannerLine1,bannerLine2,

	// text style
	title,subTitle,chapo,para,sectionTitle,subSectionTitle,
	
	// composite style
	framed,line;

	@Override
	public String getClassPrefix() {
		return "argeo-publishing";
	}

}
