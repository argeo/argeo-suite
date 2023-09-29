package org.argeo.app.geo;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.stream.JsonGenerator;

/**
 * GeoJSon format.
 * 
 * @see https://datatracker.ietf.org/doc/html/rfc7946
 */
public class GeoJSon {
	public final static String POINT_TYPE = "Point";
	public final static String LINE_STRING_TYPE = "LineString";
	public final static String POLYGON_TYPE = "Polygon";

	public final static String TYPE = "type";
	public final static String GEOMETRY = "geometry";
	public final static String COORDINATES = "coordinates";
	public final static String BBOX = "bbox";
	public final static String PROPERTIES = "properties";

	/*
	 * WRITE
	 */
	/** Writes a {@link Geometry} as GeoJSON. */
	public static void writeGeometry(JsonGenerator generator, Geometry geometry) {
		if (geometry instanceof Point point) {
			generator.write(TYPE, POINT_TYPE);
			generator.writeStartArray(COORDINATES);
			writeCoordinate(generator, point.getCoordinate());
			generator.writeEnd();// coordinates array
		} else if (geometry instanceof LineString lineString) {
			generator.write(TYPE, LINE_STRING_TYPE);
			generator.writeStartArray(COORDINATES);
			writeCoordinates(generator, lineString.getCoordinates());
			generator.writeEnd();// coordinates array
		} else if (geometry instanceof Polygon polygon) {
			generator.write(TYPE, POLYGON_TYPE);
			generator.writeStartArray(COORDINATES);
			LinearRing exteriorRing = polygon.getExteriorRing();
			generator.writeStartArray();
			writeCoordinates(generator, exteriorRing.getCoordinates());
			generator.writeEnd();
			for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
				LinearRing interiorRing = polygon.getInteriorRingN(i);
				// TODO verify that holes are clockwise
				generator.writeStartArray();
				writeCoordinates(generator, interiorRing.getCoordinates());
				generator.writeEnd();
			}
			generator.writeEnd();// coordinates array
		}
	}

	/** Writes a sequence of coordinates [[lat,lon],[lat,lon]] */
	public static void writeCoordinates(JsonGenerator generator, Coordinate[] coordinates) {
		for (Coordinate coordinate : coordinates) {
			generator.writeStartArray();
			writeCoordinate(generator, coordinate);
			generator.writeEnd();
		}
	}

	/** Writes a pair of coordinates [lat,lon]. */
	public static void writeCoordinate(JsonGenerator generator, Coordinate coordinate) {
		generator.write(coordinate.getX());
		generator.write(coordinate.getY());
		double z = coordinate.getZ();
		if (!Double.isNaN(z)) {
			generator.write(z);
		}
	}

	/**
	 * Writes the {@link Envelope} of a {@link Geometry} as a bbox GeoJSON object.
	 */
	public static void writeBBox(JsonGenerator generator, Geometry geometry) {
		generator.writeStartArray(BBOX);
		Envelope envelope = geometry.getEnvelopeInternal();
		generator.write(envelope.getMinX());
		generator.write(envelope.getMinY());
		generator.write(envelope.getMaxX());
		generator.write(envelope.getMaxY());
		generator.writeEnd();
	}

	/*
	 * READ
	 */
	/** Reads a geometry from the geometry object of a GEoJSON feature. */
	@SuppressWarnings("unchecked")
	public static <T extends Geometry> T readGeometry(JsonObject geom, Class<T> clss) {
		String type = geom.getString(TYPE);
		JsonArray coordinates = geom.getJsonArray(COORDINATES);
		Geometry res = switch (type) {
		case POINT_TYPE: {
			Coordinate coord = readCoordinate(coordinates);
			yield JTS.GEOMETRY_FACTORY_WGS84.createPoint(coord);
		}
		case LINE_STRING_TYPE: {
			Coordinate[] coords = readCoordinates(coordinates);
			yield JTS.GEOMETRY_FACTORY_WGS84.createLineString(coords);
		}
		case POLYGON_TYPE: {
			assert coordinates.size() > 0;
			LinearRing exterior = JTS.GEOMETRY_FACTORY_WGS84
					.createLinearRing(readCoordinates(coordinates.getJsonArray(0)));
			LinearRing[] holes = new LinearRing[coordinates.size() - 1];
			for (int i = 0; i < coordinates.size() - 1; i++) {
				holes[i] = JTS.GEOMETRY_FACTORY_WGS84
						.createLinearRing(readCoordinates(coordinates.getJsonArray(i + 1)));
			}
			yield JTS.GEOMETRY_FACTORY_WGS84.createPolygon(exterior, holes);
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + type);
		};
//		res.normalize();
		return (T)res;
	}

	/** Reads a coordinate sequence [[lat,lon],[lat,lon]]. */
	public static Coordinate readCoordinate(JsonArray arr) {
		assert arr.size() >= 2;
		return new Coordinate(arr.getJsonNumber(0).doubleValue(), arr.getJsonNumber(1).doubleValue());
	}

	/** Reads a coordinate pair [lat,lon]. */
	public static Coordinate[] readCoordinates(JsonArray arr) {
		Coordinate[] coords = new Coordinate[arr.size()];
		for (int i = 0; i < arr.size(); i++)
			coords[i] = readCoordinate(arr.getJsonArray(i));
		return coords;
	}

	/** singleton */
	private GeoJSon() {
	}

}
