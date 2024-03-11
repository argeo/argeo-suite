package org.argeo.api.app;

import org.argeo.api.acr.QNamed;

/**
 * Geographical coordinate in WGS84 reference datum.
 * 
 * @see https://www.w3.org/2003/01/geo/
 */
public enum WGS84PosName implements QNamed {
	lat, lon("long"), alt;

	private final String localName;

	private WGS84PosName() {
		localName = null;
	}

	private WGS84PosName(String localName) {
		this.localName = localName;
	}

	@Override
	public String getNamespace() {
		return "http://www.w3.org/2003/01/geo/wgs84_pos#";
	}

	@Override
	public String getDefaultPrefix() {
		return "geo";
	}

	@Override
	public String localName() {
		if (localName != null)
			return localName;
		return QNamed.super.localName();
	}

}
