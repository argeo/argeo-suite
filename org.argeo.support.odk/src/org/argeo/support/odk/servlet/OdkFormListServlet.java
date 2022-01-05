package org.argeo.support.odk.servlet;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.api.NodeConstants;
import org.argeo.cms.auth.RemoteAuthUtils;
import org.argeo.cms.servlet.ServletHttpRequest;
import org.argeo.entity.EntityType;
import org.argeo.jcr.Jcr;
import org.argeo.jcr.JcrxApi;
import org.argeo.support.odk.OdkForm;
import org.argeo.support.odk.OrxListName;
import org.argeo.support.odk.OrxManifestName;

/** Lists available forms. */
public class OdkFormListServlet extends HttpServlet {
	private static final long serialVersionUID = 2706191315048423321L;
	private final static Log log = LogFactory.getLog(OdkFormListServlet.class);

	private Set<OdkForm> odkForms = Collections.synchronizedSet(new HashSet<>());

//	private DateTimeFormatter versionFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd-HHmm")
//			.withZone(ZoneId.from(ZoneOffset.UTC));

	private Repository repository;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/xml; charset=utf-8");
		resp.setHeader("X-OpenRosa-Version", "1.0");
		resp.setDateHeader("Date", System.currentTimeMillis());

		String serverName = req.getServerName();
		int serverPort = req.getServerPort();
		String protocol = serverPort == 443 || req.isSecure() ? "https" : "http";

		String pathInfo = req.getPathInfo();

		Session session = RemoteAuthUtils.doAs(() -> Jcr.login(repository, NodeConstants.SYS_WORKSPACE),
				new ServletHttpRequest(req));
//		session = NodeUtils.openDataAdminSession(repository, NodeConstants.SYS_WORKSPACE);
		Writer writer = resp.getWriter();
		writer.append("<?xml version='1.0' encoding='UTF-8' ?>");
		writer.append("<xforms xmlns=\"http://openrosa.org/xforms/xformsList\">");
		boolean oldApproach = false;
		if (!oldApproach) {
			try {

				Query query;
				if (pathInfo == null) {
//				query = session.getWorkspace().getQueryManager()
//						.createQuery("SELECT * FROM [nt:unstructured]", Query.JCR_SQL2);
					query = session.getWorkspace().getQueryManager()
							.createQuery("SELECT * FROM [" + OrxListName.xform.get() + "]", Query.JCR_SQL2);
				} else {
					query = session.getWorkspace().getQueryManager()
							.createQuery(
									"SELECT node FROM [" + OrxListName.xform.get()
											+ "] AS node WHERE ISDESCENDANTNODE (node, '" + pathInfo + "')",
									Query.JCR_SQL2);
				}
				QueryResult queryResult = query.execute();

				NodeIterator nit = queryResult.getNodes();
//				log.debug(session.getUserID());
//				log.debug(session.getWorkspace().getName());
//				NodeIterator nit = session.getRootNode().getNodes();
//				while (nit.hasNext()) {
//					log.debug(nit.nextNode());
//				}
				while (nit.hasNext()) {
					StringBuilder sb = new StringBuilder();
					Node node = nit.nextNode();
					if (node.isNodeType(OrxListName.xform.get())) {
						sb.append("<xform>");
						sb.append("<formID>" + node.getProperty(OrxListName.formID.get()).getString() + "</formID>");
						sb.append("<name>" + Jcr.getTitle(node) + "</name>");
						sb.append("<version>" + node.getProperty(OrxListName.version.get()).getString() + "</version>");
						sb.append("<hash>md5:" + JcrxApi.getChecksum(node, JcrxApi.MD5) + "</hash>");
						if (node.hasProperty(Property.JCR_DESCRIPTION))
							sb.append("<name>" + node.getProperty(Property.JCR_DESCRIPTION).getString() + "</name>");
						sb.append("<downloadUrl>" + protocol + "://" + serverName
								+ (serverPort == 80 || serverPort == 443 ? "" : ":" + serverPort) + "/api/odk/form"
								+ node.getPath() + "</downloadUrl>");
						if (node.hasNode(OrxManifestName.manifest.name())) {
							sb.append("<manifestUrl>" + protocol + "://" + serverName
									+ (serverPort == 80 || serverPort == 443 ? "" : ":" + serverPort)
									+ "/api/odk/formManifest" + node.getNode(OrxManifestName.manifest.name()).getPath()
									+ "</manifestUrl>");
						}
						sb.append("</xform>");
					} else if (node.isNodeType(EntityType.formSet.get())) {
						sb.append("<xforms-group>");
						sb.append("<groupId>" + node.getPath() + "</groupId>");
						sb.append("<name>" + node.getProperty(Property.JCR_TITLE).getString() + "</name>");
						sb.append("<listUrl>" + protocol + "://" + serverName
								+ (serverPort == 80 || serverPort == 443 ? "" : ":" + serverPort) + "/api/odk/formList"
								+ node.getPath() + "</listUrl>");
						sb.append("</xforms-group>");
					}
					String str = sb.toString();
					if (!str.equals("")) {
						if (log.isDebugEnabled())
							log.debug(str);
						writer.append(str);
					}
				}
			} catch (RepositoryException e) {
				e.printStackTrace();
				// TODO error message
				// resp.sendError(500);
				resp.sendError(503);
			} finally {
				Jcr.logout(session);
			}

		} else {
			for (OdkForm form : odkForms) {
				StringBuilder sb = new StringBuilder();
				sb.append("<xform>");
				sb.append("<formID>" + form.getFormId() + "</formID>");
				sb.append("<name>" + form.getName() + "</name>");
				sb.append("<version>" + form.getVersion() + "</version>");
				sb.append("<hash>" + form.getHash(null) + "</hash>");
				sb.append("<descriptionText>" + form.getDescription() + "</descriptionText>");
				sb.append("<downloadUrl>" + protocol + "://" + serverName
						+ (serverPort == 80 || serverPort == 443 ? "" : ":" + serverPort) + "/api/odk/form/"
						+ form.getFileName() + "</downloadUrl>");
				sb.append("</xform>");
				String str = sb.toString();
				if (log.isDebugEnabled())
					log.debug(str);
				writer.append(str);
			}
		}
		writer.append("</xforms>");
	}

	public void addForm(OdkForm odkForm) {
		odkForms.add(odkForm);
	}

	public void removeForm(OdkForm odkForm) {
		odkForms.remove(odkForm);
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

}
