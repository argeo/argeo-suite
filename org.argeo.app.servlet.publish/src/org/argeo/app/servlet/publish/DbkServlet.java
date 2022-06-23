package org.argeo.app.servlet.publish;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
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
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.xalan.processor.TransformerFactoryImpl;
import org.argeo.api.cms.ux.CmsTheme;
import org.argeo.app.docbook.DbkType;
import org.argeo.app.docbook.DbkUtils;
import org.argeo.cms.auth.RemoteAuthUtils;
import org.argeo.cms.servlet.ServletHttpRequest;
import org.argeo.jcr.Jcr;
import org.argeo.jcr.JcrException;
import org.argeo.jcr.JcrUtils;
import org.w3c.dom.Document;

/**
 * A servlet transforming a dbk:* JCR node into HTML, using the DocBook XSL.
 */
public class DbkServlet extends HttpServlet {
	private static final long serialVersionUID = 6906020513498289335L;

	private Repository repository;

	private DocumentBuilderFactory documentBuilderFactory;
	private TransformerFactory transformerFactory;
	private Templates docBoookHtmlTemplates;

	// FOP
	private Templates docBoookFoTemplates;

	private Map<String, CmsTheme> themes = Collections.synchronizedMap(new HashMap<>());

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String pathInfo = req.getPathInfo();
		if (pathInfo.startsWith("//"))
			pathInfo = pathInfo.substring(1);
		String path = URLDecoder.decode(pathInfo, StandardCharsets.UTF_8);

		if (path.toLowerCase().endsWith(".css")) {
			path = path.substring(1);
			int firstSlash = path.indexOf('/');
			String themeId = path.substring(0, firstSlash);
			String cssPath = path.substring(firstSlash);
			CmsTheme cmsTheme = themes.get(themeId);
			if (cmsTheme == null)
				throw new IllegalArgumentException("Theme " + themeId + " not found.");
			resp.setContentType("text/css");
			IOUtils.copy(cmsTheme.getResourceAsStream(cssPath), resp.getOutputStream());
			return;
		}

		if (path.toLowerCase().endsWith("/index.html")) {
			path = path.substring(0, path.length() - "/index.html".length());
		}

		boolean pdf = false;
		if (path.toLowerCase().endsWith(".pdf")) {
			pdf = true;
			if (path.toLowerCase().endsWith("/index.pdf")) {
				path = path.substring(0, path.length() - "/index.pdf".length());
			}
		}

