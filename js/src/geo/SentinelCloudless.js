
import WMTS from 'ol/source/WMTS.js';
import WMTSTileGrid from 'ol/tilegrid/WMTS';
import { getTopLeft } from 'ol/extent';
import { getWidth } from 'ol/extent';
import { get as getProjection } from 'ol/proj';

export default class SentinelCloudless extends WMTS {
	static source_s2CL2019;
	static EPSG4326 = getProjection('EPSG:4326');

	static resolutions;
	static matrixIds;

	static {
		let min_zoom = 6;
		let max_zoom = 17;
		let zoomOffset = 1;

		// from https://s2maps.eu/
		let size = getWidth(this.EPSG4326.getExtent()) / 512;
		this.resolutions = new Array(max_zoom + zoomOffset);
		this.matrixIds = new Array(max_zoom + zoomOffset);
		for (let z = min_zoom; z <= max_zoom; ++z) {
			// generate resolutions and matrixIds arrays for this WMTS
			this.resolutions[z] = size / Math.pow(2, z);
			this.matrixIds[z] = z;
		}
	}
	
	constructor() {
		super({
			urls: [
				"//a.s2maps-tiles.eu/wmts/",
				"//b.s2maps-tiles.eu/wmts/",
				"//c.s2maps-tiles.eu/wmts/",
				"//d.s2maps-tiles.eu/wmts/",
				"//e.s2maps-tiles.eu/wmts/"
			],
			layer: 's2cloudless-2021',
			matrixSet: 'WGS84',
			format: 'image/jpeg',
			projection: SentinelCloudless.EPSG4326,
			tileGrid: new WMTSTileGrid({
				origin: getTopLeft(SentinelCloudless.EPSG4326.getExtent()),
				resolutions: SentinelCloudless.resolutions,
				matrixIds: SentinelCloudless.matrixIds,
			}),
			style: 'default',
			transition: 0,
			wrapX: true
		});
	}
}