package org.argeo.app.geo.acr;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;

import org.argeo.api.acr.Content;
import org.argeo.api.acr.ContentName;
import org.argeo.api.acr.DName;
import org.argeo.api.acr.QNamed;
import org.argeo.api.app.EntityName;
import org.argeo.api.app.EntityType;
import org.argeo.api.app.WGS84PosName;
import org.argeo.api.cms.CmsLog;
import org.argeo.app.geo.GeoJson;
import org.argeo.app.geo.JTS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.Point;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonParsingException;

/** Utilities around entity types related to geography. */
public class GeoEntityUtils {
	private final static CmsLog log = CmsLog.getLog(GeoEntityUtils.class);

	public static final String _GEOM_JSON = ".geom.json";

	public static void putGeometry(Content c, QNamed name, Geometry geometry) {
		putGeometry(c, name.qName(), geometry);
	}

	public static void putGeometry(Content c, QName name, Geometry geometry) {
		QName jsonFileName = getJsonFileName(name);
		Content geom = c.soleChild(jsonFileName).orElseGet(
				() -> c.add(jsonFileName, Collections.singletonMap(DName.getcontenttype.qName(), "application/json")));
		try (OutputStream out = geom.open(OutputStream.class)) {
			JsonGenerator g = Json.createGenerator(out);
			g.writeStartObject();
			GeoJson.writeGeometry(g, geometry);
			g.writeEnd();
			g.close();
		} catch (IOException e) {
			throw new UncheckedIOException("Cannot add geometry " + name + " to " + c, e);
		}

//		try (BufferedReader in = new BufferedReader(
//				new InputStreamReader(geom.open(InputStream.class), StandardCharsets.UTF_8))) {
//			System.out.println(in.readLine());
//		} catch (IOException e) {
//			throw new UncheckedIOException("Cannot parse " + c, e);
//		}
		updateBoundingBox(c);
	}

	public static boolean hasGeometry(Content c, QNamed name) {
		return hasGeometry(c, name.qName());
	}

	public static boolean hasGeometry(Content c, QName name) {
		QName jsonFileName = getJsonFileName(name);
		return c.hasChild(jsonFileName);
	}

	public static <T extends Geometry> T getGeometry(Content c, QNamed name, Class<T> clss) {
		return getGeometry(c, name.qName(), clss);
	}

	public static <T extends Geometry> T getGeometry(Content c, QName name, Class<T> clss) {
		QName jsonFileName = getJsonFileName(name);
		Content geom = c.soleChild(jsonFileName).orElse(null);
		if (geom == null)
			return null;
//		try (Reader in = new InputStreamReader(geom.open(InputStream.class), StandardCharsets.UTF_8)) {
//			String json = StreamUtils.toString(new BufferedReader(in));
//			System.out.println("JSON:\n" + json);
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		try (Reader in = new InputStreamReader(geom.open(InputStream.class), StandardCharsets.UTF_8)) {
			JsonReader jsonReader = Json.createReader(in);
			JsonObject jsonObject = jsonReader.readObject();
			T readGeom = GeoJson.readGeometry(jsonObject, clss);
			return readGeom;
		} catch (IOException e) {
			throw new UncheckedIOException("Cannot parse " + c, e);
		} catch (JsonParsingException e) {
			if (log.isTraceEnabled())
				log.warn("Invalid GeoJson for " + geom);
			// json is invalid, returning null
			return null;
		}
	}

	private static QName getJsonFileName(QName name) {
		QName jsonFileName = new ContentName(name.getNamespaceURI(), name.getLocalPart() + _GEOM_JSON);
		return jsonFileName;
	}

	public static Point toPoint(Content c) {
		if (c.containsKey(WGS84PosName.lon) && c.containsKey(WGS84PosName.lat)) {
			Double lat = c.get(WGS84PosName.lat, Double.class).orElseThrow();
			Double lon = c.get(WGS84PosName.lon, Double.class).orElseThrow();
			return JTS.GEOMETRY_FACTORY_WGS84.createPoint(new Coordinate(lat, lon));
//			Double alt = c.get(WGS84PosName.alt, Double.class).orElse(null);
//			return JTS.GEOMETRY_FACTORY_WGS84
//					.createPoint(alt != null ? new Coordinate(lat, lon, alt) : new Coordinate(lat, lon));
		}
		return null;
	}

	public static GeometryCollection getGeometries(Content entity) {
		List<Geometry> geoms = new ArrayList<>();
		Point geoPoint = toPoint(entity);
		if (geoPoint != null)
			geoms.add(geoPoint);

		Geometry place = getGeometry(entity, EntityName.place.qName(), Geometry.class);
		if (place != null)
			geoms.add(place);

		if (geoms.isEmpty())
			return null;
		GeometryCollection geometryCollection = JTS.GEOMETRY_FACTORY_WGS84
				.createGeometryCollection(geoms.toArray(new Geometry[geoms.size()]));
		return geometryCollection;
	}

	public static void updateBoundingBox(Content entity) {
		GeometryCollection geometryCollection = getGeometries(entity);
		if (geometryCollection == null)
			return;
		entity.addContentClasses(EntityType.geobounded.qName());

		Envelope bbox = geometryCollection.getEnvelopeInternal();
		entity.put(EntityName.minLat, bbox.getMinX());
		entity.put(EntityName.minLon, bbox.getMinY());
		entity.put(EntityName.maxLat, bbox.getMaxX());
		entity.put(EntityName.maxLon, bbox.getMaxY());
	}

	public static void updateBoundingBox(Content entity, QName prop) {
		Geometry geom = getGeometry(entity, prop, Geometry.class);
		if (geom == null)
			return;
		entity.addContentClasses(EntityType.geobounded.qName());

		Envelope bbox = geom.getEnvelopeInternal();
		entity.put(EntityName.minLat, bbox.getMinX());
		entity.put(EntityName.minLon, bbox.getMinY());
		entity.put(EntityName.maxLat, bbox.getMaxX());
		entity.put(EntityName.maxLon, bbox.getMaxY());
	}

	/** singleton */
	private GeoEntityUtils() {
	}

}
