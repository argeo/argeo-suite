package org.argeo.support.odk.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** Receives a form submission. */
public class OdkSubmissionServlet extends HttpServlet {
	private static final long serialVersionUID = 7834401404691302385L;
	private final static Log log = LogFactory.getLog(OdkSubmissionServlet.class);

	private final static String XML_SUBMISSION_FILE = "xml_submission_file";

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		for (Part part : req.getParts()) {
			if (log.isDebugEnabled())
				log.debug("Part: " + part.getName() + ", " + part.getContentType());
		}
		Part xmlSubmissionPart = req.getPart(XML_SUBMISSION_FILE);
		if (xmlSubmissionPart == null)
			throw new ServletException("No " + XML_SUBMISSION_FILE + " part");
		try (InputStream in = xmlSubmissionPart.getInputStream();) {
			// pretty print
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			StreamResult result = new StreamResult(new StringWriter());
			StreamSource source = new StreamSource(in);
			transformer.transform(source, result);
			String xmlString = result.getWriter().toString();
			System.out.println(xmlString);
		} catch (TransformerException e) {
			e.printStackTrace();
		}

		resp.setContentType("text/xml; charset=utf-8");
		resp.setHeader("X-OpenRosa-Version", "1.0");
		resp.setDateHeader("Date", System.currentTimeMillis());
		resp.setIntHeader("X-OpenRosa-Accept-Content-Length", 1024 * 1024);
		resp.setStatus(201);
		resp.getWriter().write("<OpenRosaResponse xmlns=\"http://openrosa.org/http/response\">"
				+ "<message>Form Received!</message>" + "</OpenRosaResponse>");

	}
}
