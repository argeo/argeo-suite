package org.argeo.app.geo;

import org.argeo.api.cms.CmsLog;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.styling.StyleFactory;
import org.locationtech.jts.geom.GeometryFactory;
import org.opengis.filter.FilterFactory2;

/**
 * Factories initialisation and workarounds for the GeoTools library. The idea
 * is to code defensively around factory initialisation, API changes, and issues
 * related to running in an OSGi environment. Rather see {@link GeoUtils} for
 * functional static utilities.
 */
public class GeoTools {
	private final static CmsLog log = CmsLog.getLog(GeoTools.class);

	public final static GeometryFactory GEOMETRY_FACTORY;
	public final static StyleFactory STYLE_FACTORY;
	public final static FilterFactory2 FILTER_FACTORY;

	static {
		try {
			GEOMETRY_FACTORY = JTSFactoryFinder.getGeometryFactory();
			STYLE_FACTORY = CommonFactoryFinder.getStyleFactory();
			FILTER_FACTORY = CommonFactoryFinder.getFilterFactory2();
		} catch (RuntimeException e) {
			log.error("Basic GeoTools initialisation failed, geographical utilities are probably not available", e);
			throw e;
		}
	}

}
