package org.argeo.app.geo.ux;

/** Known JavaScript implementations for this package. */
public enum JsImplementation {
	OPENLAYERS_MAP_PART("globalThis.argeo.app.geo.OpenLayersMapPart");

	private String jsClass;

	JsImplementation(String jsClass) {
		this.jsClass = jsClass;
	}

	public String getJsClass() {
		return jsClass;
	}
}
