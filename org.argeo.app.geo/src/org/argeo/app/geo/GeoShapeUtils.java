package org.argeo.app.geo;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

/** Utilities around ODK's GeoShape format */
public class GeoShapeUtils {

	/** Converts a {@link Geometry} with WGS84 coordinates to a GeoShape. */
	public static String geometryToGeoShape(Geometry geometry) {
		if (geometry instanceof Point point) {
			Coordinate coordinate = point.getCoordinate();
			StringBuilder sb = new StringBuilder();
			appendToGeoShape(sb, coordinate.getX(), coordinate.getY(), coordinate.getZ());
			return sb.toString();
		} else if (geometry instanceof Polygon || geometry instanceof LineString) {
			StringBuilder sb = new StringBuilder();
			for (Coordinate coordinate : geometry.getCoordinates()) {
				appendToGeoShape(sb, coordinate.getX(), coordinate.getY(), coordinate.getZ());
				sb.append(';');
			}
			return sb.toString();
		} else {
			throw new IllegalArgumentException("Unsupported geometry " + geometry.getClass());
		}
	}

	public static String geoPointToGeoShape(double lon, double lat, double alt) {
		StringBuilder sb = new StringBuilder();
		appendToGeoShape(sb, lon, lat, alt);
		return sb.toString();
	}

	private static void appendToGeoShape(StringBuilder sb, double lon, double lat, double alt) {
		sb.append(lat).append(' ');
		sb.append(lon).append(' ');
		if (alt != Double.NaN)
			sb.append(alt).append(' ');
		else
			sb.append("0 ");
		sb.append('0');
	}

	/** singleton */
	private GeoShapeUtils() {
	}

}
