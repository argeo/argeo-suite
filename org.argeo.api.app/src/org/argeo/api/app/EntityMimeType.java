package org.argeo.api.app;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/** Supported mime types. */
public enum EntityMimeType {
	XML("text/xml", "xml"), CSV("text/csv", "csv");

	private final String mimeType;
	private final String[] extensions;

	EntityMimeType(String mimeType, String... extensions) {
		this.mimeType = mimeType;
		this.extensions = extensions;
	}

	public String getMimeType() {
		return mimeType;
	}

	public String[] getExtensions() {
		return extensions;
	}

	public String getDefaultExtension() {
		if (extensions.length > 0)
			return extensions[0];
		else
			return null;
	}

	public String toHttpContentType(Charset charset) {
		if (charset == null)
			return mimeType;
		return mimeType + "; charset=" + charset.name();
	}

	public String toHttpContentType() {
		if (mimeType.startsWith("text/")) {
			return toHttpContentType(StandardCharsets.UTF_8);
		} else {
			return mimeType;
		}
	}

	public static EntityMimeType find(String mimeType) {
		for (EntityMimeType entityMimeType : values()) {
			if (entityMimeType.mimeType.equals(mimeType))
				return entityMimeType;
		}
		return null;
	}

	@Override
	public String toString() {
		return mimeType;
	}

}
