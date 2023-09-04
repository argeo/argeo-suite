/** OpenLayers-based implementation. 
 * @module OpenLayersMapPart
 */

import Map from 'ol/Map.js';
import View from 'ol/View.js';
import OSM from 'ol/source/OSM.js';
import TileLayer from 'ol/layer/Tile.js';
import { fromLonLat, getPointResolution } from 'ol/proj.js';
import VectorSource from 'ol/source/Vector.js';
import Feature from 'ol/Feature.js';
import { Point } from 'ol/geom.js';
import VectorLayer from 'ol/layer/Vector.js';
import GeoJSON from 'ol/format/GeoJSON.js';
import GPX from 'ol/format/GPX.js';
import Select from 'ol/interaction/Select.js';
import Overlay from 'ol/Overlay.js';
import { Style, Icon } from 'ol/style.js';

import * as SLDReader from '@nieuwlandgeo/sldreader';

import MapPart from './MapPart.js';
import { SentinelCloudless } from './OpenLayerTileSources.js';

/** OpenLayers implementation of MapPart. */
export default class OpenLayersMapPart extends MapPart {
	/** The OpenLayers Map. */
	#map;

	/** Externally added callback functions. */
	callbacks = {};

	/** Constructor taking the mapName as an argument. */
	constructor(mapName) {
		super(mapName);
		this.#map = new Map({
			layers: [
				//				new TileLayer({
				//					source: new SentinelCloudless(),
				//				}),
				new TileLayer({
					source: new OSM(),
					opacity: 0.4,
					transition: 0,
				}),
			],
			target: this.getMapName(),
		});
	}

	/* GEOGRAPHICAL METHODS */

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
		this.#map.addLayer(new VectorLayer({
			source: vectorSource,
			style: style,
		}));
	}

	addUrlLayer(url, format, style, sld) {
		let featureFormat;
		if (format === 'GEOJSON') {
			featureFormat = new GeoJSON();
		}
		else if (format === 'GPX') {
			featureFormat = new GPX();
		} else {
			throw new Error("Unsupported format " + format);
		}
		const vectorSource = new VectorSource({
			url: url,
			format: featureFormat,
		});
		const vectorLayer = new VectorLayer({
			source: vectorSource,
		});
		if (sld) {
			this.#applySLD(vectorLayer, style);
		} else {
			vectorLayer.setStyle(style);
		}
		this.#map.addLayer(vectorLayer);
	}


	/* CALLBACKS */
	enableFeatureSingleClick() {
		// we cannot use 'this' in the function provided to OpenLayers
		let mapPart = this;
		this.#map.on('singleclick', function(e) {
			let feature = null;
			// we chose the first one
			e.map.forEachFeatureAtPixel(e.pixel, function(f) {
				feature = f;
				return true;
			});
			if (feature !== null)
				mapPart.callbacks['onFeatureSingleClick'](feature.get('path'));
		});
	}

	enableFeatureSelected() {
		// we cannot use 'this' in the function provided to OpenLayers
		let mapPart = this;
		var select = new Select();
		this.#map.addInteraction(select);
		select.on('select', function(e) {
			if (e.selected.length > 0) {
				let feature = e.selected[0];
				mapPart.callbacks['onFeatureSelected'](feature.get('path'));
			}
		});
	}

	enableFeaturePopup() {
		// we cannot use 'this' in the function provided to OpenLayers
		let mapPart = this;
		/**
		 * Elements that make up the popup.
		 */
		const container = document.getElementById('popup');
		const content = document.getElementById('popup-content');
		const closer = document.getElementById('popup-closer');

		/**
		 * Create an overlay to anchor the popup to the map.
		 */
		const overlay = new Overlay({
			element: container,
			autoPan: false,
			autoPanAnimation: {
				duration: 250,
			},
		});
		this.#map.addOverlay(overlay);

		let selected = null;
		this.#map.on('pointermove', function(e) {
			if (selected !== null) {
				selected.setStyle(undefined);
				selected = null;
			}

			e.map.forEachFeatureAtPixel(e.pixel, function(f) {
				selected = f;
				return true;
			});

			if (selected == null) {
				overlay.setPosition(undefined);
				return;
			}
			const coordinate = e.coordinate;
			const path = selected.get('path');
			if (path === null)
				return true;
			const res = mapPart.callbacks['onFeaturePopup'](path);
			if (res != null) {
				content.innerHTML = res;
				overlay.setPosition(coordinate);
			} else {
				overlay.setPosition(undefined);
			}
		});
	}

	//
	// HTML
	//
	getMapDivCssClass() {
		return 'map';
	}

	//
	// STATIC FOR EXTENSION
	//
	static newStyle(args) {
		return new Style(args);
	}

	static newIcon(args) {
		return new Icon(args);
	}

	//
	// SLD STYLING
	//
	#applySLD(vectorLayer, text) {
		const sldObject = SLDReader.Reader(text);
		const sldLayer = SLDReader.getLayer(sldObject);
		const style = SLDReader.getStyle(sldLayer);
		const featureTypeStyle = style.featuretypestyles[0];

		const viewProjection = this.#map.getView().getProjection();
		const olStyleFunction = SLDReader.createOlStyleFunction(featureTypeStyle, {
			// Use the convertResolution option to calculate a more accurate resolution.
			convertResolution: viewResolution => {
				const viewCenter = this.#map.getView().getCenter();
				return getPointResolution(viewProjection, viewResolution, viewCenter);
			},
			// If you use point icons with an ExternalGraphic, you have to use imageLoadCallback
			// to update the vector layer when an image finishes loading.
			// If you do not do this, the image will only be visible after next layer pan/zoom.
			imageLoadedCallback: () => {
				vectorLayer.changed();
			},
		});
		vectorLayer.setStyle(olStyleFunction);
	}


}
