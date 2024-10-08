package org.argeo.app.jcr.odk.http;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.argeo.cms.http.HttpStatus.CREATED;
import static org.argeo.cms.http.HttpStatus.INTERNAL_SERVER_ERROR;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.nodetype.NodeType;

import org.argeo.api.acr.Content;
import org.argeo.api.app.AppUserState;
import org.argeo.api.cms.CmsLog;
import org.argeo.api.cms.CmsSession;
import org.argeo.app.image.ImageProcessor;
import org.argeo.app.odk.OrxType;
import org.argeo.app.xforms.FormSubmissionListener;
import org.argeo.cms.auth.RemoteAuthUtils;
import org.argeo.cms.http.server.HttpRemoteAuthExchange;
import org.argeo.cms.http.server.HttpServerUtils;
import org.argeo.cms.http.server.MimePart;
import org.argeo.cms.jcr.acr.JcrContent;
import org.argeo.jcr.JcrUtils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/** Receives a form submission. */
public class OdkSubmissionHttpHandler implements HttpHandler {
	private final static CmsLog log = CmsLog.getLog(OdkSubmissionHttpHandler.class);

	private final static String XML_SUBMISSION_FILE = "xml_submission_file";
	private final static String IS_INCOMPLETE = "*isIncomplete*";

	private DateTimeFormatter submissionNameFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd-HHmmssSSS")
			.withZone(ZoneId.from(ZoneOffset.UTC));

	private Set<FormSubmissionListener> submissionListeners = new HashSet<>();

	private AppUserState appUserState;

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		OdkHttpUtils.addOdkResponseHeaders(exchange);
		CmsSession cmsSession = RemoteAuthUtils.getCmsSession(new HttpRemoteAuthExchange(exchange));

		boolean isIncomplete = false;
		try {
			Content sessionDir = appUserState.getOrCreateSessionDir(cmsSession);
			Node cmsSessionNode = sessionDir.adapt(Node.class);
			String submissionName = submissionNameFormatter.format(Instant.now());
			Node submission = cmsSessionNode.addNode(submissionName, OrxType.submission.get());
			String submissionPath = submission.getPath();
			for (MimePart part : HttpServerUtils.extractFormData(exchange)) {
				String partNameSane = JcrUtils.replaceInvalidChars(part.getName());
				if (log.isTraceEnabled())
					log.trace("Part: " + part.getName() + ", " + part.getContentType());

				if (part.getName().equals(XML_SUBMISSION_FILE)) {
					Node xml = submission.addNode(XML_SUBMISSION_FILE, NodeType.NT_UNSTRUCTURED);
					cmsSessionNode.getSession().importXML(xml.getPath(), part.getInputStream(),
							ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);

				} else if (part.getName().equals(IS_INCOMPLETE)) {
					isIncomplete = true;
					log.debug("Form submission " + submissionPath + " is incomplete, expecting more to be uploaded...");
				} else {
					Node fileNode;
					if (part.getName().endsWith(".jpg")) {
						// Fix metadata
						Path temp = Files.createTempFile("image", ".jpg");
						try {
							ImageProcessor imageProcessor = new ImageProcessor(() -> part.getInputStream(),
									() -> Files.newOutputStream(temp));
							imageProcessor.process();
							fileNode = JcrUtils.copyStreamAsFile(submission, partNameSane, Files.newInputStream(temp));
						} finally {
							Files.deleteIfExists(temp);
						}
					} else {
						fileNode = JcrUtils.copyStreamAsFile(submission, partNameSane, part.getInputStream());
					}
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

			cmsSessionNode.getSession().save();
			try {
				for (FormSubmissionListener submissionListener : submissionListeners) {
					submissionListener.formSubmissionReceived(JcrContent.nodeToContent(submission), isIncomplete);
				}
			} catch (Exception e) {
				log.error("Cannot save submission, cancelling...", e);
				if (cmsSessionNode.getSession().hasPendingChanges())
					cmsSessionNode.getSession().refresh(false);// discard
				if (cmsSessionNode.getSession().itemExists(submissionPath))
					submission.remove();
				cmsSessionNode.getSession().save();
				HttpServerUtils.sendStatusOnly(exchange, INTERNAL_SERVER_ERROR);
				return;
			}

		} catch (Exception e) {
			log.error("Cannot save submission", e);
			HttpServerUtils.sendStatusOnly(exchange, INTERNAL_SERVER_ERROR);
			return;
		}

		byte[] msg = ("<OpenRosaResponse xmlns=\"http://openrosa.org/http/response\">"
				+ "<message>Form Received!</message>" + "</OpenRosaResponse>").getBytes(UTF_8);
		exchange.sendResponseHeaders(CREATED.get(), msg.length);
		exchange.getResponseBody().write(msg);
	}

	public synchronized void addSubmissionListener(FormSubmissionListener listener) {
		submissionListeners.add(listener);
	}

	public synchronized void removeSubmissionListener(FormSubmissionListener listener) {
		submissionListeners.remove(listener);
	}

	public void setAppUserState(AppUserState appUserState) {
		this.appUserState = appUserState;
	}

}
