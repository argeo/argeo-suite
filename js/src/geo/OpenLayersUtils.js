import { transformExtent } from 'ol/proj.js';


export function transformToEpsg4326LatLonExtent(extent, projection) {
	const proj = projection.getCode();
	if (proj === 'EPSG:4326')
		return toLatLonExtent(extent);
	var transformed = transformExtent(extent, proj, 'EPSG:4326');
	return toLatLonExtent(transformed);
}

/** From EPSG:4326 lat/lon to a proj lon/lat */
export function transformToOlLonLatExtent(extent, projection) {
	const proj = projection.getCode();
	if (proj === 'EPSG:4326')
		return toLonLatExtent(extent);
	const reordered = toLonLatExtent(extent);
	var transformed = transformExtent(reordered, 'EPSG:4326', proj);
	return transformed;
}

/** Converts from an extent in OpenLayers order (lon/lat) to WFS 2.0 order (lat/lon). */
export function toLatLonExtent(extent) {
	return [extent[1], extent[0], extent[3], extent[2]];
}

/** Converts from an extent in WFS 2.0 order (lat/lon) to OpenLayers order (lon/lat) . */
export function toLonLatExtent(extent) {
	return [extent[1], extent[0], extent[3], extent[2]];
}

