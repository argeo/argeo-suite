package org.argeo.app.geo.swt;

import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;
import org.argeo.app.geo.GeoUtils;
import org.argeo.app.geo.ux.JsImplementation;
import org.argeo.app.geo.ux.MapPart;
import org.argeo.app.swt.js.SwtBrowserJsPart;
import org.eclipse.swt.widgets.Composite;

/**
 * An SWT implementation of {@link MapPart} based on JavaScript.
 */
public class SwtJsMapPart extends SwtBrowserJsPart implements MapPart {
	static final long serialVersionUID = 2713128477504858552L;

	private String jsImplementation = JsImplementation.OPENLAYERS_MAP_PART.getJsClass();
	private final String mapName;// = "argeoMap";

	public SwtJsMapPart(String mapName, Composite parent, int style) {
		super(parent, style, "/pkg/org.argeo.app.js/geo.html");
		this.mapName = mapName;
	}

	@Override
	protected void init() {
		// create map
		doExecute(getJsMapVar() + " = new " + jsImplementation + "('" + mapName + "');");
	}

	/*
	 * MapPart.js METHODS
	 */

	@Override
	public void addPoint(double lng, double lat, String style) {
		callMapMethod("addPoint(%f, %f, %s)", lng, lat, style == null ? "'default'" : style);
	}

	@Override
	public void addUrlLayer(String url, GeoFormat format, String style) {
		callMapMethod("addUrlLayer('%s', '%s', %s, false)", url, format.name(), style);
	}

	public void addCssUrlLayer(String url, GeoFormat format, String css) {
		String style = GeoUtils.createSldFromCss("layer", "Layer", css);
		callMapMethod("addUrlLayer('%s', '%s', '%s', true)", url, format.name(), style);
	}

	@Override
	public void setZoom(int zoom) {
		callMapMethod("setZoom(%d)", zoom);
	}

	@Override
	public void setCenter(double lng, double lat) {
		callMapMethod("setCenter(%f, %f)", lng, lat);
	}

	protected CompletionStage<Object> callMapMethod(String methodCall, Object... args) {
		return callMethod(getJsMapVar(), methodCall, args);
	}

	private String getJsMapVar() {
		return getJsVarName(mapName);
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
		getReadyStage().thenAccept((ready) -> {
			String functionName = createJsFunction(mapName + "__on" + suffix, toDo);
			doExecute(getJsMapVar() + ".callbacks['on" + suffix + "']=" + functionName + ";");
			callMethod(mapName, "enable" + suffix + "()");
		});
	}
}
