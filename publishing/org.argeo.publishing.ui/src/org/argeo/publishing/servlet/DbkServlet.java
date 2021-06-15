package org.argeo.publishing.servlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

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

import org.apache.xalan.processor.TransformerFactoryImpl;
import org.argeo.cms.servlet.ServletAuthUtils;
import org.argeo.jcr.Jcr;
import org.argeo.jcr.JcrException;
import org.w3c.dom.Document;

/**
 * A servlet transforming a dbk:* JCR node into HTML, using the DocBook XSL.
 */
public class DbkServlet extends HttpServlet {
	private static final long serialVersionUID = 6906020513498289335L;

	private Repository repository;

	private DocumentBuilderFactory documentBuilderFactory;
	private TransformerFactory transformerFactory;
	private Templates docBoookTemplates;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO customise DocBook so that it outputs UTF-8
		// see http://www.sagehill.net/docbookxsl/OutputEncoding.html
		resp.setContentType("text/html; charset=ISO-8859-1");

		Session session = null;
		try {
			session = ServletAuthUtils.doAs(() -> Jcr.login(repository, null), req);

			String pathInfo = req.getPathInfo();
			if (pathInfo.startsWith("//"))
				pathInfo = pathInfo.substring(1);
			String path = URLDecoder.decode(pathInfo, StandardCharsets.UTF_8);

			// TODO optimise with pipes, SAX, etc. ?

			byte[] arr;
			try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
				session.exportDocumentView(path, out, true, false);
				arr = out.toByteArray();
//				System.out.println(new String(arr, StandardCharsets.UTF_8));
			} catch (RepositoryException e) {
				throw new JcrException(e);
			}

			try (InputStream in = new ByteArrayInputStream(arr);
//					ByteArrayOutputStream out = new ByteArrayOutputStream();
			) {

				Result xmlOutput = new StreamResult(resp.getOutputStream());

				DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
//				Document doc = docBuilder.parse(new File(
//						System.getProperty("user.home") + "/dev/git/gpl/argeo-qa/doc/platform/argeo-platform.dbk.xml"));
				Document doc = docBuilder.parse(in);
				Source xmlInput = new DOMSource(doc);

				Transformer transformer = docBoookTemplates.newTransformer();
				transformer.transform(xmlInput, xmlOutput);
//				resp.getOutputStream().write(out.toByteArray());
			} catch (Exception e) {
				throw new ServletException("Cannot transform " + path, e);
			}
		} finally {
			Jcr.logout(session);
		}
	}

	@Override
	public void init() throws ServletException {

		// TODO improve configuration and provisioning of DocBook XSL
		String xslBase = System.getProperty("argeo.docbook.xsl");
		if (xslBase == null) {
			String defaultXslBase = "/opt/docbook-xsl";
			if (!Files.exists(Paths.get(defaultXslBase))) {
				throw new ServletException("System property argeo.docbook.xsl is not set and default location "
						+ defaultXslBase + " does not exist.");
			} else {
				xslBase = defaultXslBase;
			}
		}
		String xsl = xslBase + "/html/docbook.xsl";

		documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setXIncludeAware(true);
		documentBuilderFactory.setNamespaceAware(true);

		// We must explicitly use the non-XSLTC transformer, as XSLTC is not working
		// with DocBook stylesheets
		transformerFactory = new TransformerFactoryImpl();

		Source xslSource = new StreamSource(xsl);
		try {
			docBoookTemplates = transformerFactory.newTemplates(xslSource);
			if (docBoookTemplates == null)
				throw new ServletException("Could not instantiate XSL " + xsl);
		} catch (TransformerConfigurationException e) {
			throw new ServletException("Cannot instantiate XSL " + xsl, e);
		}
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

}
