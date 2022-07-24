package org.argeo.app.ui;

import java.util.Map;

import org.argeo.api.cms.CmsEventSubscriber;
import org.argeo.api.cms.CmsLog;

/** Record UI events. */
public class EventRecorder implements CmsEventSubscriber {
	private final static CmsLog log = CmsLog.getLog(EventRecorder.class);

	public void init() {

	}

	public void destroy() {

	}

	@Override
	public void onEvent(String topic, Map<String, Object> properties) {
		if (log.isTraceEnabled())
			log.trace(topic + ": " + properties);

	}

}
