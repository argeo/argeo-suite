package org.argeo.app.geo.ux;

public interface MapPart {
	enum Format {
		GEOJSON, GPX;
	}

	void addPoint(Double lng, Double lat);

	void addUrlLayer(String layer, Format format);

	void setZoom(int zoom);

	void setCenter(Double lng, Double lat);
}
