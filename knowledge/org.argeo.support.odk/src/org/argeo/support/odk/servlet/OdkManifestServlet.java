package org.argeo.support.odk.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.argeo.cms.servlet.ServletAuthUtils;
import org.argeo.jcr.Jcr;
import org.argeo.jcr.JcrException;
import org.argeo.support.odk.OrxManifestName;
import org.argeo.util.DigestUtils;

/** Describe additional files. */
public class OdkManifestServlet extends HttpServlet {
	private static final long serialVersionUID = 138030510865877478L;

	private Repository repository;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/xml; charset=utf-8");
		resp.setHeader("X-OpenRosa-Version", "1.0");
		resp.setDateHeader("Date", System.currentTimeMillis());

		String pathInfo = req.getPathInfo();
		if (pathInfo.startsWith("//"))
			pathInfo = pathInfo.substring(1);

		String serverName = req.getServerName();
		int serverPort = req.getServerPort();
		String protocol = serverPort == 443 || req.isSecure() ? "https" : "http";

		Session session = ServletAuthUtils.doAs(() -> Jcr.login(repository, null), req);

		try {
			Node node = session.getNode(pathInfo);
			if (node.isNodeType(OrxManifestName.manifest.get())) {
				Writer writer = resp.getWriter();
				writer.append("<?xml version='1.0' encoding='UTF-8' ?>");
				writer.append("<manifest xmlns=\"http://openrosa.org/xforms/xformsManifest\">");
				NodeIterator nit = node.getNodes();
				while (nit.hasNext()) {
					Node file = nit.nextNode();
					if (file.isNodeType(OrxManifestName.mediaFile.get())) {
						writer.append("<mediaFile>");

						if (file.isNodeType(NodeType.NT_ADDRESS)) {
							Node target = file.getProperty(Property.JCR_ID).getNode();
							writer.append("<filename>");
							// Work around bug in ODK Collect not supporting paths
							// writer.append(target.getPath().substring(1) + ".xml");
							writer.append(target.getIdentifier() + ".xml");
							writer.append("</filename>");

//							StringBuilder xml = new StringBuilder();
//							xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
//							JcrUtils.toSimpleXml(target, xml);
//							String fileCsum = DigestUtils.digest(DigestUtils.MD5,
//									xml.toString().getBytes(StandardCharsets.UTF_8));
//							writer.append("<hash>");
//							writer.append("md5sum:" + fileCsum);
//							writer.append("</hash>");

							try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
								session.exportDocumentView(target.getPath(), out, true, false);
								String fileCsum = DigestUtils.digest(DigestUtils.MD5, out.toByteArray());
//						JcrxApi.addChecksum(file, fileCsum);
								writer.append("<hash>");
								writer.append("md5sum:" + fileCsum);
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
				if (node.isNodeType(NodeType.NT_ADDRESS)) {
					Node target = node.getProperty(Property.JCR_ID).getNode();

//					StringBuilder xml = new StringBuilder();
//					xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
//					JcrUtils.toSimpleXml(target, xml);
//					System.out.println(xml);
//					resp.getOutputStream().write(xml.toString().getBytes(StandardCharsets.UTF_8));
//					resp.flushBuffer();

					try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
						session.exportDocumentView(target.getPath(), out, true, false);
						System.out.println(new String(out.toByteArray(), StandardCharsets.UTF_8));
						resp.getOutputStream().write(out.toByteArray());
					}
				} else {
					throw new IllegalArgumentException("Unsupported node " + node);
				}
			} else {
				throw new IllegalArgumentException("Unsupported node " + node);
			}
		} catch (RepositoryException e) {
			throw new JcrException(e);
		} finally {
			Jcr.logout(session);
		}

	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

}
