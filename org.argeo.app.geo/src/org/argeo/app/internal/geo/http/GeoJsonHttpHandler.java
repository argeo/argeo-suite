package org.argeo.app.internal.geo.http;

import static org.argeo.app.geo.CqlUtils.CQL_FILTER;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.argeo.api.acr.Content;
import org.argeo.api.acr.ContentSession;
import org.argeo.api.acr.NamespaceUtils;
import org.argeo.api.acr.ldap.LdapAttr;
import org.argeo.api.acr.spi.ProvidedRepository;
import org.argeo.app.api.EntityType;
import org.argeo.app.api.WGS84PosName;
import org.argeo.app.geo.CqlUtils;
import org.argeo.cms.http.HttpHeader;
import org.argeo.cms.http.server.HttpServerUtils;
import org.geotools.data.DataUtilities;
import org.geotools.data.geojson.GeoJSONWriter;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class GeoJsonHttpHandler implements HttpHandler {
	private ProvidedRepository contentRepository;

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String path = HttpServerUtils.subPath(exchange);
		ContentSession session = HttpServerUtils.getContentSession(contentRepository, exchange);
		// Content content = session.get(path);

		exchange.getResponseHeaders().set(HttpHeader.CONTENT_TYPE.getHeaderName(), "application/json; charset=utf-8");

		Map<String, List<String>> parameters = HttpServerUtils.parseParameters(exchange);
		String cql = parameters.containsKey(CQL_FILTER) ? parameters.get(CQL_FILTER).get(0) : null;

		if (cql != null) {
			Stream<Content> res = session.search((search) -> {
				CqlUtils.filter(search.from(path), cql);
				search.getWhere().isContentClass(EntityType.local);
			});

			GeoJSONWriter geoJSONWriter = new GeoJSONWriter(exchange.getResponseBody());
			geoJSONWriter.setPrettyPrinting(true);

//			Writer writer = new OutputStreamWriter(exchange.getResponseBody());
//			writer.write("""
//					{
//					  "type": "FeatureCollection",
//					  "features": [
//					""");

			SimpleFeatureType TYPE;
			try {
				TYPE = DataUtilities.createType("Content", "the_geom:Point:srid=4326,path:String,name:String");
			} catch (SchemaException e) {
				throw new RuntimeException(e);
			}
			SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
			GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();

			res.forEach((c) -> {
				if (!c.hasContentClass(EntityType.geopoint))
					return;

				double latitude = c.get(WGS84PosName.lat, Double.class).get();
				double longitude = c.get(WGS84PosName.lng, Double.class).get();

				Coordinate coordinate = new Coordinate(longitude, latitude);
				Point point = geometryFactory.createPoint(coordinate);

				featureBuilder.add(point);
				String pth = c.getPath();
				featureBuilder.add(pth);
				featureBuilder.add(NamespaceUtils.toPrefixedName(c.getName()));

				String uuid = c.attr(LdapAttr.entryUUID);

				SimpleFeature feature = featureBuilder.buildFeature(uuid);
//				String json = GeoJSONWriter.toGeoJSON(feature);
				try {
					geoJSONWriter.write(feature);
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			});
//			writer.write("""
//					  ]
//					}
//					""");
			geoJSONWriter.close();
		}

	}

	public void setContentRepository(ProvidedRepository contentRepository) {
		this.contentRepository = contentRepository;
	}

	public static void main(String[] args) throws Exception {
		Filter filter = CQL.toFilter("entity:type='apafField' AND jcr:isCheckedOut=false");
		System.out.println(CQL.toCQL(filter));
	}
}
