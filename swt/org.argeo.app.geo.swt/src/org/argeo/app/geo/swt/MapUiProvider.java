package org.argeo.app.geo.swt;

import org.argeo.api.acr.Content;
import org.argeo.app.geo.swt.openlayers.OLMap;
import org.argeo.cms.swt.acr.SwtUiProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/** Create map parts. */
public class MapUiProvider implements SwtUiProvider {

	@Override
	public Control createUiPart(Composite parent, Content context) {
		OLMap map = new OLMap(parent, 0);
		return map;
	}

}
