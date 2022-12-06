package org.argeo.app.api;

import org.argeo.api.acr.QNamed;

/** Types related to entities. */
public enum EntityType implements QNamed {
	// entity
	entity, local, relatedTo,
	// structure
	space, document,
	// typology
	typologies, terms, term,
	// form
	form, formSet, formSubmission,
	// graphics
	box,
	// geography
	geopoint, bearing,
	// ldap
	person, user;

	@Override
	public String getDefaultPrefix() {
		return "entity";
	}

//	@Override
//	public String getPrefix() {
//		return getDefaultPrefix();
//	}
//
//	public static String prefix() {
//		return "entity";
//	}

	public String basePath() {
		return '/' + name();
	}

	@Override
	public String getNamespace() {
		return "http://www.argeo.org/ns/entity";
	}

//	public static String namespace() {
//		return "http://www.argeo.org/ns/entity";
//	}

}
