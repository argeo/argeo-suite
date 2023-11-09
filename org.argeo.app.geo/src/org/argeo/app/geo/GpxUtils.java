package org.argeo.app.geo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.data.DataUtilities;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.Polygon;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/** Utilities around the GPX format. */
public class GpxUtils {
	/** GPX as a LineString in WGS84 (feature type with only the_geom). */
	public static final SimpleFeatureType LINESTRING_FEATURE_TYPE;
	/** GPX as a Polygon in WGS84 (feature type with only the_geom). */
	public static final SimpleFeatureType POLYGON_FEATURE_TYPE;

	static {
		try {
			LINESTRING_FEATURE_TYPE = DataUtilities.createType("Area", "the_geom:LineString:srid=4326");
			POLYGON_FEATURE_TYPE = DataUtilities.createType("Area", "the_geom:Polygon:srid=4326");
		} catch (SchemaException e) {
			throw new RuntimeException("Cannot create GPX Feature type", e);
		}
	}

	/**
	 * Converts a GPX track to either a {@link Geometry} with WGS84 coordinates
	 * ({@link LineString} or {@link Polygon}) or a {@link SimpleFeature} (with
	 * {@link #LINESTRING_FEATURE_TYPE}).
	 */
	@SuppressWarnings("unchecked")
	public static <T> T parseGpxTrackTo(InputStream in, Class<T> clss) throws IOException {
		GeometryFactory geometryFactory = JTS.GEOMETRY_FACTORY_WGS84;
		List<Coordinate> coordinates = new ArrayList<>();
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();

			saxParser.parse(in, new DefaultHandler() {

				@Override
				public void startElement(String uri, String localName, String qName, Attributes attributes)
						throws SAXException {
					if ("trkpt".equals(qName)) {
						Double latitude = Double.parseDouble(attributes.getValue("lat"));
						Double longitude = Double.parseDouble(attributes.getValue("lon"));
						// TODO elevation in 3D context
						Coordinate coordinate = new Coordinate(latitude, longitude);
						coordinates.add(coordinate);
					}
				}

			});
		} catch (ParserConfigurationException | SAXException e) {
			throw new RuntimeException("Cannot convert GPX", e);
		}

		if (LineString.class.isAssignableFrom(clss)) {
			LineString lineString = geometryFactory
					.createLineString(coordinates.toArray(new Coordinate[coordinates.size()]));
			return (T) lineString;
		} else if (MultiPoint.class.isAssignableFrom(clss)) {
			MultiPoint multiPoint = geometryFactory
					.createMultiPointFromCoords(coordinates.toArray(new Coordinate[coordinates.size()]));
			// multiPoint.normalize();
			return (T) multiPoint;
		} else if (Polygon.class.isAssignableFrom(clss)) {
			Coordinate first = coordinates.get(0);
			Coordinate last = coordinates.get(coordinates.size() - 1);
			if (!(first.getX() == last.getX() && first.getY() == last.getY())) {
				// close the line string
				coordinates.add(first);
			}
			Polygon polygon = geometryFactory.createPolygon(coordinates.toArray(new Coordinate[coordinates.size()]));
			return (T) polygon;
		} else if (SimpleFeature.class.isAssignableFrom(clss)) {
			SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(LINESTRING_FEATURE_TYPE);
			LineString lineString = geometryFactory
					.createLineString(coordinates.toArray(new Coordinate[coordinates.size()]));
			featureBuilder.add(lineString);
			SimpleFeature area = featureBuilder.buildFeature(null);
			return (T) area;
		} else {
			throw new IllegalArgumentException("Unsupported format " + clss);
		}
	}

	/** @deprecated Use {@link #parseGpxTrackTo(InputStream, Class)} instead. */
	@Deprecated
	public static SimpleFeature parseGpxToPolygon(InputStream in) throws IOException {
		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(POLYGON_FEATURE_TYPE);
		Polygon polygon = parseGpxTrackTo(in, Polygon.class);
		featureBuilder.add(polygon);
		SimpleFeature area = featureBuilder.buildFeature(null);
		return area;
	}

	/** Write ODK GeoShape as a GPX file. */
	public static void writeGeoShapeAsGpx(String geoShape, OutputStream out) throws IOException {
		Objects.requireNonNull(geoShape);
		Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
		writer.append("<gpx><trk><trkseg>");
		StringTokenizer stSeg = new StringTokenizer(geoShape.trim(), ";");
		while (stSeg.hasMoreTokens()) {
			StringTokenizer stPt = new StringTokenizer(stSeg.nextToken().trim(), " ");
			String lat = stPt.nextToken();
			String lng = stPt.nextToken();
			String alt = stPt.nextToken();
			// String precision = stPt.nextToken();
			writer.append("<trkpt");
			writer.append(" lat=\"").append(lat).append('\"');
			writer.append(" lon=\"").append(lng).append('\"');
			if (!alt.equals("0.0")) {
				writer.append('>');
				writer.append("<ele>").append(alt).append("</ele>");
				writer.append("</trkpt>");
			} else {
				writer.append("/>");
			}
		}
		writer.append("</trkseg></trk></gpx>");
		writer.flush();
	}

	/** Singleton. */
	private GpxUtils() {
	}
}
