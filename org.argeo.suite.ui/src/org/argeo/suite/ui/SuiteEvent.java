package org.argeo.suite.ui;

import org.argeo.cms.ui.util.CmsEvent;

/** Events specific to Argeo Suite. */
public enum SuiteEvent implements CmsEvent {
	openNewPart, refreshPart, switchLayer;

	public final static String LAYER = "layer";
	public final static String NODE_ID = "nodeId";

	public String getTopicBase() {
		return "argeo/suite/ui";
	}

}
