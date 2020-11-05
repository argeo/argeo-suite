package org.argeo.support.odk.servlet;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.cms.servlet.ServletAuthUtils;
import org.argeo.entity.EntityNames;
import org.argeo.jcr.Jcr;
import org.argeo.jcr.JcrUtils;
import org.argeo.support.odk.OrxType;

/** Receives a form submission. */
public class OdkSubmissionServlet extends HttpServlet {
	private static final long serialVersionUID = 7834401404691302385L;
	private final static Log log = LogFactory.getLog(OdkSubmissionServlet.class);

	private final static String XML_SUBMISSION_FILE = "xml_submission_file";

	private DateTimeFormatter submissionNameFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd-HHmmssSSS")
			.withZone(ZoneId.from(ZoneOffset.UTC));

	private Repository repository;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/xml; charset=utf-8");
		resp.setHeader("X-OpenRosa-Version", "1.0");
		resp.setDateHeader("Date", System.currentTimeMillis());
		resp.setIntHeader("X-OpenRosa-Accept-Content-Length", 1024 * 1024);

		Session session = ServletAuthUtils.doAs(() -> Jcr.login(repository, null), req);

		try {
			Node submissions = JcrUtils.mkdirs(session,
					"/" + EntityNames.FORM_BASE + "/" + EntityNames.SUBMISSIONS_BASE);
			Node submission = submissions.addNode(submissionNameFormatter.format(Instant.now()),
					OrxType.submission.get());
			for (Part part : req.getParts()) {
				if (log.isDebugEnabled())
					log.debug("Part: " + part.getName() + ", " + part.getContentType());

				if (part.getName().equals(XML_SUBMISSION_FILE)) {
					Node xml = submission.addNode(XML_SUBMISSION_FILE, NodeType.NT_UNSTRUCTURED);
					session.importXML(xml.getPath(), part.getInputStream(),
							ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);

//					Part xmlSubmissionPart = req.getPart(XML_SUBMISSION_FILE);
//					if (xmlSubmissionPart == null)
//						throw new ServletException("No " + XML_SUBMISSION_FILE + " part");
//				try (InputStream in = xmlSubmissionPart.getInputStream();) {
//					// pretty print
//					Transformer transformer = TransformerFactory.newInstance().newTransformer();
//					transformer.setOutputProperty(OutputKeys.INDENT, "yes");
//					transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
//					StreamResult result = new StreamResult(new StringWriter());
//					StreamSource source = new StreamSource(in);
//					transformer.transform(source, result);
//					String xmlString = result.getWriter().toString();
//					System.out.println(xmlString);
//				} catch (TransformerException e) {
//					e.printStackTrace();
//				}

				} else {
					JcrUtils.copyStreamAsFile(submission, part.getName(), part.getInputStream());
				}
			}
			session.save();
		} catch (RepositoryException e) {
			e.printStackTrace();
			resp.setStatus(503);
			return;
		}

		resp.setStatus(201);
		resp.getWriter().write("<OpenRosaResponse xmlns=\"http://openrosa.org/http/response\">"
				+ "<message>Form Received!</message>" + "</OpenRosaResponse>");

	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

}
