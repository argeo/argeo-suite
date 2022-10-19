package org.argeo.app.odk;

import org.argeo.api.acr.QNamed;

/** Types related to the http://openrosa.org/xforms/xformsList namespace. */
public enum OrxListName implements QNamed {
	xform,
	// names
	formID, version;

	@Override
	public String getDefaultPrefix() {
		return "orxList";
	}

	@Override
	public String getNamespace() {
		return "http://openrosa.org/xforms/xformsList";
	}

}