		Session session = null;
		try {
			session = RemoteAuthUtils.doAs(() -> Jcr.login(repository, null), new ServletHttpRequest(req));
			Node node = session.getNode(path);

			if (node.hasNode(DbkType.article.get())) {
				Node dbkNode = node.getNode(DbkType.article.get());
				if (DbkUtils.isDbk(dbkNode)) {
					CmsTheme cmsTheme = null;
					String themeId = req.getParameter("themeId");
					if (themeId != null) {
						cmsTheme = themes.get(themeId);
						if (cmsTheme == null)
							throw new IllegalArgumentException("Theme " + themeId + " not found.");
					}

					// TODO customise DocBook so that it outputs UTF-8
					// see http://www.sagehill.net/docbookxsl/OutputEncoding.html
					resp.setContentType("text/html; charset=ISO-8859-1");

					// TODO optimise with pipes, SAX, etc. ?
					byte[] arr;
					try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
						session.exportDocumentView(dbkNode.getPath(), out, true, false);
						arr = out.toByteArray();
					} catch (RepositoryException e) {
						throw new JcrException(e);
					}

					try (InputStream in = new ByteArrayInputStream(arr);) {

						DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
						Document doc = docBuilder.parse(in);
						Source xmlInput = new DOMSource(doc);

						if (pdf) {
							//String baseUri = req.getRequestURI();
							FopFactory fopFactory = FopFactory.newInstance(URI.create(req.getRequestURL().toString()));
							resp.setContentType("application/pdf");

//							// DocBook to FO
//							byte[] foBytes;
//							try (ByteArrayOutputStream out = new ByteArrayOutputStream();) {
//								Result xmlOutput = new StreamResult(out);
//								Transformer docBookTransformer = docBoookFoTemplates.newTransformer();
//								docBookTransformer.transform(xmlInput, xmlOutput);
//								foBytes = out.toByteArray();
//							}
//
//							// FO to PDF
//							try (InputStream foIn = new ByteArrayInputStream(foBytes)) {
//								Fop fop = fopFactory.newFop("application/pdf", resp.getOutputStream());
//								Transformer fopTransformer = transformerFactory.newTransformer(); // identity
//								Source src = new StreamSource(foIn);
//								Result fopResult = new SAXResult(fop.getDefaultHandler());
//								fopTransformer.transform(src, fopResult);
//							}
//
							
							Fop fop = fopFactory.newFop("application/pdf", resp.getOutputStream());
							Transformer docBookTransformer = docBoookFoTemplates.newTransformer();
							Result fopResult = new SAXResult(fop.getDefaultHandler());
							docBookTransformer.transform(xmlInput, fopResult);

						} else {
							Result xmlOutput = new StreamResult(resp.getOutputStream());
							resp.setContentType("text/html");
							Transformer docBookTransformer = docBoookHtmlTemplates.newTransformer();

							// gather CSS
							if (cmsTheme != null) {
								StringBuilder sb = new StringBuilder();
								for (String cssPath : cmsTheme.getWebCssPaths()) {
									sb.append(req.getContextPath()).append(req.getServletPath()).append('/');
									sb.append(themeId).append('/').append(cssPath).append(' ');
								}
								// FIXME make it more generic
								sb.append("https://fonts.googleapis.com/css2?family=Roboto:wght@400;700&display=swap")
										.append(' ');
								sb.append(
										"https://fonts.googleapis.com/css2?family=Montserrat:ital,wght@0,300;0,400;0,600;1,400&display=swap")
										.append(' ');
								if (sb.length() > 0)
									docBookTransformer.setParameter("html.stylesheet", sb.toString());
							}
							docBookTransformer.transform(xmlInput, xmlOutput);
						}
//				resp.getOutputStream().write(out.toByteArray());
					} catch (Exception e) {
						throw new ServletException("Cannot transform " + path, e);
					}
				}
			} else {
				if (node.isNodeType(NodeType.NT_FILE)) {// media download etc.
					String fileNameLowerCase = node.getName().toLowerCase();
					if (fileNameLowerCase.endsWith(".jpg") || fileNameLowerCase.endsWith(".jpeg")) {
						resp.setContentType("image/jpeg");
					} else if (fileNameLowerCase.endsWith(".png")) {
						resp.setContentType("image/png");
					} else if (fileNameLowerCase.endsWith(".gif")) {
						resp.setContentType("image/gif");
					} else if (fileNameLowerCase.endsWith(".svg")) {
						resp.setContentType("image/svg+xml");
					} else {
						// TODO know more content types...
						resp.setHeader("Content-Disposition", "attachment; filename=\"" + node.getName() + "\"");
					}
					IOUtils.copy(JcrUtils.getFileAsStream(node), resp.getOutputStream());
				} else {
					throw new IllegalArgumentException("Unsupported node " + node);
				}
			}
		} catch (RepositoryException e1) {
			throw new JcrException(e1);
		} finally {
			Jcr.logout(session);
		}
	}

	@Override
	public void init() throws ServletException {

		// TODO improve configuration and provisioning of DocBook XSL
		String xslBase = System.getProperty("argeo.docbook.xsl");
		if (xslBase == null) {
			// We need namespace aware XSL!
			// Fedora (sudo dnf install docbook5-style-xsl)
			String defaultXslBase = "/usr/share/xml/docbook/stylesheet/docbook-xsl-ns/";
			if (!Files.exists(Paths.get(defaultXslBase))) {
				defaultXslBase = "/opt/docbook-xsl";
				if (!Files.exists(Paths.get(defaultXslBase))) {
					throw new ServletException("System property argeo.docbook.xsl is not set and default location "
							+ defaultXslBase + " does not exist.");
				}
			}
			xslBase = defaultXslBase;

		}

		documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setXIncludeAware(true);
		documentBuilderFactory.setNamespaceAware(true);

		// We must explicitly use the non-XSLTC transformer, as XSLTC is not working
		// with DocBook stylesheets
		transformerFactory = new TransformerFactoryImpl();

		String htmlXsl = xslBase + "/html/docbook.xsl";
		docBoookHtmlTemplates = createDocBookTemplates(htmlXsl);
		String foXsl = xslBase + "/fo/docbook.xsl";
		docBoookFoTemplates = createDocBookTemplates(foXsl);
	}

	protected Templates createDocBookTemplates(String xsl) {
		try {
			Source xslSource = new StreamSource(xsl);
			Templates templates = transformerFactory.newTemplates(xslSource);
			if (templates == null)
				throw new IllegalStateException("Could not instantiate XSL " + xsl);
			return templates;
		} catch (TransformerConfigurationException e) {
			throw new IllegalStateException("Cannot instantiate XSL " + xsl, e);
		}

	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void addTheme(CmsTheme theme, Map<String, String> properties) {
		themes.put(theme.getThemeId(), theme);
	}

	public void removeTheme(CmsTheme theme, Map<String, String> properties) {
		themes.remove(theme.getThemeId());
	}

	public static void main(String[] args) throws Exception {
		// Step 1: Construct a FopFactory by specifying a reference to the configuration
		// file
		// (reuse if you plan to render multiple documents!)
		FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());

		// Step 2: Set up output stream.
		// Note: Using BufferedOutputStream for performance reasons (helpful with
		// FileOutputStreams).
		OutputStream out = new BufferedOutputStream(new FileOutputStream(new File(
				System.getProperty("user.home") + "/dev/git/unstable/argeo-qa/doc/platform/argeo-platform-test.pdf")));

		try {
			// Step 3: Construct fop with desired output format
			Fop fop = fopFactory.newFop("application/pdf", out);

			// Step 4: Setup JAXP using identity transformer
			TransformerFactory fopTransformerFactory = TransformerFactory.newInstance();
			Transformer fopTransformer = fopTransformerFactory.newTransformer(); // identity transformer

			// Step 5: Setup input and output for XSLT transformation
			// Setup input stream
			Source src = new StreamSource(new File(
					System.getProperty("user.home") + "/dev/git/unstable/argeo-qa/doc/platform/argeo-platform.fo"));

			// Resulting SAX events (the generated FO) must be piped through to FOP
			Result res = new SAXResult(fop.getDefaultHandler());

			// Step 6: Start XSLT transformation and FOP processing
			fopTransformer.transform(src, res);

		} finally {
			// Clean-up
			out.close();
		}
	}
}
