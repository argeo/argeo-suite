package org.argeo.publishing.servlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.xalan.xsltc.trax.SmartTransformerFactoryImpl;
import org.argeo.cms.servlet.ServletAuthUtils;
import org.argeo.jcr.Jcr;
import org.argeo.jcr.JcrException;
import org.w3c.dom.Document;

public class DbkServlet extends HttpServlet {
	private static final long serialVersionUID = 6906020513498289335L;

	private Repository repository;

	private DocumentBuilderFactory factory;
	private TransformerFactory transformerFactory;
	private Templates docBoookTemplates;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/html; charset=utf-8");

		Session session = null;
		try {
			session = ServletAuthUtils.doAs(() -> Jcr.login(repository, null), req);

			String pathInfo = req.getPathInfo();
			if (pathInfo.startsWith("//"))
				pathInfo = pathInfo.substring(1);
			String path = URLDecoder.decode(pathInfo, StandardCharsets.UTF_8);

			byte[] arr;
			try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
				session.exportDocumentView(path, out, true, false);
				arr = out.toByteArray();
				System.out.println(new String(arr, StandardCharsets.UTF_8));
			} catch (RepositoryException e) {
				throw new JcrException(e);
			}

			try (InputStream in = new ByteArrayInputStream(arr);
					ByteArrayOutputStream out = new ByteArrayOutputStream();) {
//			Source xsl = new StreamSource(new File("/usr/share/sgml/docbook/xsl-stylesheets/xhtml/docbook.xsl"));
//			Source xsl = new StreamSource(
//					Files.newBufferedReader(Paths.get("/home/mbaudier/Downloads/docbook-xsl-1.79.2/xhtml/docbook.xsl"),
//							StandardCharsets.US_ASCII),
//					"file:///home/mbaudier/Downloads/docbook-xsl-1.79.2/xhtml/docbook.xsl");
				Source xsl = new StreamSource(
						new File(System.getProperty("user.home") + "/Downloads/docbook-xsl-1.79.2/html/docbook.xsl"));
//				if (docBoookTemplates == null) {
//					try {
//						docBoookTemplates = transformerFactory.newTemplates(xsl);
//					} catch (TransformerConfigurationException e) {
//						throw new ServletException("Cannot instantiate XSL " + xsl, e);
//					}
//				}

//			Source xmlInput = new StreamSource(new File("/home/mbaudier/dev/git/gpl/argeo-qa/doc/platform/argeo-platform.dbk.xml"));
//			Source xmlInput = new StreamSource(in);
				Result xmlOutput = new StreamResult(out);

//				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
////			DocumentBuilderFactory factory = new org.apache.xerces.jaxp.DocumentBuilderFactoryImpl();
//				factory.setXIncludeAware(true);
//				factory.setNamespaceAware(true);
				DocumentBuilder docBuilder = factory.newDocumentBuilder();
//			Document doc = docBuilder
//					.parse(new File("/home/mbaudier/dev/git/gpl/argeo-qa/doc/platform/argeo-platform.dbk.xml"));
				Document doc = docBuilder.parse(in);
				Source xmlInput = new DOMSource(doc);

//			SAXParserFactory factory = SAXParserFactory.newInstance();
//			SAXParser saxParser = factory.newSAXParser();
//			Source xmlInput = new SAXSource(saxParser.getXMLReader(),
//					new InputSource(new FileReader(new File("/home/mbaudier/dev/git/gpl/argeo-qa/doc/platform/argeo-platform.dbk.xml"),StandardCharsets.UTF_8)));

//			Source xmlInput = new StreamSource(
//					new File("/home/mbaudier/dev/workspaces/argeo-suite/xslt-test/input.xml"));
//			Result xmlOutput = new StreamResult(new File("output.html"));

//			TransformerFactory transformerFactory = new org.apache.xalan.processor.TransformerFactoryImpl();
				Transformer transformer = transformerFactory.newTransformer(xsl);
				transformer.transform(xmlInput, xmlOutput);
				resp.getOutputStream().write(out.toByteArray());
			} catch (Exception e) {
				throw new ServletException("Cannot transform " + path, e);
			}
		} finally {
			Jcr.logout(session);
		}
	}

	@Override
	public void init() throws ServletException {
		factory = DocumentBuilderFactory.newInstance();
		factory.setXIncludeAware(true);
		factory.setNamespaceAware(true);
		transformerFactory = new SmartTransformerFactoryImpl();
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

}
