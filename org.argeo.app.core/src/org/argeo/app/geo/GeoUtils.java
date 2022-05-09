package org.argeo.app.geo;

import javax.measure.Quantity;
import javax.measure.quantity.Area;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.simple.SimpleFeature;
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

	/** Singleton. */
	private GeoUtils() {
	}
}
