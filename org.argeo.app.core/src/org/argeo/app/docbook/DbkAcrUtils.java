package org.argeo.app.docbook;

import org.argeo.api.acr.Content;
import org.argeo.api.app.EntityType;

/** Utilities when using ACR to access DocBook. */
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

	public static Content getMetadata(Content infoContainer) {
		if (!infoContainer.hasChild(DbkType.info))
			return null;
		Content info = infoContainer.child(DbkType.info);
		if (!info.hasChild(EntityType.local))
			return null;
		return info.child(EntityType.local);
	}

	/** singleton */
	private DbkAcrUtils() {
	}
}
