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
