package org.argeo.api.app;

import org.argeo.api.acr.Content;
import org.argeo.api.cms.CmsSession;

/** Access to content which is specific to a user and their state. */
public interface AppUserState {
	Content getOrCreateSessionDir(CmsSession session);
}
