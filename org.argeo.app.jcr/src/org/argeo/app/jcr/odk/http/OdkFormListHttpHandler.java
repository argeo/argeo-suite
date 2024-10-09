package org.argeo.app.jcr.odk.http;

import static org.argeo.cms.http.HttpStatus.INTERNAL_SERVER_ERROR;

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

import org.argeo.api.app.EntityType;
import org.argeo.api.cms.CmsConstants;
import org.argeo.api.cms.CmsLog;
import org.argeo.app.odk.OrxListName;
import org.argeo.app.odk.OrxManifestName;
import org.argeo.cms.auth.RemoteAuthUtils;
import org.argeo.cms.http.server.HttpRemoteAuthExchange;
import org.argeo.cms.http.server.HttpServerUtils;
import org.argeo.jcr.Jcr;
import org.argeo.jcr.JcrxApi;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/** Lists available forms. */
public class OdkFormListHttpHandler implements HttpHandler {
	private final static CmsLog log = CmsLog.getLog(OdkFormListHttpHandler.class);

	private Repository repository;

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		OdkHttpUtils.addOdkResponseHeaders(exchange);
		// we force HTTPS since ODK Collect will fail anyhow when sending http
		// cf. https://forum.getodk.org/t/authentication-for-non-https-schems/32967/4
		StringBuilder baseServer = HttpServerUtils.getRequestUrlBase(exchange, true);
		String path = HttpServerUtils.subPath(exchange);
		Session session = RemoteAuthUtils.doAs(() -> Jcr.login(repository, CmsConstants.SYS_WORKSPACE),
				new HttpRemoteAuthExchange(exchange));

		try (Writer writer = HttpServerUtils.sendResponseAsWriter(exchange)) {
			writer.append("<?xml version='1.0' encoding='UTF-8' ?>");
			writer.append("<xforms xmlns=\"http://openrosa.org/xforms/xformsList\">");
			Query query;
			if (path == null || "/".equals(path)) {
				query = session.getWorkspace().getQueryManager()
						.createQuery("SELECT * FROM [" + OrxListName.xform.get() + "]", Query.JCR_SQL2);
			} else {
				query = session.getWorkspace().getQueryManager().createQuery("SELECT node FROM ["
						+ OrxListName.xform.get() + "] AS node WHERE ISDESCENDANTNODE (node, '" + path + "')",
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
					if (log.isTraceEnabled())
						log.trace(str);
					writer.append(str);
				}
			}
			writer.append("</xforms>");
		} catch (RepositoryException e) {
			// TODO error message
			e.printStackTrace();
			HttpServerUtils.sendStatusOnly(exchange, INTERNAL_SERVER_ERROR);
		} finally {
			Jcr.logout(session);
		}
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

}
