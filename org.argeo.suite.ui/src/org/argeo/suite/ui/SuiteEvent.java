package org.argeo.suite.ui;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;

import org.argeo.cms.ui.util.CmsEvent;
import org.argeo.jcr.Jcr;

/** Events specific to Argeo Suite. */
public enum SuiteEvent implements CmsEvent {
	openNewPart, refreshPart, switchLayer;

	public final static String LAYER = "layer";
	public final static String NODE_ID = "nodeId";
	public final static String WORKSPACE = "workspace";

	public String getTopicBase() {
		return "argeo/suite/ui";
	}

	public static Map<String, Object> eventProperties(Node node) {
		Map<String, Object> properties = new HashMap<>();
		properties.put(NODE_ID, Jcr.getIdentifier(node));
		properties.put(WORKSPACE, Jcr.getWorkspaceName(node));
		return properties;
	}
}
