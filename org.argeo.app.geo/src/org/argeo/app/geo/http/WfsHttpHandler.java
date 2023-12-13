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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
import org.argeo.app.api.geo.WfsKvp;
import org.argeo.app.geo.CqlUtils;
import org.argeo.app.geo.GeoJson;
import org.argeo.app.geo.GeoUtils;
import org.argeo.app.geo.GpxUtils;
import org.argeo.app.geo.JTS;
import org.argeo.app.geo.acr.GeoEntityUtils;
import org.argeo.cms.acr.json.AcrJsonUtils;
import org.argeo.cms.auth.RemoteAuthUtils;
import org.argeo.cms.http.HttpHeader;
import org.argeo.cms.http.RemoteAuthHttpExchange;
import org.argeo.cms.http.server.HttpServerUtils;
import org.argeo.cms.util.LangUtils;
import org.geotools.api.feature.GeometryAttribute;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.feature.type.AttributeDescriptor;
import org.geotools.api.feature.type.Name;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.api.referencing.operation.TransformException;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.wfs.GML;
import org.geotools.wfs.GML.Version;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import jakarta.json.Json;
import jakarta.json.stream.JsonGenerator;

/** A partially implemented WFS 2.0 server. */
public class WfsHttpHandler implements HttpHandler {
	private final static CmsLog log = CmsLog.getLog(WfsHttpHandler.class);
	private ProvidedRepository contentRepository;

	private final Map<QName, FeatureAdapter> featureAdapters = new HashMap<>();

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		ContentSession session = HttpServerUtils.getContentSession(contentRepository, exchange);

		String path = HttpServerUtils.subPath(exchange);

		// content path
		final String pathToUse = path;
		String fileName = null;
		boolean zipped = false;
//		int lastSlash = path.lastIndexOf('/');
//		if (lastSlash > 0) {
//			fileName = path.substring(lastSlash + 1);
//		}
//		if (fileName != null) {
//			pathToUse = path.substring(0, lastSlash);
//			if (path.endsWith(".zip")) {
//				zipped = true;
//			}
//		} else {
//			pathToUse = path;
//		}

		Map<String, List<String>> parameters = HttpServerUtils.parseParameters(exchange);

		// PARAMETERS
		String cql = getKvpParameter(parameters, WfsKvp.CQL_FILTER);
		String typeNamesStr = getKvpParameter(parameters, WfsKvp.TYPE_NAMES);
		String outputFormat = getKvpParameter(parameters, WfsKvp.OUTPUT_FORMAT);
		if (outputFormat == null) {
			outputFormat = "application/json";
		}

		// TODO deal with multiple
		String formatOption = getKvpParameter(parameters, WfsKvp.FORMAT_OPTIONS);
		if (formatOption != null) {
			if (formatOption.startsWith(WfsKvp.FILENAME_))
				fileName = formatOption.substring(WfsKvp.FILENAME_.length());
		}
		if (fileName != null && fileName.endsWith(".zip"))
			zipped = true;

