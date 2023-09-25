package org.argeo.app.swt.chart;

import java.io.StringWriter;

import org.argeo.app.ux.js.JsClient;
import org.eclipse.swt.widgets.Composite;

import jakarta.json.Json;
import jakarta.json.stream.JsonGenerator;

public class SwtJsBarChart extends AbstractJsChart {

	public SwtJsBarChart(String chartName, Composite parent, int style) {
		super(chartName, parent, style);
	}

	@Override
	protected String getJsImplementation() {
		return "globalThis.argeo.app.chart.BarChart";
	}

	public void setLabels(String[] labels) {
		executeChartMethod("setLabels(%s)", JsClient.toJsArray(labels));
	}

	public void addDataset(String label, int[] values) {
		executeChartMethod("addDataset('%s', %s)", label, JsClient.toJsArray(values));
	}

	public void setData(String[] labels, String label, int[] values) {
		executeChartMethod("setData(%s, '%s', %s)", JsClient.toJsArray(labels), label, JsClient.toJsArray(values));
	}

	public void setDatasets(String[] labels, String[] label, int[][] values) {
		executeChartMethod("setDatasets(%s, %s)", JsClient.toJsArray(labels), toDatasets(label, values));
	}

	protected String toDatasets(String[] label, int[][] values) {
		if (label.length != values.length)
			throw new IllegalArgumentException("Arrays must have the same length");
		StringWriter writer = new StringWriter();
		JsonGenerator g = Json.createGenerator(writer);
		g.writeStartArray();
		for (int i = 0; i < label.length; i++) {
			g.writeStartObject();
			g.write("label", label[i]);
			g.writeStartArray("data");
			for (int j = 0; j < values[i].length; j++) {
				g.write(values[i][j]);
			}
			g.writeEnd();// data array
			g.writeEnd();// dataset
		}
		g.writeEnd();
		g.close();
		return writer.toString();
	}

	public void clearDatasets() {
		executeChartMethod("clearDatasets()");
	}
}
