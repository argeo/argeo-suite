package org.argeo.app.api;

import org.argeo.api.acr.QNamed;

public enum EntityName implements QNamed {
	type, //
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
