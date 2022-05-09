package org.argeo.app.geo;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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

	public static SimpleFeature parseGpxToPolygon(InputStream in) {
		try {
			final SimpleFeatureType TYPE = DataUtilities.createType("Area", "the_geom:Polygon:srid=4326");
			SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);

			GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
			List<Coordinate> coordinates = new ArrayList<>();
			SAXParserFactory factory = SAXParserFactory.newInstance();
			Double[] startCoord = new Double[2];
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
		} catch (ParserConfigurationException | SAXException | IOException | SchemaException e) {
			throw new RuntimeException("Cannot convert GPX", e);
		}
	}

	/** Singleton. */
	private GpxUtils() {
	}
}
