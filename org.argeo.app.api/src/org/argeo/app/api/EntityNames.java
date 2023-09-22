package org.argeo.app.api;

import org.argeo.api.acr.ldap.LdapAttr;

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

	@Deprecated
	final String ENTITY_TYPE = "entity:type";

	// GENERIC CONCEPTS
//	/** The language which is relevant. */
//	final String XML_LANG = "xml:lang";
	/** The date which is relevant. */
	@Deprecated
	final String ENTITY_DATE = "entity:date";
	@Deprecated
	final String ENTITY_RELATED_TO = "entity:relatedTo";

	// DEFAULT FOLDER NAMES
	final String MEDIA = "media";
	final String FILES = "files";

	// LDAP-LIKE ENTITIES
	@Deprecated
	final String DISPLAY_NAME = LdapAttr.displayName.property();
	// Persons
	@Deprecated
	final String GIVEN_NAME = LdapAttr.givenName.property();
	@Deprecated
	final String SURNAME = LdapAttr.sn.property();
	@Deprecated
	final String EMAIL = LdapAttr.mail.property();
	@Deprecated
	final String OU = LdapAttr.ou.property();

	// WGS84
	@Deprecated
	final String GEO_LAT = WGS84PosName.lat.get();
	@Deprecated
	final String GEO_LONG = WGS84PosName.lon.get();
	@Deprecated
	final String GEO_ALT = WGS84PosName.alt.get();

	// SVG
	@Deprecated
	final String SVG_WIDTH = "svg:width";
	@Deprecated
	final String SVG_HEIGHT = "svg:height";
	@Deprecated
	final String SVG_LENGTH = "svg:length";
	@Deprecated
	final String SVG_UNIT = "svg:unit";
	@Deprecated
	final String SVG_DUR = "svg:dur";
	@Deprecated
	final String SVG_DIRECTION = "svg:direction";
}
