package org.argeo.app.servlet.odk;

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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.output.NullOutputStream;
import org.argeo.app.api.EntityMimeType;
import org.argeo.app.odk.OrxManifestName;
import org.argeo.cms.auth.RemoteAuthUtils;
import org.argeo.cms.servlet.ServletHttpRequest;
import org.argeo.cms.util.CsvWriter;
import org.argeo.cms.util.DigestUtils;
import org.argeo.jcr.Jcr;
import org.argeo.jcr.JcrException;

/** Describe additional files. */
public class OdkManifestServlet extends HttpServlet {
	private static final long serialVersionUID = 138030510865877478L;

	private Repository repository;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setHeader("X-OpenRosa-Version", "1.0");
		resp.setDateHeader("Date", System.currentTimeMillis());

		String pathInfo = req.getPathInfo();
		if (pathInfo.startsWith("//"))
			pathInfo = pathInfo.substring(1);

		String serverName = req.getServerName();
		int serverPort = req.getServerPort();
		String protocol = serverPort == 443 || req.isSecure() ? "https" : "http";

		Session session = RemoteAuthUtils.doAs(() -> Jcr.login(repository, null), new ServletHttpRequest(req));

		try {
			Node node = session.getNode(pathInfo);
			if (node.isNodeType(OrxManifestName.manifest.get())) {
				resp.setContentType(EntityMimeType.XML.toHttpContentType());
				Writer writer = resp.getWriter();
				writer.append("<?xml version='1.0' encoding='UTF-8' ?>");
				writer.append("<manifest xmlns=\"http://openrosa.org/xforms/xformsManifest\">");
				NodeIterator nit = node.getNodes();
				children: while (nit.hasNext()) {
					Node file = nit.nextNode();
					if (file.isNodeType(OrxManifestName.mediaFile.get())) {
						EntityMimeType mimeType = EntityMimeType
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
							try (DigestOutputStream out = new DigestOutputStream(NullOutputStream.NULL_OUTPUT_STREAM,
									messageDigest)) {
								writeMediaFile(out, target, mimeType, charset);
								writer.append("<hash>");
								writer.append("md5sum:" + DigestUtils.toHexString(out.getMessageDigest().digest()));
								writer.append("</hash>");
							}
							writer.append("<downloadUrl>" + protocol + "://" + serverName
									+ (serverPort == 80 || serverPort == 443 ? "" : ":" + serverPort)
									+ "/api/odk/formManifest" + file.getPath() + "</downloadUrl>");
						}
						writer.append("</mediaFile>");
					}
				}

				writer.append("</manifest>");
			} else if (node.isNodeType(OrxManifestName.mediaFile.get())) {
				EntityMimeType mimeType = EntityMimeType.find(node.getProperty(Property.JCR_MIMETYPE).getString());
				Charset charset = Charset.forName(node.getProperty(Property.JCR_ENCODING).getString());
				resp.setContentType(mimeType.toHttpContentType(charset));
				if (node.isNodeType(NodeType.NT_ADDRESS)) {
					Node target = node.getProperty(Property.JCR_ID).getNode();

					writeMediaFile(resp.getOutputStream(), target, mimeType, charset);
				} else {
					throw new IllegalArgumentException("Unsupported node " + node);
				}
			} else {
				throw new IllegalArgumentException("Unsupported node " + node);
			}
		} catch (RepositoryException e) {
			throw new JcrException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new ServletException(e);
		} finally {
			Jcr.logout(session);
		}

	}

	protected void writeMediaFile(OutputStream out, Node target, EntityMimeType mimeType, Charset charset)
			throws RepositoryException, IOException {
		if (target.isNodeType(NodeType.NT_QUERY)) {
			Query query = target.getSession().getWorkspace().getQueryManager().getQuery(target);
			QueryResult queryResult = query.execute();
			String[] columnNames = queryResult.getColumnNames();
			if (EntityMimeType.XML.equals(mimeType)) {
			} else if (EntityMimeType.CSV.equals(mimeType)) {
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
						csvWriter.writeLine(lst);
					}
				} else {
					// corner case of an empty initial database
					List<String> lst = new ArrayList<>();
					for (int i = 0; i < columnNames.length; i++)
						lst.add("-");
					csvWriter.writeLine(lst);
				}
			}
		} else {
			if (EntityMimeType.XML.equals(mimeType)) {
				target.getSession().exportDocumentView(target.getPath(), out, true, false);
			} else if (EntityMimeType.CSV.equals(mimeType)) {
				CsvWriter csvWriter = new CsvWriter(out, charset);
				csvWriter.writeLine(new String[] { "name", "label" });
				NodeIterator children = target.getNodes();
				while (children.hasNext()) {
					Node child = children.nextNode();
					String label = Jcr.getTitle(child);
					csvWriter.writeLine(new String[] { child.getIdentifier(), label });
				}
			}

		}

	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

}
