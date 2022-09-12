package org.argeo.app.docbook;

import org.argeo.api.acr.Content;

public class DbkAcrUtils {
	/** Whether this DocBook element is of this type. */
	public static boolean isDbk(Content content, DbkType type) {
		return content.isContentClass(type.qName());
	}

	/** singleton */
	private DbkAcrUtils() {
	}
}
