package org.argeo.app.geo.swt;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.argeo.api.cms.CmsConstants;
import org.argeo.api.cms.CmsLog;
import org.argeo.api.cms.ux.CmsView;
import org.argeo.app.geo.ux.JsImplementation;
import org.argeo.app.geo.ux.MapPart;
import org.argeo.app.ux.SuiteUxEvent;
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
public class SwtJavaScriptMapPart extends Composite implements MapPart {
	static final long serialVersionUID = 2713128477504858552L;

	private final static CmsLog log = CmsLog.getLog(SwtJavaScriptMapPart.class);

	private Browser browser;

	private CompletableFuture<Boolean> pageLoaded = new CompletableFuture<>();

	private String jsImplementation = JsImplementation.OPENLAYERS_MAP_PART.getJsClass();
	private String mapVar = "globalThis.argeoMap";

	private final CmsView cmsView;

	public SwtJavaScriptMapPart(Composite parent, int style) {
		super(parent, style);
		parent.setLayout(CmsSwtUtils.noSpaceGridLayout());
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		setLayout(CmsSwtUtils.noSpaceGridLayout());

		cmsView = CmsSwtUtils.getCmsView(parent);

		browser = new Browser(this, SWT.BORDER);
		browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// functions exposed to JavaScript
		new onFeatureSelect();

		browser.setUrl("/pkg/org.argeo.app.geo.js/index.html");
		browser.addProgressListener(new ProgressListener() {
			static final long serialVersionUID = 1L;

			@Override
			public void completed(ProgressEvent event) {
				try {
					// create map
					browser.execute(mapVar + " = new " + jsImplementation + "();");
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

	@Override
	public void addPoint(double lng, double lat, String style) {
		callMethod(mapVar, "addPoint(%f, %f, %s)", lng, lat, style == null ? "'default'" : style);
	}

	@Override
	public void addUrlLayer(String url, GeoFormat format) {
		callMethod(mapVar, "addUrlLayer('%s', '%s')", url, format.name());
	}

	@Override
	public void setZoom(int zoom) {
		callMethod(mapVar, "setZoom(%d)", zoom);
	}

	@Override
	public void setCenter(double lng, double lat) {
		callMethod(mapVar, "setCenter(%f, %f)", lng, lat);
	}

	protected CompletionStage<Object> callMethod(String jsObject, String methodCall, Object... args) {
		return evaluate(jsObject + '.' + methodCall, args);
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

	/** JavaScript function called when a feature is selected on the map. */
	private class onFeatureSelect extends BrowserFunction {

		onFeatureSelect() {
			super(browser, onFeatureSelect.class.getSimpleName());
		}

		@Override
		public Object function(Object[] arguments) {
			if (arguments.length == 0)
				return null;
			String path = arguments[0].toString();
			Map<String, Object> properties = new HashMap<>();
			properties.put(SuiteUxEvent.CONTENT_PATH, '/' + CmsConstants.SYS_WORKSPACE + path);
			cmsView.sendEvent(SuiteUxEvent.refreshPart.topic(), properties);
			return null;
		}

	}
}
