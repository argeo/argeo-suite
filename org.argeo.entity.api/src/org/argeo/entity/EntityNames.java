package org.argeo.entity;

import org.argeo.naming.LdapAttrs;

/** Constants used to name entity structures. */
public interface EntityNames {
	final String ENTITY_DEFINITIONS_PATH = "/entity";
	final String TYPOLOGIES_PATH = "/class";
	final String FORM_BASE = "form";

	/** Administrative units. */
	final String ADM = "adm";

	final String ENTITY_TYPE = "entity:type";
	final String ENTITY_UID = "entity:uid";
	final String ENTITY_NAME = "entity:name";

	// GENERIC CONCEPTS
	/** The language which is relevant. */
	final String XML_LANG = "xml:lang";
	/** The date which is relevant. */
	final String ENTITY_DATE = "entity:date";
	final String ENTITY_RELATED_TO = "entity:relatedTo";

	// LDAP-LIKE ENTITIES
	final String DISPLAY_NAME = LdapAttrs.displayName.property();
	// Persons
	final String GIVEN_NAME = LdapAttrs.givenName.property();
	final String SURNAME = LdapAttrs.sn.property();
	final String EMAIL = LdapAttrs.mail.property();

	final String OU = LdapAttrs.ou.property();

	// WGS84
	final String GEO_LAT = "geo:lat";
	final String GEO_LONG = "geo:long";
	final String GEO_ALT = "geo:alt";
}
