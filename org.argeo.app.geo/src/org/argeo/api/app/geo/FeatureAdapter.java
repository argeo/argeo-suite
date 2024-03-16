package org.argeo.api.app.geo;

import javax.xml.namespace.QName;

import org.argeo.api.acr.Content;
import org.argeo.api.acr.search.AndFilter;
import org.locationtech.jts.geom.Geometry;

import jakarta.json.stream.JsonGenerator;

/** Transform a {@link Content} to an OGC feature. */
public interface FeatureAdapter {
	Geometry getDefaultGeometry(Content c, QName targetFeature);

	void writeProperties(JsonGenerator g, Content content, QName targetFeature);

	void addConstraintsForFeature(AndFilter filter, QName targetFeature);
}
