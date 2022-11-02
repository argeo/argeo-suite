package org.argeo.app.ui;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;

import org.argeo.api.acr.Content;
import org.argeo.api.cms.CmsEvent;
import org.argeo.jcr.Jcr;
import org.osgi.service.useradmin.User;

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

	@Deprecated
	public static Map<String, Object> eventProperties(Node node) {
		Map<String, Object> properties = new HashMap<>();
		String contentPath = '/' + Jcr.getWorkspaceName(node) + Jcr.getPath(node);
		properties.put(CONTENT_PATH, contentPath);
		return properties;
	}

	public static Map<String, Object> eventProperties(User user) {
		Map<String, Object> properties = new HashMap<>();
		properties.put(USERNAME, user.getName());
		return properties;
	}
}