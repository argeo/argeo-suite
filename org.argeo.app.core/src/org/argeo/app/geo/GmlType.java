package org.argeo.app.geo;

import org.argeo.api.acr.QNamed;

public enum GmlType implements QNamed {
	measure
	//
	;

	@Override
	public String getNamespace() {
		return "http://www.opengis.net/gml/3.2";
	}

	@Override
	public String getDefaultPrefix() {
		return "gml";
	}

}
