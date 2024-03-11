package org.argeo.api.app;

import org.argeo.api.acr.QNamed;

/** Names used in the entity namespace http://www.argeo.org/ns/entity. */
public enum EntityName implements QNamed {
	type, relatedTo, //
	// time,
	date,
	// geography
	minLat, minLon, maxLat, maxLon,
	// geo entities
	place,
	//
	;

	@Override
	public String getDefaultPrefix() {
		return "entity";
	}

	public String basePath() {
		return '/' + name();
	}

	@Override
	public String getNamespace() {
		return "http://www.argeo.org/ns/entity";
	}

}
