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
import org.argeo.app.api.EntityName;
import org.argeo.app.api.EntityType;
import org.argeo.app.api.WGS84PosName;
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

/** Utilities around entity types related to geography. */
public class GeoEntityUtils {
	public static final String PLACE_GEOM_JSON = "place.geom.json";
	public static final String _GEOM_JSON = ".geom.json";

	public static void putGeometry(Content c, QNamed name, Geometry geometry) {
		putGeometry(c, name.qName(), geometry);
	}

	public static void putGeometry(Content c, QName name, Geometry geometry) {
		QName jsonFileName = new ContentName(name.getNamespaceURI(), name.getLocalPart() + _GEOM_JSON);
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
		updateBoundingBox(c);
	}

	public static <T extends Geometry> T getGeometry(Content c, QNamed name, Class<T> clss) {
		return getGeometry(c, name.qName(), clss);
	}

	public static <T extends Geometry> T getGeometry(Content c, QName name, Class<T> clss) {
		QName jsonFileName = new ContentName(name.getNamespaceURI(), name.getLocalPart() + _GEOM_JSON);
		Content geom = c.soleChild(jsonFileName).orElse(null);
		if (geom == null)
			return null;
		try (Reader in = new InputStreamReader(geom.open(InputStream.class), StandardCharsets.UTF_8)) {
			JsonReader jsonReader = Json.createReader(in);
			JsonObject jsonObject = jsonReader.readObject();
			T readGeom = GeoJson.readGeometry(jsonObject, clss);
			return readGeom;
		} catch (IOException e) {
			throw new UncheckedIOException("Cannot parse " + c, e);
		}
	}

	public static Point toPoint(Content c) {
		if (c.hasContentClass(EntityType.geopoint)) {
			Double lat = c.get(WGS84PosName.lat, Double.class).orElseThrow();
			Double lon = c.get(WGS84PosName.lon, Double.class).orElseThrow();
			Double alt = c.get(WGS84PosName.alt, Double.class).orElse(null);
			return JTS.GEOMETRY_FACTORY_WGS84
					.createPoint(alt != null ? new Coordinate(lat, lon, alt) : new Coordinate(lat, lon));
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

	/** singleton */
	private GeoEntityUtils() {
	}

}
