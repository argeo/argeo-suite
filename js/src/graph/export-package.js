import TestGraph from './TestGraph.js';
import * as Plot from "@observablehq/plot";

// PSEUDO PACKAGE
if (typeof globalThis.argeo === 'undefined')
	globalThis.argeo = {};
if (typeof globalThis.argeo.app === 'undefined')
	globalThis.argeo.app = {};
if (typeof globalThis.argeo.app.graph === 'undefined')
	globalThis.argeo.app.graph = {};

// PUBLIC CLASSES
globalThis.argeo.app.graph.TestGraph = TestGraph;

const plot = Plot.rectY({ length: 10000 }, Plot.binX({ y: "count" }, { x: Math.random })).plot();
const div = document.querySelector("#myplot");
div.append(plot);

"use strict";
