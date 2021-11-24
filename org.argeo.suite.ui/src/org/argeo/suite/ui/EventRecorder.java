package org.argeo.suite.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/** Record UI events. */
public class EventRecorder implements EventHandler {
	private final static Log log = LogFactory.getLog(EventRecorder.class);

	public void init() {

	}

	public void destroy() {

	}

	@Override
	public void handleEvent(Event event) {
		if (log.isTraceEnabled())
			log.trace(event);

	}

}
