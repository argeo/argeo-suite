package org.argeo.app.geo;

import org.argeo.api.acr.QNamed;

public enum GmlAttr implements QNamed {
	uom
	//
	;

	public final static String UOM_SQUARE_METERS = "m2";

	@Override
	public String getNamespace() {
		return "http://www.opengis.net/gml/3.2";
	}

	@Override
	public String getDefaultPrefix() {
		return "gml";
	}

}
