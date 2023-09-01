package org.argeo.app.geo.swt;

import org.argeo.api.acr.Content;
import org.argeo.cms.swt.acr.SwtUiProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/** Create map parts. */
public class MapUiProvider implements SwtUiProvider {

	@Override
	public Control createUiPart(Composite parent, Content context) {
		SwtJSMapPart map = new SwtJSMapPart(parent, 0);
		map.setCenter(13.404954, 52.520008); // Berlin
//		map.setCenter(-74.00597, 40.71427); // NYC
//		map.addPoint(-74.00597, 40.71427, null);
		map.setZoom(6);
		// map.addUrlLayer("https://openlayers.org/en/v4.6.5/examples/data/geojson/countries.geojson",
		// Format.GEOJSON);
		return map;
	}

}
