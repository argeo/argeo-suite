package org.argeo.app.geo;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class GeoToolsTest {
	public GeoToolsTest() {

	}

	public void init() {
		try {
			main(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void destroy() {

	}

	public static void main(String[] args) throws Exception {
		final SimpleFeatureType TYPE = DataUtilities.createType("Location", "the_geom:Point:srid=4326," + // <- the
		// geometry
		// attribute:
		// Point
		// type
				"name:String," + // <- a String attribute
				"number:Integer" // a number attribute
		);
		final SimpleFeatureType TYPE_HULL = DataUtilities.createType("Hull", "the_geom:MultiPolygon:srid=4326");
		System.out.println("TYPE:" + TYPE);

		/*
		 * A list to collect features as we create them.
		 */
		List<SimpleFeature> features = new ArrayList<>();
		List<Coordinate> coordinates = new ArrayList<>();

		/*
		 * GeometryFactory will be used to create the geometry attribute of each
		 * feature, using a Point object for the location.
		 */
		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();

		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);

		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(GeoToolsTest.class.getResourceAsStream("/org/djapps/on/apaf/locations.csv")))) {
			/* First line of the data file is the header */
			String line = reader.readLine();
			System.out.println("Header: " + line);

			for (line = reader.readLine(); line != null; line = reader.readLine()) {
				if (line.trim().length() > 0) { // skip blank lines
					String[] tokens = line.split("\\,");

					double latitude = Double.parseDouble(tokens[0]);
					double longitude = Double.parseDouble(tokens[1]);
					String name = tokens[2].trim();
					int number = Integer.parseInt(tokens[3].trim());

					/* Longitude (= x coord) first ! */
					Coordinate coordinate = new Coordinate(longitude, latitude);
					coordinates.add(coordinate);
					Point point = geometryFactory.createPoint(coordinate);

					featureBuilder.add(point);
					featureBuilder.add(name);
					featureBuilder.add(number);
					SimpleFeature feature = featureBuilder.buildFeature(null);
					features.add(feature);
				}
			}
		}

		LineString lineString = geometryFactory
				.createLineString(coordinates.toArray(new Coordinate[coordinates.size()]));
		Geometry convexHull = lineString.convexHull();
		System.out.println(convexHull.toText());
		SimpleFeatureBuilder hullFeatureBuilder = new SimpleFeatureBuilder(TYPE_HULL);
		hullFeatureBuilder.add(convexHull);
		SimpleFeature hull = hullFeatureBuilder.buildFeature(null);

		/*
		 * Get an output file name and create the new shapefile
		 */
		File newFile = getNewShapeFile();

		ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

		Map<String, Serializable> params = new HashMap<>();
		params.put("url", newFile.toURI().toURL());
		params.put("create spatial index", Boolean.TRUE);

		ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);

		/*
		 * TYPE is used as a template to describe the file contents
		 */
		newDataStore.createSchema(TYPE_HULL);

		/*
		 * Write the features to the shapefile
		 */
		Transaction transaction = new DefaultTransaction("create");

		String typeName = newDataStore.getTypeNames()[0];
		SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);
		SimpleFeatureType SHAPE_TYPE = featureSource.getSchema();
		/*
		 * The Shapefile format has a couple limitations: - "the_geom" is always first,
		 * and used for the geometry attribute name - "the_geom" must be of type Point,
		 * MultiPoint, MuiltiLineString, MultiPolygon - Attribute names are limited in
		 * length - Not all data types are supported (example Timestamp represented as
		 * Date)
		 *
		 * Each data store has different limitations so check the resulting
		 * SimpleFeatureType.
		 */
		System.out.println("SHAPE:" + SHAPE_TYPE);

		if (featureSource instanceof SimpleFeatureStore) {
			SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
			/*
			 * SimpleFeatureStore has a method to add features from a
			 * SimpleFeatureCollection object, so we use the ListFeatureCollection class to
			 * wrap our list of features.
			 */
			SimpleFeatureCollection collection = new ListFeatureCollection(TYPE, Collections.singletonList(hull));
			featureStore.setTransaction(transaction);
			try {
				featureStore.addFeatures(collection);
				transaction.commit();
			} catch (Exception problem) {
				problem.printStackTrace();
				transaction.rollback();
			} finally {
				transaction.close();
			}
		} else {
			System.out.println(typeName + " does not support read/write access");
		}
//		if (featureSource instanceof SimpleFeatureStore) {
//			SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
//			/*
//			 * SimpleFeatureStore has a method to add features from a
//			 * SimpleFeatureCollection object, so we use the ListFeatureCollection class to
//			 * wrap our list of features.
//			 */
//			SimpleFeatureCollection collection = new ListFeatureCollection(TYPE, features);
//			featureStore.setTransaction(transaction);
//			try {
//				featureStore.addFeatures(collection);
//				transaction.commit();
//			} catch (Exception problem) {
//				problem.printStackTrace();
//				transaction.rollback();
//			} finally {
//				transaction.close();
//			}
//		} else {
//			System.out.println(typeName + " does not support read/write access");
//		}
	}

	/**
	 * Prompt the user for the name and path to use for the output shapefile
	 *
	 * @param csvFile the input csv file used to create a default shapefile name
	 * @return name and path for the shapefile as a new File object
	 */
	private static File getNewShapeFile() {
//        String path = csvFile.getAbsolutePath();
//        String newPath = path.substring(0, path.length() - 4) + ".shp";

		JFileDataStoreChooser chooser = new JFileDataStoreChooser("shp");
		chooser.setDialogTitle("Save shapefile");
//        chooser.setSelectedFile(new File(newPath));

		int returnVal = chooser.showSaveDialog(null);

		if (returnVal != JFileDataStoreChooser.APPROVE_OPTION) {
			// the user cancelled the dialog
			System.exit(0);
		}

		File newFile = chooser.getSelectedFile();

		return newFile;
	}

}
