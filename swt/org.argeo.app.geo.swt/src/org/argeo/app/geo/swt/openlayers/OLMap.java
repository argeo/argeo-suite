package org.argeo.app.geo.swt.openlayers;

import org.argeo.app.geo.ux.MapPart;
import org.argeo.cms.swt.CmsSwtUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class OLMap extends Composite implements MapPart {
	static final long serialVersionUID = 2713128477504858552L;
	private Label div;

	public OLMap(Composite parent, int style) {
		super(parent, style);
		parent.setLayout(CmsSwtUtils.noSpaceGridLayout());
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		setLayout(CmsSwtUtils.noSpaceGridLayout());
//		div = new Label(this, SWT.NONE);
//		CmsSwtUtils.markup(div);
//		CmsSwtUtils.disableMarkupValidation(div);
//		div.setText(html);
//		div.setLayoutData(CmsSwtUtils.fillAll());
		String html = """
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8">
    <title>Simple Map</title>
    <link rel="stylesheet" href="/pkg/org.argeo.app.geo.ux.web/main.css">
    <style>
      .map {
        width: 100%;
        height: 100vh;
      }
    </style>
  </head>
  <body>
    <div id="map" class="map"></div>
    <script src="/pkg/org.argeo.app.geo.ux.web/main.bundle.js"></script>
  </body>
</html>				
				""";
		Browser browser = new Browser(this, SWT.BORDER);
		browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		//browser.setText(html);
		browser.setUrl("/pkg/org.argeo.app.geo.js/index.html");
		
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCenter(Double lng, Double lat) {
		// TODO Auto-generated method stub
		
	}

	
	
}
