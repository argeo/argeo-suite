package org.argeo.app.core;

import static java.lang.System.Logger.Level.ERROR;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Objects;

import org.argeo.api.acr.spi.ContentNamespace;
import org.argeo.cms.acr.CmsContentNamespace;

public enum SuiteContentNamespace implements ContentNamespace {
	//
	// ARGEO
	//
	ENTITY("entity", "http://www.argeo.org/ns/entity",
			"platform:/plugin/org.argeo.app.api/org/argeo/api/app/entity.xsd", null),
	//
	ARGEO_DBK("argeodbk", "http://www.argeo.org/ns/argeodbk", null, null),
	//
	// EXTERNAL
	//
	DOCBOOK5("dbk", "http://docbook.org/ns/docbook", "docbook.xsd", "http://docbook.org/xml/5.0.1/xsd/docbook.xsd"),
	//
	XML_EVENTS("ev", "http://www.w3.org/2001/xml-events", "xml-events-attribs-1.xsd",
			"http://www.w3.org/MarkUp/SCHEMA/xml-events-attribs-1.xsd"),
	//
	XFORMS("xforms", "http://www.w3.org/2002/xforms", "XForms-11-Schema.xsd",
			"https://www.w3.org/MarkUp/Forms/2007/XForms-11-Schema.xsd"),
	//
	XCARD("xcard", "urn:ietf:params:xml:ns:vcard-4.0", "xCard-4.0.xsd", null),
	//
	XSL_FO("fo", "http://www.w3.org/1999/XSL/Format", "fop.xsd",
			"https://svn.apache.org/repos/asf/xmlgraphics/fop/trunk/fop/src/foschema/fop.xsd"),
	//
//	XCAL_2_0("xcal", "urn:ietf:params:xml:ns:icalendar-2.0", "xCal-2.0.xsd", null),
	//
	XHTML("h", "http://www.w3.org/1999/xhtml", null, "https://www.w3.org/MarkUp/SCHEMA/xhtml11.xsd"),
	//
	// ODK
	//
	JR("jr", "http://openrosa.org/javarosa", null, null),
	//
	ORX("orx", "http://openrosa.org/xforms", null, null),
	//
	ORX_LIST("orxList", "http://openrosa.org/xforms/xformsList", null, null),
	//
	ORX_MANIFEST("orxManifest", "http://openrosa.org/xforms/xformsManifest", null, null),
	//
	ODK("odk", "http://www.opendatakit.org/xforms", null, null),
	//
	WGS84("geo", "http://www.w3.org/2003/01/geo/wgs84_pos#", null, null),
	// Re-add XML in order to solve import issue with xlink
	XML("xml", "http://www.w3.org/XML/1998/namespace", "xml.xsd", "http://www.w3.org/2001/xml.xsd"),
	//

	;

	private final static String RESOURCE_BASE = "/org/argeo/app/core/schemas/";

	private String defaultPrefix;
	private String namespace;
	private URL resource;
	private URL publicUrl;

	SuiteContentNamespace(String defaultPrefix, String namespace, String resourceFileName, String publicUrl) {
		Objects.requireNonNull(namespace);
		this.defaultPrefix = defaultPrefix;
		Objects.requireNonNull(namespace);
		this.namespace = namespace;
		if (resourceFileName != null) {
			try {
				// FIXME workaround when in nested OSGi frameworks
				// we should use class path, as before
				if (!resourceFileName.startsWith("platform:")) {
					resource = URI.create("platform:/plugin/org.argeo.app.core" + RESOURCE_BASE + resourceFileName)
							.toURL();
				} else {
					resource = URI.create(resourceFileName).toURL();
				}
			} catch (MalformedURLException e) {
				resource = null;
				System.getLogger(CmsContentNamespace.class.getName()).log(ERROR,
						"Cannot load " + resourceFileName + ": " + e.getMessage());
				// throw new IllegalArgumentException("Cannot convert " + resourceFileName + "
				// to URL");
			}
			// Objects.requireNonNull(resource);
		}
		if (publicUrl != null)
			try {
				this.publicUrl = new URL(publicUrl);
			} catch (MalformedURLException e) {
				throw new IllegalArgumentException("Cannot interpret public URL", e);
			}
	}

	@Override
	public String getDefaultPrefix() {
		return defaultPrefix;
	}

	@Override
	public String getNamespaceURI() {
		return namespace;
	}

	@Override
	public URL getSchemaResource() {
		return resource;
	}

	public URL getPublicUrl() {
		return publicUrl;
	}

}
