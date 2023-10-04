package org.argeo.app.geo.ux;

import org.argeo.app.ol.AbstractOlObject;
import org.argeo.app.ol.Layer;
import org.argeo.app.ol.OlMap;
import org.argeo.app.ol.TileLayer;
import org.argeo.app.ol.VectorLayer;
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

	public void setSld(String xml) {
		executeMethod(getMethodName(), JsClient.escapeQuotes(xml));
	}

	public void applyStyle(String layerName, String styledLayerName) {
		executeMethod(getMethodName(), layerName, styledLayerName);
	}

	public void applyBboxStrategy(String layerName) {
		executeMethod(getMethodName(), layerName);
	}

	public Layer getLayer(String name) {
		// TODO deal with not found
		String reference = "getLayerByName('" + name + "')";
		if (getJsClient().isInstanceOf(reference, AbstractOlObject.getJsClassName(VectorLayer.class))) {
			return new VectorLayer(getJsClient(), reference);
		} else if (getJsClient().isInstanceOf(reference, AbstractOlObject.getJsClassName(TileLayer.class))) {
			return new TileLayer(getJsClient(), reference);
		} else {
			return new Layer(getJsClient(), reference);
		}
	}
}
