package org.argeo.app.odk;

import org.argeo.api.acr.QNamed;

/** Types related to the http://openrosa.org/xforms/xformsList namespace. */
public enum OrxManifestName implements QNamed {
	manifest, mediaFile;

	@Override
	public String getDefaultPrefix() {
		return "orxManifest";
	}

	@Override
	public String getNamespace() {
		return "http://openrosa.org/xforms/xformsManifest";
	}

}
