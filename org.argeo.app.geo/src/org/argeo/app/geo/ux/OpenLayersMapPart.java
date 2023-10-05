package org.argeo.app.geo.ux;

import org.argeo.app.ol.AbstractOlObject;
import org.argeo.app.ol.Layer;
import org.argeo.app.ol.OlMap;
import org.argeo.app.ol.TileLayer;
import org.argeo.app.ol.VectorLayer;
import org.argeo.app.ux.js.JsClient;

/**
 * A wrapper around an OpenLayers map, adding specific features, such as SLD
 * styling.
 */
public class OpenLayersMapPart extends AbstractGeoJsObject {
	private final String mapPartName;

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

	public Layer getLayer(String name) {
		// TODO deal with not found
		String reference = getReference() + ".getLayerByName('" + name + "')";
		if (getJsClient().isInstanceOf(reference, AbstractOlObject.getJsClassName(VectorLayer.class))) {
			return new VectorLayer(getJsClient(), reference);
		} else if (getJsClient().isInstanceOf(reference, AbstractOlObject.getJsClassName(TileLayer.class))) {
			return new TileLayer(getJsClient(), reference);
		} else {
			return new Layer(getJsClient(), reference);
		}
	}

	public String getMapPartName() {
		return mapPartName;
	}

}
