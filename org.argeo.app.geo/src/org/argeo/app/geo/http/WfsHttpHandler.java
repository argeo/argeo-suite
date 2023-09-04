package org.argeo.app.geo.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.xml.namespace.QName;

import org.argeo.api.acr.Content;
import org.argeo.api.acr.ContentSession;
import org.argeo.api.acr.NamespaceUtils;
import org.argeo.api.acr.ldap.LdapAttr;
import org.argeo.api.acr.spi.ProvidedRepository;
import org.argeo.app.api.EntityName;
import org.argeo.app.api.EntityType;
import org.argeo.app.api.WGS84PosName;
import org.argeo.app.geo.CqlUtils;
import org.argeo.app.geo.GpxUtils;
import org.argeo.cms.http.HttpHeader;
import org.argeo.cms.http.server.HttpServerUtils;
import org.geotools.data.DataUtilities;
import org.geotools.data.geojson.GeoJSONWriter;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/** A partially implemented WFS 2.0 server. */
public class WfsHttpHandler implements HttpHandler {
	private ProvidedRepository contentRepository;

	// HTTP parameters
	final static String OUTPUT_FORMAT = "outputFormat";
	final static String TYPE_NAMES = "typeNames";
	final static String CQL_FILTER = "cql_filter";

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String path = HttpServerUtils.subPath(exchange);
		ContentSession session = HttpServerUtils.getContentSession(contentRepository, exchange);
		// Content content = session.get(path);

		Map<String, List<String>> parameters = HttpServerUtils.parseParameters(exchange);
		String cql = parameters.containsKey(CQL_FILTER) ? parameters.get(CQL_FILTER).get(0) : null;
		String typeNamesStr = parameters.containsKey(TYPE_NAMES) ? parameters.get(TYPE_NAMES).get(0) : null;
		String outputFormat = parameters.containsKey(OUTPUT_FORMAT) ? parameters.get(OUTPUT_FORMAT).get(0) : null;
		if (outputFormat == null) {
			outputFormat = "application/json";
		}

		switch (outputFormat) {
		case "application/json" -> {
			exchange.getResponseHeaders().set(HttpHeader.CONTENT_TYPE.getHeaderName(), "application/json");
		}

		default -> throw new IllegalArgumentException("Unexpected value: " + outputFormat);
		}

		QName[] typeNames;
		if (typeNamesStr != null) {
			String[] arr = typeNamesStr.split(",");
			typeNames = new QName[arr.length];
			for (int i = 0; i < arr.length; i++) {
				typeNames[i] = NamespaceUtils.parsePrefixedName(arr[i]);
			}
		} else {
			typeNames = new QName[] { EntityType.local.qName() };
		}

		if (cql != null) {
			Stream<Content> res = session.search((search) -> {
				CqlUtils.filter(search.from(path), cql);
				search.getWhere().any((f) -> {
					for (QName typeName : typeNames)
						f.isContentClass(typeName);
				});
			});

			exchange.sendResponseHeaders(200, 0);

			// BODY PROCESSING
			GeoJSONWriter geoJSONWriter = new GeoJSONWriter(exchange.getResponseBody());
			geoJSONWriter.setPrettyPrinting(true);

			boolean gpx = false;
			SimpleFeatureType TYPE;
			try {
				if (gpx)
					TYPE = DataUtilities.createType("Content", "the_geom:Polygon:srid=4326,path:String,type:String,name:String");
				else
					TYPE = DataUtilities.createType("Content", "the_geom:Point:srid=4326,path:String,type:String,name:String");
			} catch (SchemaException e) {
				throw new RuntimeException(e);
			}
			SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
			GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();

			res.forEach((c) -> {
				Geometry the_geom;
				if (gpx) {// experimental
					Content area = c.getContent("gpx/area.gpx").orElse(null);
					if (area == null)
						return;
					try (InputStream in = area.open(InputStream.class)) {
						SimpleFeature feature = GpxUtils.parseGpxToPolygon(in);
						the_geom = (Geometry) feature.getDefaultGeometry();
					} catch (IOException e) {
						throw new UncheckedIOException("Cannot parse " + c, e);
					}
				} else {
					if (!c.hasContentClass(EntityType.geopoint))
						return;

					double latitude = c.get(WGS84PosName.lat, Double.class).get();
					double longitude = c.get(WGS84PosName.lng, Double.class).get();

					Coordinate coordinate = new Coordinate(longitude, latitude);
					the_geom = geometryFactory.createPoint(coordinate);

				}

				featureBuilder.add(the_geom);
				String pth = c.getPath();
				featureBuilder.add(pth);
				if (c.hasContentClass(EntityType.local)) {
					String type = c.attr(EntityName.type);
					featureBuilder.add(type);
				} else {
					List<QName> contentClasses = c.getContentClasses();
					if (!contentClasses.isEmpty()) {
						featureBuilder.add(NamespaceUtils.toPrefixedName(contentClasses.get(0)));
					}
				}
				featureBuilder.add(NamespaceUtils.toPrefixedName(c.getName()));

				String uuid = c.attr(LdapAttr.entryUUID);

				SimpleFeature feature = featureBuilder.buildFeature(uuid);
				try {
					geoJSONWriter.write(feature);
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			});
			geoJSONWriter.close();
		}

	}

	public void setContentRepository(ProvidedRepository contentRepository) {
		this.contentRepository = contentRepository;
	}
}
