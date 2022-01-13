package org.argeo.app.odk;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.argeo.util.DigestUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/** {@link OdkForm} implementation based on an OSGi {@link Bundle} resource. */
public class BundleResourceOdkForm implements OdkForm {
	private String formId;
	private String name;
	private String version;
	private String description;
	private String hash;
	private String fileName;

	private byte[] data;

	public void init(Map<String, String> properties, BundleContext bundleContext) throws IOException {
		String location = properties.get("location");
		fileName = FilenameUtils.getName(location);
		URL url = bundleContext.getBundle().getResource(location);
		data = IOUtils.toByteArray(url.openStream());
		hash = "md5:" + DigestUtils.digest(DigestUtils.MD5, data);

		// TODO get it from the XML
		formId = properties.get("formId");
		version = properties.get("version");

		name = properties.get("name");
		description = properties.get("description");
	}

	@Override
	public String getFormId() {
		return formId;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getVersion() {
		return version;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getHash(String hashType) {
		return hash;
	}

	@Override
	public String getFileName() {
		return fileName;
	}

	@Override
	public InputStream openStream() {
		return new ByteArrayInputStream(data);
	}

	@Override
	public int hashCode() {
		assert formId != null;
		assert version != null;
		return formId.hashCode() + version.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		assert formId != null;
		assert version != null;
		if (!(obj instanceof OdkForm))
			return false;
		OdkForm other = (OdkForm) obj;
		assert other.getFormId() != null;
		assert other.getVersion() != null;

		return other.getFormId().equals(formId) && other.getVersion().equals(version);
	}

	@Override
	public String toString() {
		return "ODK Form " + formId + ", v" + version;
	}

}
