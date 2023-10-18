package org.argeo.app.geo.ux;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.argeo.app.ol.AbstractOlObject;
import org.argeo.app.ol.Layer;
import org.argeo.app.ol.OlMap;
import org.argeo.app.ol.TileLayer;
import org.argeo.app.ol.VectorLayer;
import org.argeo.app.ux.js.JsClient;
import org.locationtech.jts.geom.Envelope;

/**
 * A wrapper around an OpenLayers map, adding specific features, such as SLD
 * styling.
 */
public class OpenLayersMapPart extends AbstractGeoJsObject implements MapPart {
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

	public void setCenter(double lat, double lon) {
		executeMethod(getMethodName(), lat, lon);
	}

	public void fit(double[] extent, Map<String, Object> options) {
		executeMethod(getMethodName(), extent, options);
	}

	public void fit(Envelope extent, Map<String, Object> options) {
		fit(new double[] { extent.getMinX(), extent.getMinY(), extent.getMaxX(), extent.getMaxY() }, options);
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

	public void selectFeatures(String layerName, Object... ids) {
		executeMethod(getMethodName(), layerName, (Object[]) ids);
	}

	public void fitToLayer(String layerName) {
		executeMethod(getMethodName(), layerName);
	}

	/*
	 * CALLBACKS
	 */
	public void onFeatureSelected(Consumer<FeatureSelectedEvent> toDo) {
		addCallback("FeatureSelected", (arr) -> {
			toDo.accept(new FeatureSelectedEvent((String) arr[0]));
			return null;
		});
	}

	public void onFeatureSingleClick(Consumer<FeatureSingleClickEvent> toDo) {
		addCallback("FeatureSingleClick", (arr) -> {
			toDo.accept(new FeatureSingleClickEvent((String) arr[0]));
			return null;
		});
	}

	public void onFeaturePopup(Function<FeaturePopupEvent, String> toDo) {
		addCallback("FeaturePopup", (arr) -> {
			return toDo.apply(new FeaturePopupEvent((String) arr[0]));
		});
	}

	protected void addCallback(String suffix, Function<Object[], Object> toDo) {
		getJsClient().getReadyStage().thenAccept((ready) -> {
			String functionName = getJsClient().createJsFunction(getMapPartName() + "__on" + suffix, toDo);
			getJsClient().execute(getJsReference() + ".callbacks['on" + suffix + "']=" + functionName + ";");
			executeMethod("enable" + suffix);
		});
	}

}
