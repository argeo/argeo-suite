import ChartPart from './ChartPart.js';

import { Chart } from 'chart.js';
import annotationPlugin from 'chartjs-plugin-annotation';

Chart.register(annotationPlugin);

export default class ChartJsPart extends ChartPart {
	#chart;

	/** Constructor taking the mapName as an argument. */
	constructor(chartName) {
		super(chartName);
	}

	setChart(chart) {
		this.#chart = chart;
	}

	getChart() {
		return this.#chart;
	}

	//
	// DATA
	//
	setLabels(labels) {
		const chart = this.getChart();
		chart.data.labels = labels;
		this.update();
	}

	addDataset(label, data) {
		const chart = this.getChart();
		chart.data.datasets.push({
			label: label,
			data: data,
			borderWidth: 1
		});
		this.update();
	}

	setData(labels, label, data) {
		this.clearDatasets();
		this.setLabels(labels);
		this.addDataset(label, data);
	}

	setDatasets(labels, datasets) {
		const chart = this.getChart();
		chart.data.datasets = datasets;
		chart.data.labels = labels;
		this.update();
	}

	clearDatasets() {
		const chart = this.getChart();
		chart.data.datasets = [];
		this.update();
	}

	update() {
		this.#chart.update();
	}
}
