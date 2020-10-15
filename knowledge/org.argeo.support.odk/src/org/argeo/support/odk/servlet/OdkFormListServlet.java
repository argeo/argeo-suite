package org.argeo.support.odk.servlet;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.support.odk.OdkForm;

/** Lists available forms. */
public class OdkFormListServlet extends HttpServlet {
	private static final long serialVersionUID = 2706191315048423321L;
	private final static Log log = LogFactory.getLog(OdkFormListServlet.class);

	private Set<OdkForm> odkForms = Collections.synchronizedSet(new HashSet<>());

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/xml; charset=utf-8");
		resp.setHeader("X-OpenRosa-Version", "1.0");
		resp.setDateHeader("Date", System.currentTimeMillis());

		String serverName = req.getServerName();
		int serverPort = req.getServerPort();
		String protocol = serverPort == 443 || req.isSecure() ? "https" : "http";

		Writer writer = resp.getWriter();
		writer.append("<?xml version='1.0' encoding='UTF-8' ?>");
		writer.append("<xforms xmlns=\"http://openrosa.org/xforms/xformsList\">");
		for (OdkForm form : odkForms) {
			StringBuilder sb = new StringBuilder();
			sb.append("<xform>");
			sb.append("<formID>" + form.getFormId() + "</formID>");
			sb.append("<name>" + form.getName() + "</name>");
			sb.append("<version>" + form.getVersion() + "</version>");
			sb.append("<hash>" + form.getHash(null) + "</hash>");
			sb.append("<descriptionText>" + form.getDescription() + "</descriptionText>");
			sb.append("<downloadUrl>" + protocol + "://" + serverName
					+ (serverPort == 80
							|| serverPort == 443 ? "" : ":" + serverPort) + "/api/odk/" + form.getFileName()
					+ "</downloadUrl>");
			sb.append("</xform>");
			String str = sb.toString();
			if (log.isDebugEnabled())
				log.debug(str);
			writer.append(str);
		}
		writer.append("</xforms>");
	}

	public void addForm(OdkForm odkForm) {
		odkForms.add(odkForm);
	}

	public void removeForm(OdkForm odkForm) {
		odkForms.remove(odkForm);
	}
}
