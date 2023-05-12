package org.argeo.app.servlet.odk;

import java.io.IOException;
import java.io.Writer;

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

import org.argeo.api.cms.CmsConstants;
import org.argeo.api.cms.CmsLog;
import org.argeo.app.api.EntityType;
import org.argeo.app.odk.OrxListName;
import org.argeo.app.odk.OrxManifestName;
import org.argeo.cms.auth.RemoteAuthUtils;
import org.argeo.cms.servlet.ServletHttpRequest;
import org.argeo.cms.servlet.ServletUtils;
import org.argeo.jcr.Jcr;
import org.argeo.jcr.JcrxApi;

/** Lists available forms. */
public class OdkFormListServlet extends HttpServlet {
	private static final long serialVersionUID = 2706191315048423321L;
	private final static CmsLog log = CmsLog.getLog(OdkFormListServlet.class);

	private Repository repository;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/xml; charset=utf-8");
		resp.setHeader("X-OpenRosa-Version", "1.0");
		resp.setDateHeader("Date", System.currentTimeMillis());

		// we force HTTPS since ODK Collect will fail anyhow when sending http
		// cf. https://forum.getodk.org/t/authentication-for-non-https-schems/32967/4
		StringBuilder baseServer = ServletUtils.getRequestUrlBase(req, true);

		String pathInfo = req.getPathInfo();

		Session session = RemoteAuthUtils.doAs(() -> Jcr.login(repository, CmsConstants.SYS_WORKSPACE),
				new ServletHttpRequest(req));
		Writer writer = resp.getWriter();
		writer.append("<?xml version='1.0' encoding='UTF-8' ?>");
		writer.append("<xforms xmlns=\"http://openrosa.org/xforms/xformsList\">");
		try {

			Query query;
			if (pathInfo == null) {
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
					sb.append("<downloadUrl>" + baseServer + "/api/odk/form" + node.getPath() + "</downloadUrl>");
					if (node.hasNode(OrxManifestName.manifest.name())) {
						sb.append("<manifestUrl>" + baseServer + "/api/odk/formManifest"
								+ node.getNode(OrxManifestName.manifest.name()).getPath() + "</manifestUrl>");
					}
					sb.append("</xform>");
				} else if (node.isNodeType(EntityType.formSet.get())) {
					sb.append("<xforms-group>");
					sb.append("<groupId>" + node.getPath() + "</groupId>");
					sb.append("<name>" + node.getProperty(Property.JCR_TITLE).getString() + "</name>");
					sb.append("<listUrl>" + baseServer + "/api/odk/formList" + node.getPath() + "</listUrl>");
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
		writer.append("</xforms>");
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

}
