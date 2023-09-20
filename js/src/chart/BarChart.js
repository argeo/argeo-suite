import Chart from 'chart.js/auto';

import ChartJsPart from './ChartJsPart.js';

export default class BarChart extends ChartJsPart {
	/** Constructor taking the mapName as an argument. */
	constructor(chartName) {
		super(chartName);
		this.setChart(new Chart(this.getChartCanvas(), {
			type: 'bar',
			data: {
				datasets: []
			},
			options: {
				scales: {
					y: {
						beginAtZero: true
					},
				},
				animation: false,
			}
		}));

	}


}
