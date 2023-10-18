/** OpenLayers-based implementation. 
 * @module OpenLayersMapPart
 */

import { fromLonLat, getPointResolution } from 'ol/proj.js';
import { transformExtent } from 'ol/proj.js';

import TileLayer from 'ol/layer/Tile.js';

import OSM from 'ol/source/OSM.js';
import { isEmpty } from 'ol/extent';

import Select from 'ol/interaction/Select.js';
import Overlay from 'ol/Overlay.js';

import Map from 'ol/Map.js';

import { OverviewMap, ScaleLine, defaults as defaultControls } from 'ol/control.js';
import { easeOut } from 'ol/easing';

import * as SLDReader from '@nieuwlandgeo/sldreader';

import MapPart from './MapPart.js';

/** OpenLayers implementation of MapPart. */
export default class OpenLayersMapPart extends MapPart {
	/** The OpenLayers Map. */
	#map;

	/** The overview map */
	#overviewMap;

	/** Styled layer descriptor */
	#sld;

	/** The select interaction */
	select;

	/** Externally added callback functions. */
	callbacks = {};

	/** Constructor taking the mapName as an argument. */
	constructor(mapName) {
		super(mapName);
		this.#overviewMap = new OverviewMap({
			layers: [
				new TileLayer({
					source: new OSM(),
				}),
			],
		});
		this.select = new Select();
		this.#map = new Map({
			controls: defaultControls({
				attribution: false,
				rotate: false,
			}).extend([this.#overviewMap, new ScaleLine({
				bar: false,
				steps: 2,
				text: false,
				minWidth: 150,
				maxWidth: 200,
			})]),
			layers: [
			],
			//						view: new View({
			//							projection: 'EPSG:4326',
			//							center: [0, 0],
			//							zoom: 2,
			//						}),
			target: this.getMapName(),
		});
		this.#map.addInteraction(this.select);
		//this.#map.getView().set('projection', 'EPSG:4326', true);
	}

	/* GEOGRAPHICAL METHODS */

	setCenter(lat, lon) {
		this.#map.getView().setCenter(fromLonLat([lon, lat]));
	}

	fit(extent, options) {
		var transformed = transformExtent(extent, 'EPSG:4326', this.#map.getView().getProjection());
		this.#map.getView().fit(transformed, options);
	}

	/** Accessors */
	getMap() {
		return this.#map;
	}

	getLayerByName(name) {
		let layers = this.#map.getLayers();
		for (let i = 0; i < layers.getLength(); i++) {
			let layer = layers.item(i);
			let n = layer.get('name');
			if (n !== undefined) {
				if (name === n)
					return layer;
			}
		}
		return undefined;
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
				mapPart.callbacks['onFeatureSingleClick'](feature.get('cr:path'));
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
				mapPart.callbacks['onFeatureSelected'](feature.get('cr:path'));
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
			const path = selected.get('cr:path');
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

	selectFeatures(layerName, featureIds) {
		// we cannot use 'this' in the function provided to OpenLayers
		let mapPart = this;
		this.select.getFeatures().clear();
		const layer = this.getLayerByName(layerName);
		const source = layer.getSource();
		for (const featureId of featureIds) {
			let feature = source.getFeatureById(featureId);
			if (feature === null) {
				source.on('featuresloadend', function(e) {
					feature = source.getFeatureById(featureId);
					if (feature !== null)
						mapPart.select.getFeatures().push(feature);
				});
			} else {
				this.select.getFeatures().push(feature);
			}
		}
	}

	fitToLayer(layerName) {
		// we cannot use 'this' in the function provided to OpenLayers
		let mapPart = this;
		const layer = this.getLayerByName(layerName);
		const source = layer.getSource();
		const extent = source.getExtent();
		const options = {
			duration: 1000,
			padding: [20, 20, 20, 20],
			easing: easeOut,
		};
		if (!isEmpty(extent))
			this.#map.getView().fit(source.getExtent(), options);
		source.on('featuresloadend', function(e) {
			mapPart.getMap().getView().fit(source.getExtent(), options);
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

	setSld(xml) {
		this.#sld = SLDReader.Reader(xml);
	}

	/** Get a FeatureTypeStyle (https://nieuwlandgeo.github.io/SLDReader/api.html#FeatureTypeStyle).  */
	getFeatureTypeStyle(styledLayerName, styleName) {
		const sldLayer = SLDReader.getLayer(this.#sld, styledLayerName);
		const style = styleName === undefined ? SLDReader.getStyle(sldLayer) : SLDReader.getStyle(sldLayer, styleName);
		// OpenLayers can only use one definition
		const featureTypeStyle = style.featuretypestyles[0];
		return featureTypeStyle;
	}

	applyStyle(layerName, styledLayerName, styleName) {
		const layer = this.getLayerByName(layerName);
		const featureTypeStyle = this.getFeatureTypeStyle(styledLayerName, styleName);
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
				layer.changed();
			},
		});
		layer.setStyle(olStyleFunction);
	}

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
