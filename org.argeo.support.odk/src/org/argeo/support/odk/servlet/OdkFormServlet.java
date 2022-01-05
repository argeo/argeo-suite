package org.argeo.support.odk.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.argeo.cms.servlet.ServletAuthUtils;
import org.argeo.cms.servlet.ServletHttpRequest;
import org.argeo.jcr.Jcr;
import org.argeo.support.odk.OdkForm;
import org.argeo.support.odk.OdkNames;

/** Retrieves a single form. */
public class OdkFormServlet extends HttpServlet {
	private static final long serialVersionUID = 7838305967987687370L;

	private Repository repository;
	private Map<String, OdkForm> odkForms = Collections.synchronizedMap(new HashMap<>());

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/xml; charset=utf-8");

		Session session = ServletAuthUtils.doAs(() -> Jcr.login(repository, null), new ServletHttpRequest(req));

		String pathInfo = req.getPathInfo();
		if (pathInfo.startsWith("//"))
			pathInfo = pathInfo.substring(1);

		boolean oldApproach = false;
		try {
			if (!oldApproach) {
				String path = URLDecoder.decode(pathInfo, StandardCharsets.UTF_8);
				session.exportDocumentView(path + "/" + OdkNames.H_HTML, resp.getOutputStream(), true, false);
			} else {

				String fileName = FilenameUtils.getName(pathInfo);
				OdkForm form = odkForms.get(fileName);
				if (form == null)
					throw new IllegalArgumentException("No form named " + fileName + " was found");

				byte[] buffer = new byte[1024];
				try (InputStream in = form.openStream(); OutputStream out = resp.getOutputStream();) {
					int bytesRead;
					while ((bytesRead = in.read(buffer)) != -1)
						out.write(buffer, 0, bytesRead);
				}
			}
		} catch (RepositoryException e) {
			e.printStackTrace();
			// TODO error message
			resp.sendError(500);
		} finally {
			Jcr.logout(session);
		}
	}

	public void addForm(OdkForm odkForm) {
		odkForms.put(odkForm.getFileName(), odkForm);
	}

	public void removeForm(OdkForm odkForm) {
		odkForms.remove(odkForm.getFileName());
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

}
