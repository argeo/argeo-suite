/** OpenLayers-based implementation. 
 * @module OpenLayersMapPart
 */

import Map from 'ol/Map.js';
import OSM from 'ol/source/OSM.js';
import TileLayer from 'ol/layer/Tile.js';
import { fromLonLat } from 'ol/proj.js';
import VectorSource from 'ol/source/Vector.js';
import Feature from 'ol/Feature.js';
import { Point } from 'ol/geom.js';
import VectorLayer from 'ol/layer/Vector.js';
import GeoJSON from 'ol/format/GeoJSON.js';
import GPX from 'ol/format/GPX.js';

import MapPart from './MapPart.js';
import { SentinelCloudless } from './OpenLayerTileSources.js';

/** OpenLayers implementation of MapPart. */
export default class OpenLayersMapPart extends MapPart {
	/** The OpenLayers Map. */
	#map;
	// Constructor
	constructor() {
		super();
		this.#map = new Map({
			layers: [
				new TileLayer({
					source: new SentinelCloudless(),
				}),
				new TileLayer({
					source: new OSM(),
					opacity: 0.4,
					transition: 0,
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

	addPoint(lng, lat, style) {
		let vectorSource = new VectorSource({
			features: [new Feature({
				geometry: new Point(fromLonLat([lng, lat]))
			})]
		});
		this.#map.addLayer(new VectorLayer({ source: vectorSource }));
	}

	addUrlLayer(url, format) {
		let vectorSource;
		if (format === 'GEOJSON') {
			vectorSource = new VectorSource({ url: url, format: new GeoJSON() })
		}
		else if (format === 'GPX') {
			vectorSource = new VectorSource({ url: url, format: new GPX() })
		}
		this.#map.addLayer(new VectorLayer({
			source: vectorSource,
		}));
	}
}
