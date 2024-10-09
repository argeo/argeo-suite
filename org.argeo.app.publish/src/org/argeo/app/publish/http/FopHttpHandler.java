package org.argeo.app.publish.http;

import static org.argeo.cms.http.CommonMediaType.APPLICATION_PDF;
import static org.argeo.cms.http.CommonMediaType.APPLICATION_XML;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.xmlgraphics.io.Resource;
import org.apache.xmlgraphics.io.ResourceResolver;
import org.argeo.api.acr.Content;
import org.argeo.api.acr.ContentRepository;
import org.argeo.api.acr.ContentSession;
import org.argeo.api.acr.NamespaceUtils;
import org.argeo.app.geo.GeoUtils;
import org.argeo.app.geo.GpxUtils;
import org.argeo.app.geo.acr.GeoEntityUtils;
import org.argeo.cms.acr.xml.XmlNormalizer;
import org.argeo.cms.auth.RemoteAuthUtils;
import org.argeo.cms.http.server.HttpRemoteAuthExchange;
import org.argeo.cms.http.server.HttpServerUtils;
import org.argeo.cms.util.LangUtils;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import net.sf.saxon.BasicTransformerFactory;

/**
 * A servlet transforming an XML view of the data to either FOP or PDF.
 */
public class FopHttpHandler implements HttpHandler {
	private final static String PROP_ARGEO_FO_XSL = "argeo.fo.xsl";

	private ContentRepository contentRepository;

	private DocumentBuilderFactory documentBuilderFactory;
	private TransformerFactory transformerFactory;
	private Templates foTemplates;

	private URL xslUrl;

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String pathInfo = HttpServerUtils.subPath(exchange);
//		String servletPath = req.getServletPath();
		String ext = pathInfo.substring(pathInfo.lastIndexOf('.'));
		pathInfo = pathInfo.substring(0, pathInfo.lastIndexOf('.'));
		// servletPath = servletPath.substring(servletPath.indexOf('/'),
		// servletPath.length());
		String path = URLDecoder.decode(pathInfo, StandardCharsets.UTF_8);

		boolean pdf = ".pdf".equals(ext);
		ContentSession session = RemoteAuthUtils.doAs(() -> contentRepository.get(),
				new HttpRemoteAuthExchange(exchange));
		Content content = session.get(path);

		// dev only
		final boolean DEV = false;
		if (DEV) {
			try (InputStream in = xslUrl.openStream()) {
				Source xslSource = new StreamSource(in);
				foTemplates = transformerFactory.newTemplates(xslSource);
				if (foTemplates == null)
					throw new IllegalStateException("Could not instantiate XSL " + xslUrl);
			} catch (TransformerConfigurationException | IOException e) {
				throw new IllegalStateException("Cannot instantiate XSL " + xslUrl, e);
			}

			Source xmlInput = content.adapt(Source.class);
			XmlNormalizer.print(xmlInput, 0);
		}

		Source xmlInput = content.adapt(Source.class);
		String systemId = exchange.getRequestURI().toString();
		xmlInput.setSystemId(systemId);

