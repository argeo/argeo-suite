package org.argeo.app.geo.ux;

/** An UX part displaying a map. */
public interface MapPart {
	/** A supported geographical data format. */
	enum GeoFormat {
		GEOJSON, GPX;
	}

	void addPoint(double lng, double lat, String style);

	void addUrlLayer(String url, GeoFormat format, String style);

	void setZoom(int zoom);

	void setCenter(double lng, double lat);

	/** Event when a feature has been single-clicked. */
	record FeatureSingleClickEvent(String path) {
	};

	/** Event when a feature has been selected. */
	record FeatureSelectedEvent(String path) {
	};

	/** Event when a feature popup is requested. */
	record FeaturePopupEvent(String path) {
	};
}
