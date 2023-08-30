import Map from 'ol/Map.js';
import OSM from 'ol/source/OSM.js';
import TileLayer from 'ol/layer/Tile.js';
import View from 'ol/View.js';
import { fromLonLat, toLonLat } from 'ol/proj.js';

import MapPart from './MapPart.js';

export default class OpenLayersMapPart extends MapPart {
	#map;
	// Constructor
	constructor() {
		super();
		this.#map = new Map({
			layers: [
				new TileLayer({
					source: new OSM(),
				}),
			],
			target: 'map',
		});
	}

	setZoom(zoom) {
		this.#map.getView().setZoom(zoom);
	}

	setCenter(lng, lat) {
		this.#map.getView().setCenter(fromLonLat([lng, lat]));
	}
}
