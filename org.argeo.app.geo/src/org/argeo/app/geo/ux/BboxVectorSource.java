package org.argeo.app.geo.ux;

import org.argeo.app.ol.FeatureFormat;
import org.argeo.app.ol.VectorSource;

public class BboxVectorSource extends VectorSource {

	public BboxVectorSource(Object... args) {
		super(args);
	}

	public BboxVectorSource(String baseUrl, FeatureFormat format) {
		setFormat(format);
		setBaseUrl(baseUrl);
	}

	@Override
	public String getJsPackage() {
		return AbstractGeoJsObject.JS_PACKAGE;
	}

	public void setBaseUrl(String baseUrl) {
		doSetValue(null, "baseUrl", baseUrl);
	}

}
