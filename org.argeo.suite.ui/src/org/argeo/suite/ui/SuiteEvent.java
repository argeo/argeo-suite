package org.argeo.suite.ui;

/** Events specific to Argeo Suite. */
public enum SuiteEvent {
	switchLayer;

	public final static String LAYER_PARAM = "layer";

	String topic() {
		return getTopicBase() + "/" + name();
	}

	String getTopicBase() {
		return "argeo/suite/ui";
	}

}
