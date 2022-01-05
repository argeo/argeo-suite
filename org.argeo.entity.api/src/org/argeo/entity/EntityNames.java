package org.argeo.entity;

import org.argeo.util.naming.LdapAttrs;

/** Constants used to name entity structures. */
public interface EntityNames {
	@Deprecated
	final String FORM_BASE = "form";
	final String SUBMISSIONS_BASE = "submissions";
	@Deprecated
	final String TERM = "term";
	final String NAME = "name";

//	final String ENTITY_DEFINITIONS_PATH = "/entity";
	@Deprecated
	final String TYPOLOGIES_PATH = "/" + TERM;
	/** Administrative units. */
	final String ADM = "adm";

	final String ENTITY_TYPE = "entity:type";
	// final String ENTITY_UID = "entity:uid";
	// final String ENTITY_NAME = "entity:name";

	// GENERIC CONCEPTS
	/** The language which is relevant. */
	final String XML_LANG = "xml:lang";
	/** The date which is relevant. */
	final String ENTITY_DATE = "entity:date";
	@Deprecated
	final String ENTITY_RELATED_TO = "entity:relatedTo";

	// DEFAULT FOLDER NAMES
	final String MEDIA = "media";
	final String FILES = "files";

	// LDAP-LIKE ENTITIES
	@Deprecated
	final String DISPLAY_NAME = LdapAttrs.displayName.property();
	// Persons
	@Deprecated
	final String GIVEN_NAME = LdapAttrs.givenName.property();
	@Deprecated
	final String SURNAME = LdapAttrs.sn.property();
	@Deprecated
	final String EMAIL = LdapAttrs.mail.property();
	@Deprecated
	final String OU = LdapAttrs.ou.property();

	// WGS84
	final String GEO_LAT = "geo:lat";
	final String GEO_LONG = "geo:long";
	final String GEO_ALT = "geo:alt";

	// SVG
	final String SVG_WIDTH = "svg:width";
	final String SVG_HEIGHT = "svg:height";
	final String SVG_LENGTH = "svg:length";
	final String SVG_UNIT = "svg:unit";
	final String SVG_DUR = "svg:dur";
	final String SVG_DIRECTION = "svg:direction";
}
