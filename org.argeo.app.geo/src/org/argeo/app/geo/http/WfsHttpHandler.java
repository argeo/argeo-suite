package org.argeo.app.geo.http;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import javax.xml.namespace.QName;

import org.argeo.api.acr.Content;
import org.argeo.api.acr.ContentSession;
import org.argeo.api.acr.NamespaceUtils;
import org.argeo.api.acr.ldap.LdapAttr;
import org.argeo.api.acr.search.AndFilter;
import org.argeo.api.acr.spi.ProvidedRepository;
import org.argeo.api.cms.CmsLog;
import org.argeo.app.api.EntityName;
import org.argeo.app.api.EntityType;
import org.argeo.app.api.WGS84PosName;
import org.argeo.app.api.geo.FeatureAdapter;
import org.argeo.app.geo.CqlUtils;
import org.argeo.app.geo.GeoJSon;
import org.argeo.app.geo.GpxUtils;
import org.argeo.app.geo.JTS;
import org.argeo.cms.acr.json.AcrJsonUtils;
import org.argeo.cms.http.HttpHeader;
import org.argeo.cms.http.server.HttpServerUtils;
import org.argeo.cms.util.LangUtils;
import org.geotools.data.DataUtilities;
import org.geotools.data.geojson.GeoJSONWriter;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.NameImpl;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.wfs.GML;
import org.geotools.wfs.GML.Version;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import jakarta.json.Json;
import jakarta.json.stream.JsonGenerator;

/** A partially implemented WFS 2.0 server. */
public class WfsHttpHandler implements HttpHandler {
	private final static CmsLog log = CmsLog.getLog(WfsHttpHandler.class);
	private ProvidedRepository contentRepository;

	// HTTP parameters
	final static String OUTPUT_FORMAT = "outputFormat";
	final static String TYPE_NAMES = "typeNames";
	final static String CQL_FILTER = "cql_filter";

	private final Map<QName, FeatureAdapter> featureAdapters = new HashMap<>();

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String path = HttpServerUtils.subPath(exchange);
		ContentSession session = HttpServerUtils.getContentSession(contentRepository, exchange);
		// Content content = session.get(path);

		Map<String, List<String>> parameters = HttpServerUtils.parseParameters(exchange);
		String cql = getKvpParameter(parameters, CQL_FILTER);
		String typeNamesStr = getKvpParameter(parameters, TYPE_NAMES);
		String outputFormat = getKvpParameter(parameters, OUTPUT_FORMAT);
		if (outputFormat == null) {
			outputFormat = "application/json";
		}

		switch (outputFormat) {
		case "application/json" -> {
			exchange.getResponseHeaders().set(HttpHeader.CONTENT_TYPE.getHeaderName(), "application/json");
		}
		case "GML3" -> {
//			exchange.getResponseHeaders().set(HttpHeader.CONTENT_TYPE.getHeaderName(), "application/gml+xml");
			exchange.getResponseHeaders().set(HttpHeader.CONTENT_TYPE.getHeaderName(), "application/xml");
		}

		default -> throw new IllegalArgumentException("Unexpected value: " + outputFormat);
		}

		List<QName> typeNames = new ArrayList<>();
		if (typeNamesStr != null) {
			String[] arr = typeNamesStr.split(",");
			for (int i = 0; i < arr.length; i++) {
				typeNames.add(NamespaceUtils.parsePrefixedName(arr[i]));
			}
		} else {
			typeNames.add(EntityType.local.qName());
		}

		if (typeNames.size() > 1)
			throw new UnsupportedOperationException("Only one type name is currently supported");

		Stream<Content> res = session.search((search) -> {
			if (cql != null) {
				CqlUtils.filter(search.from(path), cql);
			} else {
				search.from(path).where((and) -> {
				});
			}
//			search.getWhere().any((f) -> {
			for (QName typeName : typeNames) {
				FeatureAdapter featureAdapter = featureAdapters.get(typeName);
				if (featureAdapter == null)
					throw new IllegalStateException("No feature adapter found for " + typeName);
				// f.isContentClass(typeName);
				featureAdapter.addConstraintsForFeature((AndFilter) search.getWhere(), typeName);
			}
//			});
		});

		exchange.sendResponseHeaders(200, 0);

