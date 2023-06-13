package org.argeo.app.core;

import java.net.MalformedURLException;
import java.net.URL;

import javax.measure.Quantity;
import javax.measure.quantity.Area;

import org.argeo.api.acr.spi.ContentNamespace;
import org.argeo.api.acr.spi.ProvidedRepository;
import org.geotools.gml3.v3_2.GML;

import si.uom.SI;
import tech.units.indriya.quantity.Quantities;

/**
 * Background service starting and stopping with the whole system, and making
 * sure it is in a proper state.
 */
public class SuiteMaintenance {
	private ProvidedRepository contentRepository;

	public void start() {
		// make sure that the unit system is initialised
		Quantity<Area> dummy = Quantities.getQuantity(0, SI.SQUARE_METRE);

		getContentRepository().registerTypes(SuiteContentNamespace.values());
//		for (SuiteContentTypes types : SuiteContentTypes.values()) {
//			getContentRepository().registerTypes(types.getDefaultPrefix(), types.getNamespace(),
//					types.getResource() != null ? types.getResource().toExternalForm() : null);
//		}

		// GML schema import fails because of xlinks issues
		getContentRepository().registerTypes(new ContentNamespace() {

			@Override
			public URL getSchemaResource() {
				try {
					return new URL(GML.getInstance().getSchemaLocation());
				} catch (MalformedURLException e) {
					throw new IllegalArgumentException(e);
				}
			}

			@Override
			public String getNamespaceURI() {
				return GML.getInstance().getNamespaceURI();
			}

			@Override
			public String getDefaultPrefix() {
				return "gml";
			}
		});

	}

	public void stop() {

	}

	protected ProvidedRepository getContentRepository() {
		return contentRepository;
	}

	public void setContentRepository(ProvidedRepository contentRepository) {
		this.contentRepository = contentRepository;
	}

}
