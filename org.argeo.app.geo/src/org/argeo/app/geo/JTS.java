package org.argeo.app.geo;

import org.argeo.api.cms.CmsLog;
import org.locationtech.jts.geom.GeometryFactory;

/**
 * Factories initialisation and workarounds for the JTS library. The idea is to
 * code defensively around factory initialisation, API changes, and issues
 * related to running in an OSGi environment. Rather see {@link GeoUtils} for
 * functional static utilities.
 */
public class JTS {
	private final static CmsLog log = CmsLog.getLog(JTS.class);

	public final static GeometryFactory GEOMETRY_FACTORY;

	static {
		try {
			// GEOMETRY_FACTORY = JTSFactoryFinder.getGeometryFactory();
			GEOMETRY_FACTORY = new GeometryFactory();
		} catch (RuntimeException e) {
			log.error("Basic JTS initialisation failed, geographical utilities are probably not available", e);
			throw e;
		}
	}

}
