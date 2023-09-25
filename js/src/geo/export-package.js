import OpenLayersMapPart from './OpenLayersMapPart.js';
import SentinelCloudless from './SentinelCloudless.js';

import Map from 'ol/Map.js';
import View from 'ol/View.js';
import OSM from 'ol/source/OSM.js';
import TileLayer from 'ol/layer/Tile.js';
import VectorSource from 'ol/source/Vector.js';
import VectorLayer from 'ol/layer/Vector.js';
import GeoJSON from 'ol/format/GeoJSON.js';

// PSEUDO PACKAGE
if (typeof globalThis.argeo === 'undefined')
	globalThis.argeo = {};
if (typeof globalThis.argeo.app === 'undefined')
	globalThis.argeo.app = {};
if (typeof globalThis.argeo.app.geo === 'undefined')
	globalThis.argeo.app.geo = {};

// THIRD PARTY
if (typeof globalThis.argeo.tp === 'undefined')
	globalThis.argeo.tp = {};
if (typeof globalThis.argeo.tp.ol === 'undefined')
	globalThis.argeo.tp.ol = {};

// PUBLIC CLASSES
globalThis.argeo.app.geo.OpenLayersMapPart = OpenLayersMapPart;
globalThis.argeo.app.geo.SentinelCloudless = SentinelCloudless;

globalThis.argeo.tp.ol.Map = Map;
globalThis.argeo.tp.ol.View = View;
globalThis.argeo.tp.ol.TileLayer = TileLayer;
globalThis.argeo.tp.ol.OSM = OSM;
globalThis.argeo.tp.ol.VectorSource = VectorSource;
globalThis.argeo.tp.ol.VectorLayer = VectorLayer;
globalThis.argeo.tp.ol.GeoJSON = GeoJSON;

"use strict";

