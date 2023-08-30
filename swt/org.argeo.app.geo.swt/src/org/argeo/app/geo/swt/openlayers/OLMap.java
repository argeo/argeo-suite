package org.argeo.app.geo.swt.openlayers;

import java.util.concurrent.CompletableFuture;

import org.argeo.app.geo.ux.MapPart;
import org.argeo.cms.swt.CmsSwtUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

public class OLMap extends Composite implements MapPart {
	static final long serialVersionUID = 2713128477504858552L;
	private Browser browser;

	// private CompletableFuture<Boolean> renderCompleted = new
	// CompletableFuture<>();
	private CompletableFuture<Boolean> pageLoaded = new CompletableFuture<>();

	private String mapVar = "globalThis.argeoMap";

	public OLMap(Composite parent, int style) {
		super(parent, style);
		parent.setLayout(CmsSwtUtils.noSpaceGridLayout());
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		setLayout(CmsSwtUtils.noSpaceGridLayout());
		browser = new Browser(this, SWT.BORDER);
		browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		// functions exposed to JavaScript
//		new onRenderComplete();
		browser.setUrl("/pkg/org.argeo.app.geo.js/index.html");
		browser.addProgressListener(new ProgressListener() {
			static final long serialVersionUID = 1L;

			@Override
			public void completed(ProgressEvent event) {

				// create map
				browser.execute(mapVar + " = new globalThis.argeo.app.geo.ArgeoMap();");

				// browser.execute("console.log(myInstance.myField)");
				pageLoaded.complete(true);
			}

			@Override
			public void changed(ProgressEvent event) {
			}
		});
	}

	@Override
	public void addPoint(Double lng, Double lat) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addUrlLayer(String layer, Format format) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setZoom(int zoom) {
		pageLoaded.thenAccept((b) -> {
			browser.evaluate(mapVar + ".setZoom(" + zoom + ")");
		});
	}

	@Override
	public void setCenter(Double lng, Double lat) {
		pageLoaded.thenAccept((b) -> {
			browser.evaluate(mapVar + ".setCenter(" + lng + ", " + lat + ")");
		});

	}

//	private void setRenderCompleted() {
//		renderCompleted.complete(true);
//	}
//
//	private class onRenderComplete extends BrowserFunction {
//
//		onRenderComplete() {
//			super(browser, onRenderComplete.class.getSimpleName());
//		}
//
//		@Override
//		public Object function(Object[] arguments) {
//			setRenderCompleted();
//			System.out.println("Render complete (Java)");
//			return null;
//		}
//
//	}
}
