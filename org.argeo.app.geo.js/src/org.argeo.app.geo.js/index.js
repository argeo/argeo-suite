import Map from 'ol/Map.js';
import OSM from 'ol/source/OSM.js';
import TileLayer from 'ol/layer/Tile.js';
import View from 'ol/View.js';
import { fromLonLat, toLonLat } from 'ol/proj.js';

import 'ol/ol.css';

//window.map = new Map({
//	layers: [
//		new TileLayer({
//			source: new OSM(),
//		}),
//	],
//	target: 'mapTarget',
//	view: new View({
//		center: [0, 0],
//		zoom: 2,
//	}),
//});
//window.map.on('rendercomplete', e => {
//	console.log('Render completed.');
//	onRenderComplete();
//});
//map.getView().setZoom(4);

if (typeof globalThis.argeo === 'undefined')
	globalThis.argeo = {};
if (typeof globalThis.argeo.app === 'undefined')
	globalThis.argeo.app = {};
if (typeof globalThis.argeo.app.geo === 'undefined')
	globalThis.argeo.app.geo = {};



//window.argeo.app.geo.ArgeoMap = {
//	map: new Map({
//		layers: [
//			new TileLayer({
//				source: new OSM(),
//			}),
//		],
//		target: 'map',
//	}),
//
//	setZoom: function(zoom) {
//		this.map.getView().setZoom(zoom);
//	},
//
//	setCenter: function(lng, lat) {
//		this.map.getView().setCenter(fromLonLat([lng, lat]));
//	},
//
//};

export class ArgeoMap {
	#map;
	// Constructor
	constructor() {
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

globalThis.argeo.app.geo.ArgeoMap = ArgeoMap;

//globalThis.argeoMap = new ArgeoMap();

//window.argeoMap = Object.create(argeo.app.geo.ArgeoMap);
//window.argeoMap.map.on('rendercomplete', e => {
//	console.log('Render completed.');
//	onRenderComplete();
//});


//function argeo_app_geo_Map() {
//	console.log('Entered constructor');
//	this.map = new Map({
//		layers: [
//			new TileLayer({
//				source: new OSM(),
//			}),
//		],
//		target: 'map',
//		view: new View({
//			center: [0, 0],
//			zoom: 2,
//		}),
//	});
//	this.map.on('rendercomplete', e => {
//		console.log('Render completed.');
//		onRenderComplete();
//	});
//
//	this.setCenter = function(lng, lat) {
//		console.log('Center set ');
//		//this.map.getView().setCenter(ol.proj.fromLonLat([lng, lat]));
//	}
//
//	this.setZoom = function(zoom) {
//		this.map.getView().setZoom(zoom);
//	}
//}
//
//window.argeoMap = new argeo_app_geo_Map();
//argeoMap.setCenter(13.404954, 52.520008);
//function setCenter(lng, lat) {
//	map.getView().setCenter(lng, lat);
//}