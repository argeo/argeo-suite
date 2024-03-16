package org.argeo.api.app;

import org.argeo.api.acr.QNamed;

/** Types used in the entity namespace http://www.argeo.org/ns/entity. */
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
	geopoint, bearing, geobounded,
	// ldap
	person, user;

	public final static String ENTITY_NAMESPACE_URI = "http://www.argeo.org/ns/entity";
	public final static String ENTITY_DEFAULT_PREFIX = "entity";

	@Override
	public String getDefaultPrefix() {
		return ENTITY_DEFAULT_PREFIX;
	}

	public String basePath() {
		return '/' + name();
	}

	@Override
	public String getNamespace() {
		return ENTITY_NAMESPACE_URI;
	}
}
