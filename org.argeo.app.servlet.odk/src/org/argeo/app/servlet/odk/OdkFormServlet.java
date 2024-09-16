package org.argeo.app.servlet.odk;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.argeo.app.odk.OdkNames;
import org.argeo.cms.auth.RemoteAuthUtils;
import org.argeo.cms.servlet.javax.JavaxServletHttpRequest;
import org.argeo.jcr.Jcr;

/** Retrieves a single form. */
public class OdkFormServlet extends HttpServlet {
	private static final long serialVersionUID = 7838305967987687370L;

	private Repository repository;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/xml; charset=utf-8");

		Session session = RemoteAuthUtils.doAs(() -> Jcr.login(repository, null), new JavaxServletHttpRequest(req));

		String pathInfo = req.getPathInfo();
		if (pathInfo.startsWith("//"))
			pathInfo = pathInfo.substring(1);

		try {
			String path = URLDecoder.decode(pathInfo, StandardCharsets.UTF_8);
			session.exportDocumentView(path + "/" + OdkNames.H_HTML, resp.getOutputStream(), true, false);
		} catch (RepositoryException e) {
			e.printStackTrace();
			// TODO error message
			resp.sendError(500);
		} finally {
			Jcr.logout(session);
		}
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

}
