
import VectorSource from 'ol/source/Vector.js';
import { bbox } from 'ol/loadingstrategy';
import { transformToLatLonExtent } from './OpenLayersUtils.js';

export default class BboxVectorSource extends VectorSource {
	constructor(options) {
		super(BboxVectorSource.processOptions(options));
	}

	static processOptions(options) {
		options.strategy = bbox;
		options.url = function(extent, resolution, projection) {
			var bbox = transformToLatLonExtent(extent, projection);

			const baseUrl = options.baseUrl;
			// invert bbox order in order to have minLat,minLon,maxLat,maxLon as required by WFS 2.0.0
			const url = baseUrl + '&bbox=' + bbox.join(',') + ',EPSG:4326';
			return url;
		}
		return options;
	}


}