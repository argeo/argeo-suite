package org.argeo.app.geo.ux;

import org.argeo.app.ol.Source;

public class SentinelCloudless extends Source {

	public SentinelCloudless(Object... args) {
		super(args);
	}

	@Override
	public String getJsPackage() {
		return "argeo.app.geo";
	}

}
