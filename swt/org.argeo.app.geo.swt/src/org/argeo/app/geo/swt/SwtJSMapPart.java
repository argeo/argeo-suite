package org.argeo.app.geo.swt;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;

import org.argeo.api.cms.CmsLog;
import org.argeo.app.geo.ux.JsImplementation;
import org.argeo.app.geo.ux.MapPart;
import org.argeo.cms.swt.CmsSwtUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

/**
 * An SWT implementation of {@link MapPart} based on JavaScript execute in a
 * {@link Browser} control.
 */
public class SwtJSMapPart extends Composite implements MapPart {
	static final long serialVersionUID = 2713128477504858552L;

	private final static CmsLog log = CmsLog.getLog(SwtJSMapPart.class);

	private final static String GLOBAL_THIS_ = "globalThis.";

	private final Browser browser;

	private final CompletableFuture<Boolean> pageLoaded = new CompletableFuture<>();

	private String jsImplementation = JsImplementation.OPENLAYERS_MAP_PART.getJsClass();
	private String mapVar = "argeoMap";

	public SwtJSMapPart(Composite parent, int style) {
		super(parent, style);
		parent.setLayout(CmsSwtUtils.noSpaceGridLayout());
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		setLayout(CmsSwtUtils.noSpaceGridLayout());

		browser = new Browser(this, SWT.BORDER);
		browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		browser.setUrl("/pkg/org.argeo.app.geo.js/index.html");
		browser.addProgressListener(new ProgressListener() {
			static final long serialVersionUID = 1L;

			@Override
			public void completed(ProgressEvent event) {
				try {
					// create map
					browser.execute(getJsMapVar() + " = new " + jsImplementation + "();");
					pageLoaded.complete(true);
				} catch (Exception e) {
					log.error("Cannot create map in browser", e);
					pageLoaded.complete(false);
				}
			}

			@Override
			public void changed(ProgressEvent event) {
			}
		});
	}

	/*
	 * MapPart.js METHODS
	 */

	@Override
	public void addPoint(double lng, double lat, String style) {
		callMapMethod("addPoint(%f, %f, %s)", lng, lat, style == null ? "'default'" : style);
	}

	@Override
	public void addUrlLayer(String url, GeoFormat format) {
		callMapMethod("addUrlLayer('%s', '%s')", url, format.name());
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

	protected CompletionStage<Object> callMethod(String jsObject, String methodCall, Object... args) {
		return evaluate(jsObject + '.' + methodCall, args);
	}

	private String getJsMapVar() {
		return GLOBAL_THIS_ + mapVar;
	}

	/**
	 * Execute this JavaScript on the client side after making sure that the page
	 * has been loaded and the map object has been created.
	 * 
	 * @param js   the JavaScript code, possibly formatted according to
	 *             {@link String#format}, with {@link Locale#ROOT} as locale (for
	 *             stability of decimal separator, as expected by JavaScript.
	 * @param args the optional arguments of
	 *             {@link String#format(String, Object...)}
	 */
	protected CompletionStage<Object> evaluate(String js, Object... args) {
		CompletableFuture<Object> res = pageLoaded.thenApply((ready) -> {
			if (!ready)
				throw new IllegalStateException("Map " + mapVar + " is not initialised.");
			Object result = browser.evaluate(String.format(Locale.ROOT, js, args));
			return result;
		});
		return res.minimalCompletionStage();
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
		pageLoaded.thenAccept((ready) -> {
			// browser functions must be directly on window (RAP specific)
			new BrowserFunction(browser, mapVar + "__on" + suffix) {

				@Override
				public Object function(Object[] arguments) {
					Object result = toDo.apply(arguments);
					return result;
				}

			};
			browser.execute(getJsMapVar() + ".callbacks['on" + suffix + "']=window." + mapVar + "__on" + suffix + ";");
			callMethod(mapVar, "enable" + suffix + "()");
		});
	}
}
