package org.argeo.app.docbook;

import org.argeo.api.acr.Content;

public class DbkAcrUtils {
	/** Whether this DocBook element is of this type. */
	public static boolean isDbk(Content content, DbkType type) {
		return content.isContentClass(type.qName());
	}

	public static String getMediaFileref(Content node) {
		Content mediadata;
		if (node.hasChild(DbkType.imageobject)) {
			mediadata = node.child(DbkType.imageobject).child(DbkType.imagedata);
		} else {
			mediadata = node.child(DbkType.videoobject).child(DbkType.videodata);
		}

		if (mediadata.containsKey(DbkAttr.fileref)) {
			return mediadata.attr(DbkAttr.fileref);
		} else {
			return null;
		}
	}

	/** singleton */
	private DbkAcrUtils() {
	}
}