		// bbox
		String bboxStr = getKvpParameter(parameters, WfsKvp.BBOX);
		if (log.isTraceEnabled())
			log.trace(bboxStr);
		final Envelope bbox;
		if (bboxStr != null) {
			String srs;
			String[] arr = bboxStr.split(",");
			// TODO check SRS and convert to WGS84
			double minLat = Double.parseDouble(arr[0]);
			double minLon = Double.parseDouble(arr[1]);
			double maxLat = Double.parseDouble(arr[2]);
			double maxLon = Double.parseDouble(arr[3]);
			if (arr.length == 5) {
				srs = arr[4];
			} else {
				srs = null;
			}

			if (srs != null && !srs.equals(GeoUtils.EPSG_4326)) {
				try {
					// TODO optimise
					CoordinateReferenceSystem sourceCRS = CRS.decode(srs);
					CoordinateReferenceSystem targetCRS = CRS.decode(GeoUtils.EPSG_4326);
					MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, true);
					bbox = org.geotools.geometry.jts.JTS.transform(
							new Envelope(new Coordinate(minLat, minLon), new Coordinate(maxLat, maxLon)), transform);
				} catch (FactoryException | TransformException e) {
					throw new IllegalArgumentException("Cannot convert bounding box", e);
					// bbox = null;
				}
			} else {
				bbox = new Envelope(new Coordinate(minLat, minLon), new Coordinate(maxLat, maxLon));
			}
		} else {
			bbox = null;
		}

		// response headers
		exchange.getResponseHeaders().set(HttpHeader.DATE.getHeaderName(), Long.toString(System.currentTimeMillis()));

		if (fileName != null) {
			exchange.getResponseHeaders().set(HttpHeader.CONTENT_DISPOSITION.getHeaderName(),
					HttpHeader.ATTACHMENT + ";" + HttpHeader.FILENAME + "=\"" + fileName + "\"");

		}

		// content type
		if (zipped) {
			exchange.getResponseHeaders().set(HttpHeader.CONTENT_TYPE.getHeaderName(), "application/zip");

		} else {
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

		// QUERY
		Stream<Content> res = session.search((search) -> {
			if (cql != null) {
				CqlUtils.filter(search.from(pathToUse), cql);
			} else {
				search.from(pathToUse);
			}
			for (QName typeName : typeNames) {
				FeatureAdapter featureAdapter = featureAdapters.get(typeName);
				if (featureAdapter == null)
					throw new IllegalStateException("No feature adapter found for " + typeName);
				// f.isContentClass(typeName);
				RemoteAuthUtils.doAs(() -> {
					featureAdapter.addConstraintsForFeature((AndFilter) search.getWhere(), typeName);
					return null;
				}, new RemoteAuthHttpExchange(exchange));
			}

			if (bbox != null) {
				search.getWhere().any((or) -> {
					// box overlap, see
					// https://stackoverflow.com/questions/20925818/algorithm-to-check-if-two-boxes-overlap
					// isOverlapping = (x1min < x2max AND x2min < x1max AND y1min < y2max AND y2min
					// < y1max)
					// x1 = entity, x2 = bbox
					or.all((and) -> {
						and.lte(EntityName.minLat, bbox.getMaxX());
						and.gte(EntityName.maxLat, bbox.getMinX());
						and.lte(EntityName.minLon, bbox.getMaxY());
						and.gte(EntityName.maxLon, bbox.getMinY());
					});
					or.all((and) -> {
						and.gte(WGS84PosName.lat, bbox.getMinX());
						and.gte(WGS84PosName.lon, bbox.getMinY());
						and.lte(WGS84PosName.lat, bbox.getMaxX());
						and.lte(WGS84PosName.lon, bbox.getMaxY());
					});
				});
			}
		});

		exchange.sendResponseHeaders(200, 0);

		final int BUFFER_SIZE = 100 * 1024;
		try (OutputStream out = zipped ? new ZipOutputStream(exchange.getResponseBody())
				: new BufferedOutputStream(exchange.getResponseBody(), BUFFER_SIZE)) {
			if (out instanceof ZipOutputStream zipOut) {
				String unzippedFileName = fileName.substring(0, fileName.length() - ".zip".length());
				zipOut.putNextEntry(new ZipEntry(unzippedFileName));
			}

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
	protected String getKvpParameter(Map<String, List<String>> parameters, WfsKvp key) {
		Objects.requireNonNull(key, "KVP key cannot be null");
		// let's first try the default (CAML case) which should be more efficient
		List<String> values = parameters.get(key.getKey());
		if (values == null) {
			// then let's do an ignore case comparison of the key
			keys: for (String k : parameters.keySet()) {
				if (key.getKey().equalsIgnoreCase(k)) {
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

			boolean geometryWritten = false;
//			if (typeName.getLocalPart().equals("fieldSimpleFeature")) {
//				Content area = c.getContent("place.geom.json").orElse(null);
//				if (area != null) {
//					generator.writeStartObject();
//					generator.write("type", "Feature");
//					String featureId = getFeatureId(c);
//					if (featureId != null)
//						generator.write("id", featureId);
//
//					generator.flush();
//					try (InputStream in = area.open(InputStream.class)) {
//						out.write(",\"geometry\":".getBytes());
//						StreamUtils.copy(in, out);						
//						//out.flush();
//					} catch (Exception e) {
//						log.error(c.getPath() + " : " + e.getMessage());
//					} finally {
//					}
//					geometryWritten = true;
//				}else {
//					return;
//				}
//			}

			if (!geometryWritten) {

				Geometry defaultGeometry = featureAdapter != null ? featureAdapter.getDefaultGeometry(c, typeName)
						: getDefaultGeometry(c);
				if (defaultGeometry == null)
					return;
				generator.writeStartObject();
				generator.write("type", "Feature");
				String featureId = getFeatureId(c);
				if (featureId != null)
					generator.write("id", featureId);

				GeoJson.writeBBox(generator, defaultGeometry);
				generator.writeStartObject(GeoJson.GEOMETRY);
				GeoJson.writeGeometry(generator, defaultGeometry);
				generator.writeEnd();// geometry object
			}
			generator.writeStartObject(GeoJson.PROPERTIES);
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

		if (log.isTraceEnabled())
			log.trace("GeoJSon encoding took " + (System.currentTimeMillis() - begin) + " ms.");
	}

	protected Geometry getDefaultGeometry(Content content) {
		if (content.hasContentClass(EntityType.geopoint)) {
			return GeoEntityUtils.toPoint(content);
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
		List<String> typeNames = LangUtils.toStringList(properties.get(WfsKvp.TYPE_NAMES.getKey()));
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
		List<String> typeNames = LangUtils.toStringList(properties.get(WfsKvp.TYPE_NAMES.getKey()));
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
