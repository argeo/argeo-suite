package org.argeo.app.servlet.odk;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.argeo.api.cms.CmsSession;
import org.argeo.app.core.SuiteUtils;
import org.argeo.app.odk.OrxType;
import org.argeo.app.xforms.FormSubmissionListener;
import org.argeo.api.cms.CmsLog;
import org.argeo.cms.auth.RemoteAuthRequest;
import org.argeo.cms.auth.RemoteAuthUtils;
import org.argeo.cms.jcr.CmsJcrUtils;
import org.argeo.cms.servlet.ServletHttpRequest;
import org.argeo.jcr.Jcr;
import org.argeo.jcr.JcrUtils;

/** Receives a form submission. */
public class OdkSubmissionServlet extends HttpServlet {
	private static final long serialVersionUID = 7834401404691302385L;
	private final static CmsLog log = CmsLog.getLog(OdkSubmissionServlet.class);

	private final static String XML_SUBMISSION_FILE = "xml_submission_file";

	private DateTimeFormatter submissionNameFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd-HHmmssSSS")
			.withZone(ZoneId.from(ZoneOffset.UTC));

	private Repository repository;

	private Set<FormSubmissionListener> submissionListeners = new HashSet<>();

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/xml; charset=utf-8");
		resp.setHeader("X-OpenRosa-Version", "1.0");
		resp.setDateHeader("Date", System.currentTimeMillis());
		resp.setIntHeader("X-OpenRosa-Accept-Content-Length", 1024 * 1024);

		RemoteAuthRequest request = new ServletHttpRequest(req);
		Session session = RemoteAuthUtils.doAs(() -> Jcr.login(repository, null), request);

		try {
//			Node submissions = JcrUtils.mkdirs(session,
//					"/" + EntityType.form.get() + "/" + EntityNames.SUBMISSIONS_BASE);
			CmsSession cmsSession = RemoteAuthUtils.getCmsSession(request);

//			ClassLoader currentContextCl = Thread.currentThread().getContextClassLoader();
//			Thread.currentThread().setContextClassLoader(CmsJcrUtils.class.getClassLoader());
			Session adminSession = null;
			try {
				// TODO centralise at a deeper level
				adminSession = CmsJcrUtils.openDataAdminSession(repository, null);
				SuiteUtils.getOrCreateCmsSessionNode(adminSession, cmsSession);
			} finally {
				Jcr.logout(adminSession);
//				Thread.currentThread().setContextClassLoader(currentContextCl);
			}

			Node cmsSessionNode = SuiteUtils.getCmsSessionNode(session, cmsSession);
			Node submission = cmsSessionNode.addNode(submissionNameFormatter.format(Instant.now()),
					OrxType.submission.get());
			for (Part part : req.getParts()) {
				if (log.isDebugEnabled())
					log.debug("Part: " + part.getName() + ", " + part.getContentType());

				if (part.getName().equals(XML_SUBMISSION_FILE)) {
					Node xml = submission.addNode(XML_SUBMISSION_FILE, NodeType.NT_UNSTRUCTURED);
					session.importXML(xml.getPath(), part.getInputStream(),
							ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);

				} else {
					Node fileNode = JcrUtils.copyStreamAsFile(submission, part.getName(), part.getInputStream());
					String contentType = part.getContentType();
					if (contentType != null) {
						fileNode.addMixin(NodeType.MIX_MIMETYPE);
						fileNode.setProperty(Property.JCR_MIMETYPE, contentType);

					}
					if (part.getName().endsWith(".jpg") || part.getName().endsWith(".png")) {
						// TODO meta data and thumbnails
					}
				}
			}
			session.save();
			for (FormSubmissionListener submissionListener : submissionListeners) {
				submissionListener.formSubmissionReceived(submission);
			}
		} catch (RepositoryException e) {
			e.printStackTrace();
			resp.setStatus(503);
			return;
		} finally {
			Jcr.logout(session);
		}

		resp.setStatus(201);
		resp.getWriter().write("<OpenRosaResponse xmlns=\"http://openrosa.org/http/response\">"
				+ "<message>Form Received!</message>" + "</OpenRosaResponse>");

	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public synchronized void addSubmissionListener(FormSubmissionListener listener) {
		submissionListeners.add(listener);
	}

	public synchronized void removeSubmissionListener(FormSubmissionListener listener) {
		submissionListeners.remove(listener);
	}
}
