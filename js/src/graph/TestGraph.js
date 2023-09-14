import * as Plot from "@observablehq/plot";

export default class TestGraph {

	init() {

		const plot = Plot.rectY({ length: 10000 }, Plot.binX({ y: "count" }, { x: Math.random })).plot();
		const div = document.querySelector("#myplot");
		div.append(plot);

	}
}
