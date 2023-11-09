package org.argeo.app.api.geo;

import javax.xml.namespace.QName;

import org.argeo.api.acr.Content;
import org.argeo.api.acr.search.AndFilter;
import org.argeo.app.api.EntityType;
import org.argeo.app.geo.acr.GeoEntityUtils;
import org.locationtech.jts.geom.Geometry;

import jakarta.json.stream.JsonGenerator;

public interface FeatureAdapter {
	default Geometry getDefaultGeometry(Content c, QName targetFeature) {
		// TODO deal with more defaults
		// TODO deal with target feature
		if (c.hasContentClass(EntityType.geopoint)) {
			return getGeoPointGeometry(c);
		}
		return null;
	}

	void writeProperties(JsonGenerator g, Content content, QName targetFeature);

	void addConstraintsForFeature(AndFilter filter, QName targetFeature);

	static Geometry getGeoPointGeometry(Content c) {
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
