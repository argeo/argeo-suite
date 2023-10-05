import { transformExtent } from 'ol/proj.js';


export function transformToLatLonExtent(extent, projection) {
	const proj = projection.getCode();
	if (proj === 'EPSG:4326')
		return toLatLonExtent(extent);
	var transformed = transformExtent(extent, proj, 'EPSG:4326');
	return toLatLonExtent(transformed);
}

export function toLatLonExtent(extent) {
	return [extent[1], extent[0], extent[3], extent[2]];
}