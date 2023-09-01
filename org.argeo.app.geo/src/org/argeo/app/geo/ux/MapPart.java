package org.argeo.app.geo.ux;

/** An UX part displaying a map. */
public interface MapPart {
	/** A supported geographical data format. */
	enum GeoFormat {
		GEOJSON, GPX;
	}

	void addPoint(double lng, double lat, String style);

	void addUrlLayer(String url, GeoFormat format);

	void setZoom(int zoom);

	void setCenter(double lng, double lat);
}
