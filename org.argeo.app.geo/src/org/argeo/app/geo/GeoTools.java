package org.argeo.app.geo;

import org.argeo.api.cms.CmsLog;
import org.geotools.api.filter.FilterFactory;
import org.geotools.api.style.StyleFactory;
import org.geotools.factory.CommonFactoryFinder;

/**
 * Factories initialisation and workarounds for the GeoTools library. The idea
 * is to code defensively around factory initialisation, API changes, and issues
 * related to running in an OSGi environment. Rather see {@link GeoUtils} for
 * functional static utilities.
 */
public class GeoTools {
	private final static CmsLog log = CmsLog.getLog(GeoTools.class);

	public final static StyleFactory STYLE_FACTORY;
	public final static FilterFactory FILTER_FACTORY;

	static {
		try {
			STYLE_FACTORY = CommonFactoryFinder.getStyleFactory();
			FILTER_FACTORY = CommonFactoryFinder.getFilterFactory();
		} catch (RuntimeException e) {
			log.error("Basic GeoTools initialisation failed, geographical utilities are probably not available", e);
			throw e;
		}
	}

}
