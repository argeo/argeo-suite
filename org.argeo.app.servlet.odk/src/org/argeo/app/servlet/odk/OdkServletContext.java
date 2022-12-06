package org.argeo.app.servlet.odk;

import org.argeo.cms.auth.RemoteAuthRequest;
import org.argeo.cms.auth.RemoteAuthResponse;
import org.argeo.cms.servlet.PrivateWwwAuthServletContext;

/** ODK specific authentication (with additional headers). */
public class OdkServletContext extends PrivateWwwAuthServletContext {

	@Override
	protected boolean authIsRequired(RemoteAuthRequest remoteAuthRequest, RemoteAuthResponse remoteAuthResponse) {
		remoteAuthResponse.setHeader("X-OpenRosa-Version", "1.0");
		remoteAuthResponse.setHeader("Date", Long.toString(System.currentTimeMillis()));
		return true;

	}

}
