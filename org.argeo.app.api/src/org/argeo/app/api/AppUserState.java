package org.argeo.app.api;

import org.argeo.api.acr.Content;
import org.argeo.api.acr.ContentSession;
import org.argeo.api.cms.CmsSession;

public interface AppUserState {
	Content getOrCreateSessionDir(ContentSession contentSession, CmsSession session);
}
