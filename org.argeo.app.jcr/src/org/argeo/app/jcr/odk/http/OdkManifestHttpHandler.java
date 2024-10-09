package org.argeo.app.jcr.odk.http;

import static org.argeo.cms.http.CommonMediaType.TEXT_XML;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.argeo.api.app.EntityType;
import org.argeo.api.app.WGS84PosName;
import org.argeo.api.cms.CmsConstants;
import org.argeo.api.cms.CmsLog;
import org.argeo.app.geo.GeoShapeUtils;
import org.argeo.app.odk.OrxManifestName;
import org.argeo.cms.auth.RemoteAuthUtils;
import org.argeo.cms.http.CommonMediaType;
import org.argeo.cms.http.server.HttpRemoteAuthExchange;
import org.argeo.cms.http.server.HttpServerUtils;
import org.argeo.cms.util.CsvWriter;
import org.argeo.cms.util.DigestUtils;
import org.argeo.cms.util.StreamUtils;
import org.argeo.jcr.Jcr;
import org.argeo.jcr.JcrException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/** Describe additional files. */
public class OdkManifestHttpHandler implements HttpHandler {
	private final static CmsLog log = CmsLog.getLog(OdkManifestHttpHandler.class);

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

		try {
			Node node = session.getNode(path);
			if (node.isNodeType(OrxManifestName.manifest.get())) {
				HttpServerUtils.setContentType(exchange, TEXT_XML);
				try (Writer writer = HttpServerUtils.sendResponseAsWriter(exchange)) {
					writer.append("<?xml version='1.0' encoding='UTF-8' ?>");
					writer.append("<manifest xmlns=\"http://openrosa.org/xforms/xformsManifest\">");
					NodeIterator nit = node.getNodes();
					children: while (nit.hasNext()) {
						Node file = nit.nextNode();
						if (file.isNodeType(OrxManifestName.mediaFile.get())) {
							CommonMediaType mimeType = CommonMediaType
									.find(file.getProperty(Property.JCR_MIMETYPE).getString());
							Charset charset = Charset.forName(file.getProperty(Property.JCR_ENCODING).getString());

							if (file.isNodeType(NodeType.NT_ADDRESS)) {
								Node target;
								try {
									target = file.getProperty(Property.JCR_ID).getNode();
								} catch (ItemNotFoundException e) {
									// TODO remove old manifests
									continue children;
								}
								writer.append("<mediaFile>");
								writer.append("<filename>");
								// Work around bug in ODK Collect not supporting paths
								// writer.append(target.getPath().substring(1) + ".xml");
								writer.append(target.getIdentifier() + "." + mimeType.getDefaultExtension());
								writer.append("</filename>");

								MessageDigest messageDigest = MessageDigest.getInstance(DigestUtils.MD5);
								// TODO cache a temp file ?
								try (DigestOutputStream out = new DigestOutputStream(StreamUtils.NULL_OUTPUT_STREAM,
										messageDigest)) {
									writeMediaFile(out, target, mimeType, charset);
									String checksum = DigestUtils.toHexString(out.getMessageDigest().digest());
//								System.out.println(checksum);
									writer.append("<hash>");
									writer.append("md5sum:" + checksum);
									writer.append("</hash>");
								}
								writer.append("<downloadUrl>" + baseServer + "/api/odk/formManifest" + file.getPath()
										+ "</downloadUrl>");
							}
							writer.append("</mediaFile>");
						}
					}
					writer.append("</manifest>");
				} catch (NoSuchAlgorithmException e) {
					throw new IllegalStateException(e);
				}
			} else if (node.isNodeType(OrxManifestName.mediaFile.get())) {
				CommonMediaType mimeType = CommonMediaType.find(node.getProperty(Property.JCR_MIMETYPE).getString());
				Charset charset = Charset.forName(node.getProperty(Property.JCR_ENCODING).getString());
				HttpServerUtils.setContentType(exchange, mimeType, charset);
				if (node.isNodeType(NodeType.NT_ADDRESS)) {
					Node target = node.getProperty(Property.JCR_ID).getNode();
					try (OutputStream out = HttpServerUtils.sendResponse(exchange)) {
						writeMediaFile(out, target, mimeType, charset);
					}
				} else {
					throw new IllegalArgumentException("Unsupported node " + node);
				}
			} else {
				throw new IllegalArgumentException("Unsupported node " + node);
			}
		} catch (RepositoryException e) {
			log.error("Cannot generate manifest", e);
			throw new JcrException(e);
		} finally {
			Jcr.logout(session);
		}

	}

	protected void writeMediaFile(OutputStream out, Node target, CommonMediaType mimeType, Charset charset)
			throws RepositoryException, IOException {
		if (target.isNodeType(NodeType.NT_QUERY)) {
			Query query = target.getSession().getWorkspace().getQueryManager().getQuery(target);
//			System.out.println(query.getStatement());
			QueryResult queryResult = query.execute();

			List<String> columnNames = new ArrayList<>();
			for (String c : queryResult.getColumnNames()) {
				columnNames.add(c);
			}
			// TODO make it more configurable
			columnNames.add("display");
			columnNames.add("geometry");

			if (CommonMediaType.TEXT_XML.equals(mimeType)) {
			} else if (CommonMediaType.TEXT_CSV.equals(mimeType)) {
				CsvWriter csvWriter = new CsvWriter(out, charset);
				csvWriter.writeLine(columnNames);
				RowIterator rit = queryResult.getRows();
				if (rit.hasNext()) {
					while (rit.hasNext()) {
						Row row = rit.nextRow();
						Value[] values = row.getValues();
						List<String> lst = new ArrayList<>();
						for (Value value : values) {
							lst.add(value.getString());
						}
						// display
						lst.add(row.getValue("name").getString() + " (" + row.getValue("label").getString() + ")");
						Node field = row.getNode("geopoint");
						if (field != null && field.isNodeType(EntityType.geopoint.get())) {
							try {
								double lat = field.getProperty(WGS84PosName.lat.get()).getDouble();
								double lon = field.getProperty(WGS84PosName.lon.get()).getDouble();
								double alt = field.hasProperty(WGS84PosName.alt.get())
										? field.getProperty(WGS84PosName.alt.get()).getDouble()
										: Double.NaN;
								String geoshape = GeoShapeUtils.geoPointToGeoShape(lon, lat, alt);
								lst.add(geoshape);
							} catch (RepositoryException e) {
								if (log.isTraceEnabled())
									log.error("Geopoint " + field + " is missing data", e);
							}
						}
						csvWriter.writeLine(lst);
					}
				} else {
					// corner case of an empty initial database
					List<String> lst = new ArrayList<>();
					for (int i = 0; i < columnNames.size(); i++)
						lst.add("-");
					csvWriter.writeLine(lst);
				}
			}
		} else {
			if (CommonMediaType.TEXT_XML.equals(mimeType)) {
				target.getSession().exportDocumentView(target.getPath(), out, true, false);
			} else if (CommonMediaType.TEXT_CSV.equals(mimeType)) {
				CsvWriter csvWriter = new CsvWriter(out, charset);
				csvWriter.writeLine(new Object[] { "name", "label" });
				NodeIterator children = target.getNodes();
				while (children.hasNext()) {
					Node child = children.nextNode();
					String label = Jcr.getTitle(child);
					csvWriter.writeLine(new Object[] { child.getIdentifier(), label });
				}
			}

		}

	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

}
