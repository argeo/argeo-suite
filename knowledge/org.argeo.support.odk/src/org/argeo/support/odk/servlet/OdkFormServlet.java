package org.argeo.support.odk.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.argeo.support.odk.OdkForm;

/** Retrieves a single form. */
public class OdkFormServlet extends HttpServlet {
	private static final long serialVersionUID = 7838305967987687370L;

	private Map<String, OdkForm> odkForms = Collections.synchronizedMap(new HashMap<>());

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/xml; charset=utf-8");

		String path = req.getServletPath();
		String fileName = FilenameUtils.getName(path);
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

	public void addForm(OdkForm odkForm) {
		odkForms.put(odkForm.getFileName(), odkForm);
	}

	public void removeForm(OdkForm odkForm) {
		odkForms.remove(odkForm.getFileName());
	}

}
