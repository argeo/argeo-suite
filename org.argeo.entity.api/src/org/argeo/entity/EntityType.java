package org.argeo.entity;

import org.argeo.naming.QualifiedName;

/** Types related to entities. */
public enum EntityType implements QualifiedName {
	// entity
	entity, definition,
	// xml
	xmlvalue, xmltext,
	// typology
	typology, term,
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
