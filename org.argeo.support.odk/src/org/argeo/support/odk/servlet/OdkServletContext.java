package org.argeo.support.odk.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.argeo.cms.servlet.PrivateWwwAuthServletContext;

/** ODK specific authentication (with additional headers).*/
public class OdkServletContext extends PrivateWwwAuthServletContext {

	@Override
	protected void askForWwwAuth(HttpServletRequest request, HttpServletResponse response) {
		super.askForWwwAuth(request, response);
		response.setHeader("X-OpenRosa-Version", "1.0");
		response.setDateHeader("Date", System.currentTimeMillis());

	}

}
