package org.argeo.entity;

/** Types related to entities. */
public enum EntityType implements JcrName {
	// entity
	entity, definition,
	// typology
	typologies, terms, term,
	// form
	form, formSet,
	// ldap
	person;

	@Override
	public String getPrefix() {
		return prefix();
	}

	public static String prefix() {
		return "entity";
	}

	@Override
	public String getNamespace() {
		return namespace();
	}

	public static String namespace() {
		return "http://www.argeo.org/ns/entity";
	}

}