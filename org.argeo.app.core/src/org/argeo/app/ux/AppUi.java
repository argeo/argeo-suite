package org.argeo.app.ux;

import org.argeo.api.cms.ux.CmsUi;
import org.argeo.cms.Localized;

public interface AppUi extends CmsUi {
	Localized getTitle();
	
	boolean isLoginScreen();

}
