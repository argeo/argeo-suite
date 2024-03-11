package org.argeo.app.geo.acr;

import javax.xml.namespace.QName;

import org.argeo.api.acr.Content;
import org.argeo.api.app.EntityType;
import org.argeo.api.app.geo.FeatureAdapter;
import org.locationtech.jts.geom.Geometry;

public abstract class AbstractFeatureAdapter implements FeatureAdapter {
	@Override
	public Geometry getDefaultGeometry(Content c, QName targetFeature) {
		// TODO deal with more defaults
		// TODO deal with target feature
		if (c.hasContentClass(EntityType.geopoint)) {
			return getGeoPointGeometry(c);
		}
		return null;
	}

	protected Geometry getGeoPointGeometry(Content c) {
		if (c.hasContentClass(EntityType.geopoint)) {
			return GeoEntityUtils.toPoint(c);
//			double latitude = c.get(WGS84PosName.lat, Double.class).get();
//			double longitude = c.get(WGS84PosName.lon, Double.class).get();
//
//			Coordinate coordinate = new Coordinate(longitude, latitude);
//			Point the_geom = JTS.GEOMETRY_FACTORY.createPoint(coordinate);
//			return the_geom;
		}
		return null;
	}

}