		URIResolver uriResolver = (href, base) -> {
			try {
				URI url = URI.create(href);
				if (url.getScheme() != null) {
					if (url.getScheme().equals("file")) {
						InputStream in = Files.newInputStream(Paths.get(URI.create(url.toString())));
						return new StreamSource(in);
					}
					if (url.getScheme().equals("geo2svg")) {
						int lastDot = url.getPath().lastIndexOf('.');
						Polygon polygon;
						if (lastDot > 0) {
							String includePath = path + url.getPath();
							Content geoContent = session.get(includePath);
							String geoExt = includePath.substring(lastDot);
							if (".gpx".equals(geoExt)) {
								try (InputStream in = geoContent.open(InputStream.class)) {
									polygon = GpxUtils.parseGpxTrackTo(in, Polygon.class);
								}
							} else {
								throw new UnsupportedOperationException(geoExt + " is not supported");
							}
						} else {
							Content geoContent;
							String attrName;
							if (url.getPath().startsWith("/@")) {
								geoContent = content;
								attrName = url.getPath().substring(2);// remove /@
							} else {
								throw new IllegalArgumentException("Only direct attributes are currently supported");
							}
							polygon = GeoEntityUtils.getGeometry(geoContent, NamespaceUtils.parsePrefixedName(attrName),
									Polygon.class);
						}
						try (StringWriter writer = new StringWriter()) {
							GeoUtils.exportToSvg(new Geometry[] { polygon }, writer, 100, 100);
							StreamSource res = new StreamSource(new StringReader(writer.toString()));
							return res;
						}
					}
				}
			} catch (IOException e) {
				throw new RuntimeException("Cannot process " + href);
			}

			String p = href.startsWith("/") ? href : path + '/' + href;
			p = URLDecoder.decode(p, StandardCharsets.UTF_8);
			Content subContent = session.get(p);
			return subContent.adapt(Source.class);
		};

		ResourceResolver resourceResolver = new ResourceResolver() {

			@Override
			public Resource getResource(URI uri) throws IOException {
				String subPath = uri.getPath();
				Content subContent = session.get(subPath);
				InputStream in = subContent.open(InputStream.class);
				return new Resource(in);
			}

			@Override
			public OutputStream getOutputStream(URI uri) throws IOException {
				return null;
			}
		};

		try {
			if (pdf) {
				FopFactoryBuilder builder = new FopFactoryBuilder(exchange.getRequestURI(), resourceResolver);
				FopFactory fopFactory = builder.build();
//				FopFactory fopFactory = FopFactory.newInstance(URI.create(req.getRequestURL().toString()));
				HttpServerUtils.setContentType(exchange, APPLICATION_PDF);
				try (OutputStream out = HttpServerUtils.sendResponse(exchange)) {
					Fop fop = fopFactory.newFop(APPLICATION_PDF.get(), out);
					Transformer transformer = foTemplates.newTransformer();
					transformer.setURIResolver(uriResolver);
					Result fopResult = new SAXResult(fop.getDefaultHandler());
					transformer.transform(xmlInput, fopResult);
				}
			} else {
				HttpServerUtils.setContentType(exchange, APPLICATION_XML);
				try (OutputStream out = HttpServerUtils.sendResponse(exchange)) {
					Result xmlOutput = new StreamResult(out);
					Transformer transformer = foTemplates.newTransformer();
//				transformer = transformerFactory.newTransformer();// identity
					transformer.setURIResolver(uriResolver);
					transformer.transform(xmlInput, xmlOutput);
				}
			}
		} catch (FOPException | IOException | TransformerException e) {
			throw new RuntimeException("Cannot process " + path, e);
		}

	}

	public void start(Map<String, Object> properties) {
		documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setXIncludeAware(true);
		documentBuilderFactory.setNamespaceAware(true);

		transformerFactory = new BasicTransformerFactory();
//		transformerFactory = TransformerFactory.newDefaultInstance();
		try {
			String xslStr = LangUtils.get(properties, PROP_ARGEO_FO_XSL);
			Objects.requireNonNull(xslStr);
			xslUrl = new URL(xslStr);
			try (InputStream in = xslUrl.openStream()) {
				Source xslSource = new StreamSource(in);
				foTemplates = transformerFactory.newTemplates(xslSource);
				if (foTemplates == null)
					throw new IllegalStateException("Could not instantiate XSL " + xslUrl);
			}

		} catch (TransformerConfigurationException | IOException e) {
			throw new IllegalStateException("Cannot instantiate XSL " + xslUrl, e);
		}
	}

	public void stop(Map<String, Object> properties) {

	}

	public void setContentRepository(ContentRepository contentRepository) {
		this.contentRepository = contentRepository;
	}

}
