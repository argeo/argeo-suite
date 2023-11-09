package org.argeo.app.geo.ux;

import org.argeo.app.ux.js.AbstractJsObject;

public class AbstractGeoJsObject extends AbstractJsObject {
	public final static String ARGEO_APP_GEO_JS_URL = "/pkg/org.argeo.app.js/geo.html";
	public final static String JS_PACKAGE = "argeo.app.geo";

	public AbstractGeoJsObject(Object... args) {
		super(args);
	}

	@Override
	public String getJsPackage() {
		return JS_PACKAGE;
	}
}
