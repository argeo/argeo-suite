package org.argeo.app.ux;

import java.util.HashMap;
import java.util.Map;

import org.argeo.api.acr.Content;
import org.argeo.api.cms.CmsEvent;

/** Events specific to Argeo Suite UX. */
public enum SuiteUxEvent implements CmsEvent {
	openNewPart, refreshPart, switchLayer;

	public final static String LAYER = "layer";
	public final static String USERNAME = "username";

	// ACR
	public final static String CONTENT_PATH = "contentPath";

	public String getTopicBase() {
		return "argeo.suite.ui";
	}

	public static Map<String, Object> eventProperties(Content content) {
		Map<String, Object> properties = new HashMap<>();
		properties.put(CONTENT_PATH, content.getPath());
		return properties;
	}

//	public static Map<String, Object> eventProperties(User user) {
//		Map<String, Object> properties = new HashMap<>();
//		properties.put(USERNAME, user.getName());
//		return properties;
//	}
}