		final int BUFFER_SIZE = 100 * 1024;
		try (BufferedOutputStream out = new BufferedOutputStream(exchange.getResponseBody(), BUFFER_SIZE)) {
			if ("GML3".equals(outputFormat)) {
				encodeCollectionAsGML(res, out);
			} else if ("application/json".equals(outputFormat)) {
				encodeCollectionAsGeoJSon(res, out, typeNames);
			}
		}
	}

	/**
	 * Retrieve KVP (keyword-value pairs) parameters, which are lower case, as per
	 * specifications.
	 * 
	 * @see https://docs.ogc.org/is/09-025r2/09-025r2.html#19
	 */
	protected String getKvpParameter(Map<String, List<String>> parameters, String key) {
		Objects.requireNonNull(key, "KVP key cannot be null");
		// let's first try the default (CAML case) which should be more efficient
		List<String> values = parameters.get(key);
		if (values == null) {
			// then let's do an ignore case comparison of the key
			keys: for (String k : parameters.keySet()) {
				if (key.equalsIgnoreCase(k)) {
					values = parameters.get(k);
					break keys;
				}
			}
		}
		if (values == null) // nothing was found
			return null;
		if (values.size() != 1) {
			// although not completely clear from the standard, we assume keys must be
			// unique
			// since lists are defined here
			// https://docs.ogc.org/is/09-026r2/09-026r2.html#10
			throw new IllegalArgumentException("Key " + key + " as multiple values");
		}
		String value = values.get(0);
		assert value != null;
		return value;
	}

	protected void encodeCollectionAsGeoJSon(Stream<Content> features, OutputStream out, List<QName> typeNames)
			throws IOException {
		long begin = System.currentTimeMillis();
		AtomicLong count = new AtomicLong(0);
		JsonGenerator generator = Json.createGenerator(out);
		generator.writeStartObject();
		generator.write("type", "FeatureCollection");
		generator.writeStartArray("features");
		features.forEach((c) -> {
			// TODO deal with multiple type names
			FeatureAdapter featureAdapter = null;
			QName typeName = null;
			if (!typeNames.isEmpty()) {
				typeName = typeNames.get(0);
				featureAdapter = featureAdapters.get(typeName);
			}

			Geometry defaultGeometry = featureAdapter != null ? featureAdapter.getDefaultGeometry(c, typeName)
					: getDefaultGeometry(c);
			if (defaultGeometry == null)
				return;
			generator.writeStartObject();
			generator.write("type", "Feature");
			String featureId = getFeatureId(c);
			if (featureId != null)
				generator.write("id", featureId);
			GeoJSon.writeBBox(generator, defaultGeometry);
			GeoJSon.writeGeometry(generator, defaultGeometry);

			generator.writeStartObject("properties");
			AcrJsonUtils.writeTimeProperties(generator, c);
			if (featureAdapter != null)
				featureAdapter.writeProperties(generator, c, typeName);
			else
				writeProperties(generator, c);
			generator.writeEnd();// properties object

			generator.writeEnd();// feature object

			if (count.incrementAndGet() % 10 == 0)
				try {
					out.flush();
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
		});
		generator.writeEnd();// features array
		generator.writeEnd().close();

		log.debug("GeoJSon encoding took " + (System.currentTimeMillis() - begin) + " ms.");
	}

	protected Geometry getDefaultGeometry(Content content) {
		if (content.hasContentClass(EntityType.geopoint)) {
			double latitude = content.get(WGS84PosName.lat, Double.class).get();
			double longitude = content.get(WGS84PosName.lon, Double.class).get();

			Coordinate coordinate = new Coordinate(longitude, latitude);
			Point the_geom = JTS.GEOMETRY_FACTORY.createPoint(coordinate);
			return the_geom;
		}
		return null;
	}

	protected String getFeatureId(Content content) {
		String uuid = content.attr(LdapAttr.entryUUID);
		return uuid;
	}

	public void writeProperties(JsonGenerator generator, Content content) {
		String path = content.getPath();
		generator.write("path", path);
		if (content.hasContentClass(EntityType.local)) {
			String type = content.attr(EntityName.type);
			generator.write("type", type);
		} else {
			List<QName> contentClasses = content.getContentClasses();
			if (!contentClasses.isEmpty()) {
				generator.write("type", NamespaceUtils.toPrefixedName(contentClasses.get(0)));
			}
		}

	}

	protected void encodeCollectionAsGeoJSonOld(Stream<Content> features, OutputStream out) throws IOException {

		// BODY PROCESSING
		try (GeoJSONWriter geoJSONWriter = new GeoJSONWriter(out)) {
			geoJSONWriter.setPrettyPrinting(true);
			geoJSONWriter.setEncodeFeatureBounds(true);

			boolean gpx = true;
			SimpleFeatureType TYPE;
			try {
				if (gpx)
					TYPE = DataUtilities.createType("Content",
							"the_geom:Polygon:srid=4326,path:String,type:String,name:String");
				else
					TYPE = DataUtilities.createType("Content",
							"the_geom:Point:srid=4326,path:String,type:String,name:String");
			} catch (SchemaException e) {
				throw new RuntimeException(e);
			}
			SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
			GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();

			features.forEach((c) -> {
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
					double longitude = c.get(WGS84PosName.lon, Double.class).get();

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
		}
	}

	protected void encodeCollectionAsGML(Stream<Content> features, OutputStream out) throws IOException {
		String entityType = "entity";
		URL schemaLocation = getClass().getResource("/org/argeo/app/api/entity.xsd");
		String namespace = "http://www.argeo.org/ns/entity";

		GML gml = new GML(Version.WFS1_1);
		gml.setCoordinateReferenceSystem(DefaultGeographicCRS.WGS84);
		gml.setNamespace("local", namespace);

		SimpleFeatureType featureType = gml.decodeSimpleFeatureType(schemaLocation,
				new NameImpl(namespace, entityType + "Feature"));

//		CoordinateReferenceSystem crs=DefaultGeographicCRS.WGS84;
//		QName featureName = new QName(namespace,"apafFieldFeature");
//		GMLConfiguration configuration = new GMLConfiguration();
//		FeatureType parsed = GTXML.parseFeatureType(configuration, featureName, crs);
//		SimpleFeatureType featureType = DataUtilities.simple(parsed);

		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);

		DefaultFeatureCollection featureCollection = new DefaultFeatureCollection();

		features.forEach((c) -> {
//			boolean gpx = false;
			Geometry the_geom = null;
			Polygon the_area = null;
//			if (gpx) {
			Content area = c.getContent("gpx/area.gpx").orElse(null);
			if (area != null) {

				try (InputStream in = area.open(InputStream.class)) {
					the_area = GpxUtils.parseGpxTrackTo(in, Polygon.class);
				} catch (IOException e) {
					throw new UncheckedIOException("Cannot parse " + c, e);
				}
			}
//			} else {
			if (c.hasContentClass(EntityType.geopoint)) {
				double latitude = c.get(WGS84PosName.lat, Double.class).get();
				double longitude = c.get(WGS84PosName.lon, Double.class).get();

				Coordinate coordinate = new Coordinate(longitude, latitude);
				the_geom = JTS.GEOMETRY_FACTORY.createPoint(coordinate);
			}

//			}
			if (the_geom != null)
				featureBuilder.set(new NameImpl(namespace, "geopoint"), the_geom);
			if (the_area != null)
				featureBuilder.set(new NameImpl(namespace, "area"), the_area);

			List<AttributeDescriptor> attrDescs = featureType.getAttributeDescriptors();
			for (AttributeDescriptor attrDesc : attrDescs) {
				if (attrDesc instanceof GeometryAttribute)
					continue;
				Name name = attrDesc.getName();
				QName qName = new QName(name.getNamespaceURI(), name.getLocalPart());
				String value = c.attr(qName);
				if (value == null) {
					value = c.attr(name.getLocalPart());
				}
				if (value != null) {
					featureBuilder.set(name, value);
				}
			}

			String uuid = c.attr(LdapAttr.entryUUID);

			SimpleFeature feature = featureBuilder.buildFeature(uuid);
			featureCollection.add(feature);

		});
		gml.encode(out, featureCollection);
		out.close();

	}

	/*
	 * DEPENDENCY INJECTION
	 */

	public void addFeatureAdapter(FeatureAdapter featureAdapter, Map<String, Object> properties) {
		List<String> typeNames = LangUtils.toStringList(properties.get(TYPE_NAMES));
		if (typeNames.isEmpty()) {
			log.warn("FeatureAdapter " + featureAdapter.getClass() + " does not declare type names. Ignoring it...");
			return;
		}

		for (String tn : typeNames) {
			QName typeName = NamespaceUtils.parsePrefixedName(tn);
			featureAdapters.put(typeName, featureAdapter);
		}
	}

	public void removeFeatureAdapter(FeatureAdapter featureAdapter, Map<String, Object> properties) {
		List<String> typeNames = LangUtils.toStringList(properties.get(TYPE_NAMES));
		if (!typeNames.isEmpty()) {
			// ignore if noe type name declared
			return;
		}

		for (String tn : typeNames) {
			QName typeName = NamespaceUtils.parsePrefixedName(tn);
			featureAdapters.remove(typeName);
		}
	}

	public void setContentRepository(ProvidedRepository contentRepository) {
		this.contentRepository = contentRepository;
	}
}
