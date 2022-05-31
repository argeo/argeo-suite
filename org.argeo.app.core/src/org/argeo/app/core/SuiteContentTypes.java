package org.argeo.app.core;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

public enum SuiteContentTypes {
	//
	XCARD_4_0("xcard", "urn:ietf:params:xml:ns:vcard-4.0", "xCard-4.0.xsd", null),
	//
	XSL_FO_1999("fo", "http://www.w3.org/1999/XSL/Format", "fop.xsd",
			"https://svn.apache.org/repos/asf/xmlgraphics/fop/trunk/fop/src/foschema/fop.xsd"),
	//
//	XCAL_2_0("xcal", "urn:ietf:params:xml:ns:icalendar-2.0", "xCal-2.0.xsd", null),
	//
	;

	private final static String RESOURCE_BASE = "/org/argeo/app/core/schemas/";

	private String defaultPrefix;
	private String namespace;
	private URL resource;
	private URL publicUrl;

	SuiteContentTypes(String defaultPrefix, String namespace, String resourceFileName, String publicUrl) {
		Objects.requireNonNull(namespace);
		this.defaultPrefix = defaultPrefix;
		Objects.requireNonNull(namespace);
		this.namespace = namespace;
		resource = getClass().getResource(RESOURCE_BASE + resourceFileName);
		Objects.requireNonNull(resource);
		if (publicUrl != null)
			try {
				this.publicUrl = new URL(publicUrl);
			} catch (MalformedURLException e) {
				throw new IllegalArgumentException("Cannot interpret public URL", e);
			}
	}

	public String getDefaultPrefix() {
		return defaultPrefix;
	}

	public String getNamespace() {
		return namespace;
	}

	public URL getResource() {
		return resource;
	}

	public URL getPublicUrl() {
		return publicUrl;
	}

}
