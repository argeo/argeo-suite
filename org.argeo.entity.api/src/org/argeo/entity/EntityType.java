package org.argeo.entity;

/** Types related to entities. */
public enum EntityType implements JcrName {
	// entity
	entity, local,
	// typology
	typologies, terms, term,
	// form
	form, formSet, formSubmission,
	// graphics
	box,
	// ldap
	person, user;

	@Override
	public String getPrefix() {
		return prefix();
	}

	public static String prefix() {
		return "entity";
	}

	public String basePath() {
		return '/' + name();
	}

	@Override
	public String getNamespace() {
		return namespace();
	}

	public static String namespace() {
		return "http://www.argeo.org/ns/entity";
	}

}
