package org.argeo.app.jcr.odk.http;

import static org.argeo.cms.http.HttpStatus.INTERNAL_SERVER_ERROR;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.api.cms.CmsConstants;
import org.argeo.app.odk.OdkNames;
import org.argeo.cms.auth.RemoteAuthUtils;
import org.argeo.cms.http.server.HttpRemoteAuthExchange;
import org.argeo.cms.http.server.HttpServerUtils;
import org.argeo.jcr.Jcr;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/** Retrieves a single form. */
public class OdkFormHttpHandler implements HttpHandler {
	private Repository repository;

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		OdkHttpUtils.addOdkResponseHeaders(exchange);

		String pathInfo = HttpServerUtils.subPath(exchange);
		Session session = RemoteAuthUtils.doAs(() -> Jcr.login(repository, CmsConstants.SYS_WORKSPACE),
				new HttpRemoteAuthExchange(exchange));

		if (pathInfo.startsWith("//"))
			pathInfo = pathInfo.substring(1);

		try (OutputStream out = HttpServerUtils.sendResponse(exchange)) {
			String path = URLDecoder.decode(pathInfo, StandardCharsets.UTF_8);
			session.exportDocumentView(path + "/" + OdkNames.H_HTML, out, true, false);
		} catch (RepositoryException e) {
			// TODO error message
			e.printStackTrace();
			HttpServerUtils.sendStatusOnly(exchange, INTERNAL_SERVER_ERROR);
		} finally {
			Jcr.logout(session);
		}
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

}
