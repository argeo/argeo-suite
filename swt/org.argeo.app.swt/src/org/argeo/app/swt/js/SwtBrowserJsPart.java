package org.argeo.app.swt.js;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.function.Supplier;

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
import org.eclipse.swt.widgets.Display;

/**
 * A part using a {@link Browser} and remote JavaScript components on the client
 * side.
 */
public class SwtBrowserJsPart {
	private final static CmsLog log = CmsLog.getLog(SwtBrowserJsPart.class);

	private final static String GLOBAL_THIS_ = "globalThis.";

	private final Browser browser;
	private final CompletableFuture<Boolean> readyStage = new CompletableFuture<>();

	/**
	 * Tasks that were requested before the context was ready. Typically
	 * configuration methods on the part while the user interfaces is being build.
	 */
	private List<Supplier<Boolean>> preReadyToDos = new ArrayList<>();

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
					// execute todos in order
					for (Supplier<Boolean> toDo : preReadyToDos) {
						boolean success = toDo.get();
						if (!success)
							throw new IllegalStateException("Post-initalisation JavaScript execution failed");
					}
					preReadyToDos.clear();
					readyStage.complete(true);
				} catch (Exception e) {
					log.error("Cannot initialise " + url + " in browser", e);
					readyStage.complete(false);
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

	/**
	 * Called when the page has been loaded, typically in order to initialise
	 * JavaScript objects. One MUST use {@link #doExecute(String, Object...)} in
	 * order to do so, since the context is not yet considered ready and calls to
	 * {@link #evaluate(String, Object...)} will block.
	 */
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
		return readyStage.minimalCompletionStage();
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
	protected Object evaluate(String js, Object... args) {
		assert browser.getDisplay().equals(Display.findDisplay(Thread.currentThread())) : "Not the proper UI thread.";
		if (!readyStage.isDone())
			throw new IllegalStateException("Methods returning a result can only be called after UI initilaisation.");
		// wait for the context to be ready
//		boolean ready = readyStage.join();
//		if (!ready)
//			throw new IllegalStateException("Component is not initialised.");
		Object result = browser.evaluate(String.format(Locale.ROOT, js, args));
		return result;
	}

	protected void execute(String js, Object... args) {
		if (readyStage.isDone()) {
			boolean success = browser.execute(String.format(Locale.ROOT, js, args));
			if (!success)
				throw new RuntimeException("JavaScript execution failed.");
		} else {
			Supplier<Boolean> toDo = () -> {
				boolean success = browser.execute(String.format(Locale.ROOT, js, args));
				return success;
			};
			preReadyToDos.add(toDo);
		}
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

	/**
	 * Directly executes, even if {@link #getReadyStage()} is not completed. Except
	 * in initialisation, {@link #evaluate(String, Object...)} should be used
	 * instead.
	 */
	protected void doExecute(String js, Object... args) {
		browser.execute(String.format(Locale.ROOT, js, args));
	}

	protected Object callMethod(String jsObject, String methodCall, Object... args) {
		return evaluate(jsObject + '.' + methodCall, args);
	}

	protected void executeMethod(String jsObject, String methodCall, Object... args) {
		execute(jsObject + '.' + methodCall, args);
	}

	protected String getJsVarName(String name) {
		return GLOBAL_THIS_ + name;
	}

	protected static String toJsArray(int... arr) {
		return Arrays.toString(arr);
	}

	protected static String toJsArray(long... arr) {
		return Arrays.toString(arr);
	}

	protected static String toJsArray(double... arr) {
		return Arrays.toString(arr);
	}

	protected static String toJsArray(String... arr) {
		return toJsArray((Object[]) arr);
	}

	protected static String toJsArray(Object... arr) {
		StringJoiner sj = new StringJoiner(",", "[", "]");
		for (Object o : arr) {
			sj.add(toJsValue(o));
		}
		return sj.toString();
	}

	protected static String toJsValue(Object o) {
		if (o instanceof CharSequence)
			return '\"' + o.toString() + '\"';
		else if (o instanceof Number)
			return o.toString();
		else if (o instanceof Boolean)
			return o.toString();
		else
			return '\"' + o.toString() + '\"';
	}

	/*
	 * ACCESSORS
	 */

	public Control getControl() {
		return browser;
	}

}
