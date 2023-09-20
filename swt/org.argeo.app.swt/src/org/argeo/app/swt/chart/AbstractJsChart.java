package org.argeo.app.swt.chart;

import org.argeo.app.swt.js.SwtBrowserJsPart;
import org.eclipse.swt.widgets.Composite;

/** Base class for charts. */
public abstract class AbstractJsChart extends SwtBrowserJsPart {
	private String chartName;

	protected abstract String getJsImplementation();

	public AbstractJsChart(String chartName, Composite parent, int style) {
		super(parent, style, "/pkg/org.argeo.app.js/chart.html");
		this.chartName = chartName;
	}

	@Override
	protected void init() {
		// create chart
		doExecute(getJsChartVar() + " = new " + getJsImplementation() + "('" + chartName + "');");
	}

	protected String getJsChartVar() {
		return getJsVarName(chartName);
	}

}
