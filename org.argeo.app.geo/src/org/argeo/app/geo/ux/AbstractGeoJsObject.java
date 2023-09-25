package org.argeo.app.geo.ux;

import org.argeo.app.ux.js.AbstractJsObject;

public class AbstractGeoJsObject extends AbstractJsObject {
	public AbstractGeoJsObject(Object... args) {
		super(args);
	}

	@Override
	public String getJsPackage() {
		return "argeo.app.geo";
	}

}
