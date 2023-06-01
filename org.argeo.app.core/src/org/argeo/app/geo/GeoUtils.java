package org.argeo.app.geo;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.Quantity;
import javax.measure.quantity.Area;

import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import si.uom.SI;
import tech.units.indriya.quantity.Quantities;

/** Utilities around geographical format, mostly wrapping GeoTools patterns. */
public class GeoUtils {

	/** In square meters. */
	public static Quantity<Area> calcArea(SimpleFeature feature) {
		try {
			Polygon p = (Polygon) feature.getDefaultGeometry();
			Point centroid = p.getCentroid();
			String code = "AUTO:42001," + centroid.getX() + "," + centroid.getY();
			CoordinateReferenceSystem auto = CRS.decode(code);

			MathTransform transform = CRS.findMathTransform(DefaultGeographicCRS.WGS84, auto);

			Polygon projed = (Polygon) JTS.transform(p, transform);
			return Quantities.getQuantity(projed.getArea(), SI.SQUARE_METRE);
		} catch (MismatchedDimensionException | FactoryException | TransformException e) {
			throw new IllegalStateException("Cannot claculate area of feature");
		}
	}

	public static void exportToSvg(SimpleFeatureCollection features, Writer out, int width, int height) {
		try {
			double minY = Double.POSITIVE_INFINITY;
			double maxY = Double.NEGATIVE_INFINITY;
			double minX = Double.POSITIVE_INFINITY;
			double maxX = Double.NEGATIVE_INFINITY;
			List<String> shapes = new ArrayList<>();
			for (SimpleFeatureIterator it = features.features(); it.hasNext();) {
				SimpleFeature feature = it.next();
				StringBuffer sb = new StringBuffer();
				sb.append("<polyline style=\"stroke-width:1;stroke:#000000;fill:none;\" points=\"");

				Polygon p = (Polygon) feature.getDefaultGeometry();
				Point centroid = p.getCentroid();
				String code = "AUTO:42001," + centroid.getX() + "," + centroid.getY();
				CoordinateReferenceSystem auto = CRS.decode(code);

				MathTransform transform = CRS.findMathTransform(DefaultGeographicCRS.WGS84, auto);

				Polygon projed = (Polygon) JTS.transform(p, transform);

				for (Coordinate coord : projed.getCoordinates()) {
					double x = coord.x;
					if (x < minX)
						minX = x;
					if (x > maxX)
						maxX = x;
					double y = -coord.y;
					if (y < minY)
						minY = y;
					if (y > maxY)
						maxY = y;
					sb.append(x + "," + y + " ");
				}
				sb.append("\">");
				sb.append("</polyline>\n");
				shapes.add(sb.toString());

			}
			double viewportHeight = maxY - minY;
			double viewportWidth = maxX - minX;
			out.write("<svg xmlns=\"http://www.w3.org/2000/svg\"\n");
			out.write(" width=\"" + width + "\"\n");
			out.write(" height=\"" + height + "\"\n");
			out.write(" viewBox=\"" + minX + " " + minY + " " + viewportWidth + " " + viewportHeight + "\"\n");
			out.write(" preserveAspectRatio=\"xMidYMid meet\"\n");
			out.write(">\n");
			for (String shape : shapes) {
				out.write(shape);
				out.write("\n");
			}
			out.write("</svg>");
		} catch (IOException | FactoryException | MismatchedDimensionException | TransformException e) {
			throw new RuntimeException("Cannot export to SVG", e);
		}
	}

	/** Write a list of simple features to a shapefile. */
	public static void saveFeaturesAsShapefile(SimpleFeatureType featureType, List<SimpleFeature> features,
			Path shpFile) {
		try {
			ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

			Map<String, Serializable> params = new HashMap<>();
			params.put("url", shpFile.toUri().toURL());

			params.put("create spatial index", Boolean.TRUE);

			ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
			newDataStore.createSchema(featureType);

			String typeName = newDataStore.getTypeNames()[0];
			SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);
			if (featureSource instanceof SimpleFeatureStore) {
				SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
				SimpleFeatureCollection collection = new ListFeatureCollection(featureType, features);

				try (Transaction transaction = new DefaultTransaction("create")) {
					try {
						featureStore.setTransaction(transaction);
						featureStore.addFeatures(collection);
						transaction.commit();
					} catch (Exception problem) {
						transaction.rollback();
						throw new RuntimeException("Cannot write shapefile " + shpFile, problem);
					}
				}
			} else {
				throw new IllegalArgumentException(typeName + " does not support read/write access");
			}
		} catch (IOException e) {
			throw new RuntimeException("Cannot write shapefile " + shpFile, e);
		}
	}

	/** Singleton. */
	private GeoUtils() {
	}
}
