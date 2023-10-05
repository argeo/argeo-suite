
import VectorSource from 'ol/source/Vector.js';
import { bbox } from 'ol/loadingstrategy';
import { transformExtent } from 'ol/proj.js';

export default class BboxVectorSource extends VectorSource {
	constructor(options) {
		super(BboxVectorSource.processOptions(options));
	}

	static processOptions(options) {
		options.strategy = bbox;
		options.url = function(extent, resolution, projection) {
			const proj = projection.getCode();
			var bbox = transformExtent(extent, proj, 'EPSG:4326');

			const baseUrl = options.baseUrl;
			const url = baseUrl + '&bbox=' + bbox.join(',');
			return url;
		}
		return options;
	}
}