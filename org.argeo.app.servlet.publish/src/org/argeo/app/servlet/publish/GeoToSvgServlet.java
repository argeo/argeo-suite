package org.argeo.app.servlet.publish;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.argeo.api.acr.Content;
import org.argeo.api.acr.ContentRepository;
import org.argeo.api.acr.ContentSession;
import org.argeo.app.geo.GeoUtils;
import org.argeo.app.geo.GpxUtils;
import org.argeo.cms.auth.RemoteAuthUtils;
import org.argeo.cms.servlet.ServletHttpRequest;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;

/**
 * A servlet transforming an geographical data to SVG.
 */
public class GeoToSvgServlet extends HttpServlet {
	private static final long serialVersionUID = -6346379324580671894L;
	private ContentRepository contentRepository;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String servletPath = req.getServletPath();

		servletPath = servletPath.substring(1, servletPath.lastIndexOf('.'));
		servletPath = servletPath.substring(servletPath.indexOf('/'), servletPath.length());
		String path = URLDecoder.decode(servletPath, StandardCharsets.UTF_8);
		String ext = servletPath.substring(path.lastIndexOf('.'));

		resp.setContentType("image/svg+xml");

		ContentSession session = RemoteAuthUtils.doAs(() -> contentRepository.get(), new ServletHttpRequest(req));
		Content content = session.get(path);
		if (".gpx".equals(ext)) {
			try (InputStream in = content.open(InputStream.class)) {
				SimpleFeature field = GpxUtils.parseGpxToPolygon(in);

				SimpleFeatureCollection features = new ListFeatureCollection(field.getType(), field);
				GeoUtils.exportToSvg(features, resp.getWriter(), 100, 100);
//			log.debug("SVG:\n" + writer.toString() + "\n");
			}
		}
	}

	public void start(Map<String, Object> properties) {
	}

	public void stop(Map<String, Object> properties) {

	}

	public void setContentRepository(ContentRepository contentRepository) {
		this.contentRepository = contentRepository;
	}

}
