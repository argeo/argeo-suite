package org.argeo.app.swt.js;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import org.argeo.api.cms.CmsLog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * A part using a {@link Browser} and remote JavaScript components on the client
 * side.
 */
public class SwtBrowserJsPart {
	private final static CmsLog log = CmsLog.getLog(SwtBrowserJsPart.class);

	private final static String GLOBAL_THIS_ = "globalThis.";

	private final Browser browser;
	private final CompletableFuture<Boolean> pageLoaded = new CompletableFuture<>();

	public SwtBrowserJsPart(Composite parent, int style, String url) {
		this.browser = new Browser(parent, 0);
		if (parent.getLayout() instanceof GridLayout)
			browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		// TODO other layouts

		browser.setUrl(url);
		browser.addProgressListener(new ProgressListener() {
			static final long serialVersionUID = 1L;

			@Override
			public void completed(ProgressEvent event) {
				try {
					init();
					loadExtensions();
					pageLoaded.complete(true);
				} catch (Exception e) {
					log.error("Cannot initialise " + url + " in browser", e);
					pageLoaded.complete(false);
				}
			}

			@Override
			public void changed(ProgressEvent event) {
			}
		});
	}

	/*
	 * LIFECYCLE
	 */

	/** Called when the page has been loaded. */
	protected void init() {
	}

	/** To be overridden with calls to {@link #loadExtension(String)}. */
	protected void loadExtensions() {

	}

	protected void loadExtension(String url) {
//			String js = """
//					var script = document.createElement("script");
//					script.src = '%s';
//					document.head.appendChild(script);
//					""";
//			browser.evaluate(String.format(Locale.ROOT, js, url));
		browser.evaluate(String.format(Locale.ROOT, "import('%s')", url));
	}

	protected CompletionStage<Boolean> getReadyStage() {
		return pageLoaded.minimalCompletionStage();
	}

	/*
	 * JAVASCRIPT ACCESS
	 */

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
				throw new IllegalStateException("Component is not initialised.");
			Object result = browser.evaluate(String.format(Locale.ROOT, js, args));
			return result;
		});
		return res.minimalCompletionStage();
	}

	/** @return the globally usable function name. */
	protected String createJsFunction(String name, Function<Object[], Object> toDo) {
		// browser functions must be directly on window (RAP specific)
		new BrowserFunction(browser, name) {

			@Override
			public Object function(Object[] arguments) {
				Object result = toDo.apply(arguments);
				return result;
			}

		};
		return "window." + name;
	}

	/** Directly executes */
	protected void doExecute(String js, Object... args) {
		browser.execute(String.format(Locale.ROOT, js, args));
	}

	protected CompletionStage<Object> callMethod(String jsObject, String methodCall, Object... args) {
		return evaluate(jsObject + '.' + methodCall, args);
	}

	protected String getJsVarName(String name) {
		return GLOBAL_THIS_ + name;
	}

	/*
	 * ACCESSORS
	 */
	
	public Control getControl() {
		return browser;
	}

}
