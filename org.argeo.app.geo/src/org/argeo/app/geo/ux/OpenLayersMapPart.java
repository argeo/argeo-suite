package org.argeo.app.geo.ux;

import org.argeo.app.ol.OlMap;
import org.argeo.app.ux.js.AbstractJsObject;
import org.argeo.app.ux.js.JsClient;

public class OpenLayersMapPart extends AbstractGeoJsObject {
	private String mapPartName;

	public OpenLayersMapPart(JsClient jsClient, String mapPartName) {
		super(mapPartName);
		this.mapPartName = mapPartName;
		create(jsClient, mapPartName);
	}

	public OlMap getMap() {
		return new OlMap(getJsClient(), getReference() + ".getMap()");
	}
}
