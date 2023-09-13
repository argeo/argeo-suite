package org.argeo.app.geo;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import jakarta.json.stream.JsonGenerator;

/**
 * GeoJSon format.
 * 
 * @see https://datatracker.ietf.org/doc/html/rfc7946
 */
public class GeoJSon {
	public static void writeBBox(JsonGenerator generator, Geometry geometry) {
		generator.writeStartArray("bbox");
		Envelope envelope = geometry.getEnvelopeInternal();
		generator.write(envelope.getMinX());
		generator.write(envelope.getMinY());
		generator.write(envelope.getMaxX());
		generator.write(envelope.getMaxY());
		generator.writeEnd();
	}

	public static void writeGeometry(JsonGenerator generator, Geometry geometry) {
		generator.writeStartObject("geometry");
		if (geometry instanceof Point point) {
			generator.write("type", "Point");
			generator.writeStartArray("coordinates");
			writeCoordinate(generator, point.getCoordinate());
			generator.writeEnd();// coordinates array
		} else if (geometry instanceof LineString lineString) {
			generator.write("type", "LineString");
			generator.writeStartArray("coordinates");
			writeCoordinates(generator, lineString.getCoordinates());
			generator.writeEnd();// coordinates array
		} else if (geometry instanceof Polygon polygon) {
			generator.write("type", "Polygon");
			generator.writeStartArray("coordinates");
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
		generator.writeEnd();// geometry object
	}

	public static void writeCoordinates(JsonGenerator generator, Coordinate[] coordinates) {
		for (Coordinate coordinate : coordinates) {
			generator.writeStartArray();
			writeCoordinate(generator, coordinate);
			generator.writeEnd();
		}
	}

	public static void writeCoordinate(JsonGenerator generator, Coordinate coordinate) {
		generator.write(coordinate.getX());
		generator.write(coordinate.getY());
		double z = coordinate.getZ();
		if (!Double.isNaN(z)) {
			generator.write(z);
		}
	}

	/** singleton */
	private GeoJSon() {
	}

}
