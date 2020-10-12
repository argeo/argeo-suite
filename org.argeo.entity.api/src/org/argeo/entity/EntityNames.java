package org.argeo.entity;

import org.argeo.naming.LdapAttrs;

/** Constants used to name entity structures. */
public interface EntityNames {
	final String ENTITY_DEFINITIONS_PATH = "/entity:entityDefinitions";

	final String ENTITY_TYPE = "entity:type";
	final String ENTITY_UID = "entity:uid";

	// GENERIC CONCEPTS
	/** The date which is clearly relevant for this entity. */
	final String ENTITY_DATE = "entity:date";
	final String ENTITY_RELATED_TO = "entity:relatedTo";

	// LDAP-LIKE ENTITIES
	final String DISPLAY_NAME = LdapAttrs.displayName.property();
	// Persons
	final String GIVEN_NAME = LdapAttrs.givenName.property();
	final String SURNAME = LdapAttrs.sn.property();
	final String EMAIL = LdapAttrs.mail.property();

}
