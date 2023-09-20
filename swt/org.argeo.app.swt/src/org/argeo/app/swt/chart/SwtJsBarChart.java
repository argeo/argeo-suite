package org.argeo.app.swt.chart;

import java.io.StringWriter;

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
		callMethod(getJsChartVar(), "setLabels(%s)", toJsArray(labels));
	}

	public void addDataset(String label, int[] values) {
		callMethod(getJsChartVar(), "addDataset('%s', %s)", label, toJsArray(values));
	}

	public void setData(String[] labels, String label, int[] values) {
		callMethod(getJsChartVar(), "setData(%s, '%s', %s)", toJsArray(labels), label, toJsArray(values));
	}

	public void setDatasets(String[] labels, String[] label, int[][] values) {
		callMethod(getJsChartVar(), "setDatasets(%s, %s)", toJsArray(labels), toDatasets(label, values));
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
		callMethod(getJsChartVar(), "clearDatasets()");
	}
}
