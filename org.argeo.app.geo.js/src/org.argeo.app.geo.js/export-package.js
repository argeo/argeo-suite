import OpenLayersMapPart from './OpenLayersMapPart.js';

// PSEUDO PACKAGE
if (typeof globalThis.argeo === 'undefined')
	globalThis.argeo = {};
if (typeof globalThis.argeo.app === 'undefined')
	globalThis.argeo.app = {};
if (typeof globalThis.argeo.app.geo === 'undefined')
	globalThis.argeo.app.geo = {};


globalThis.argeo.app.geo.OpenLayersMapPart = OpenLayersMapPart;
