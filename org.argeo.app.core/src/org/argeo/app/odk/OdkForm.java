package org.argeo.app.odk;

import java.io.InputStream;

/** Abstraction of a single ODK form. */
public interface OdkForm {
	String getFormId();

	String getName();

	String getVersion();

	String getDescription();

	String getHash(String hashType);

	String getFileName();

	InputStream openStream();
}
