/** API to be used by Java.
 *  @module MapPart
 */

/** Abstract base class for displaying a map. */
export default class ChartPart {

	/** The name of the chart, will also be the name of the variable */
	#chartName;

	constructor(chartName) {
		this.#chartName = chartName;
		this.createChartCanvas(this.#chartName);
	}


	//
	// HTML
	//
	/** Create the div element where the chart will be displayed. */
	createChartCanvas(id) {
		const chartDiv = document.createElement('canvas');
		chartDiv.id = id;
		//chartDiv.style.cssText = 'width: 100%;';
		chartDiv.style.cssText = 'width: 100%; height: 100vh;';
		document.body.appendChild(chartDiv);
	}

	/** Get the div element where the chart is displayed. */
	getChartCanvas() {
		return document.getElementById(this.#chartName);
	}

}