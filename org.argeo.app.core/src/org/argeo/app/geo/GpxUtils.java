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

import org.geotools.data.DataUtilities;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/** Utilities around the GPX format. */
public class GpxUtils {

	public static SimpleFeature parseGpxToPolygon(InputStream in) throws IOException {
		try {
			final SimpleFeatureType TYPE = DataUtilities.createType("Area", "the_geom:Polygon:srid=4326");
			SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);

			GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
			List<Coordinate> coordinates = new ArrayList<>();
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();

			saxParser.parse(in, new DefaultHandler() {

				@Override
				public void startElement(String uri, String localName, String qName, Attributes attributes)
						throws SAXException {
					if ("trkpt".equals(qName)) {
						Double latitude = Double.parseDouble(attributes.getValue("lat"));
						Double longitude = Double.parseDouble(attributes.getValue("lon"));
						Coordinate coordinate = new Coordinate(longitude, latitude);
						coordinates.add(coordinate);
					}
				}

			});
			// close the line string
			coordinates.add(coordinates.get(0));

			Polygon polygon = geometryFactory.createPolygon(coordinates.toArray(new Coordinate[coordinates.size()]));
			featureBuilder.add(polygon);
			SimpleFeature area = featureBuilder.buildFeature(null);
			return area;
		} catch (ParserConfigurationException | SAXException | SchemaException e) {
			throw new RuntimeException("Cannot convert GPX", e);
		}
	}

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
