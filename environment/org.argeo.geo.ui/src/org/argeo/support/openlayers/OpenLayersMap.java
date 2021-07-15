package org.argeo.support.openlayers;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.api.NodeConstants;
import org.argeo.cms.ui.CmsView;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.entity.EntityNames;
import org.argeo.entity.EntityType;
import org.argeo.suite.ui.SuiteEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/** Display a map. */
public class OpenLayersMap extends Composite {
	private static final long serialVersionUID = 1055893020490283622L;

	private final static Log log = LogFactory.getLog(OpenLayersMap.class);

	private Browser browser;
	private boolean renderCompleted = false;

	private Double centerLng = null, centerLat = null;
	private Integer zoom = null;
	private String vectorSource = null;
	private String gpxSource = null;

	private List<String> geoJsonSources = new ArrayList<>();

	private CmsView cmsView;

	public OpenLayersMap(Composite parent, int style, URL mapHtml) {
		super(parent, style);
		cmsView = CmsView.getCmsView(parent);
		setLayout(new GridLayout());

		browser = new Browser(this, SWT.BORDER);
		browser.setLayoutData(CmsUiUtils.fillAll());
		String html;
		try (InputStream in = mapHtml.openStream()) {
			html = IOUtils.toString(in, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		new RenderCompleted(browser, "renderCompleted");
		new OnFeatureSelect(browser, "onFeatureSelect");
		new OnFeatureUnselect(browser, "onFeatureUnselect");
		new OnFeatureClick(browser, "onFeatureClick");
		browser.setText(html);
	}

	public void setCenter(Double lng, Double lat) {
		if (isRenderCompleted())
			browser.evaluate("map.getView().setCenter(ol.proj.fromLonLat([" + lng + ", " + lat + "]))");
		this.centerLat = lat;
		this.centerLng = lng;
	}

	public synchronized void setRenderCompleted(boolean renderCompleted) {
		this.renderCompleted = renderCompleted;
		notifyAll();
	}

	public synchronized boolean isRenderCompleted() {
		return renderCompleted;
	}

	@Override
	public synchronized void dispose() {
		long timeout = 500;
		long begin = System.currentTimeMillis();
		while (!isRenderCompleted() && ((System.currentTimeMillis() - begin) < timeout)) {
			try {
				wait(50);
			} catch (InterruptedException e) {
				// silent
			}
		}
		super.dispose();
	}

	public void setZoom(int zoom) {
		if (isRenderCompleted())
			browser.evaluate("map.getView().setZoom(" + zoom + ")");
		this.zoom = zoom;
	}

	public void addPoints(List<Node> geoPoints) throws RepositoryException {
		boolean first = true;
		StringBuffer sb = new StringBuffer("new ol.source.Vector({ features: [");
		for (int i = 0; i < geoPoints.size(); i++) {
			Node node = geoPoints.get(i);
			if (node.isNodeType(EntityType.geopoint.get())) {
				if (first)
					first = false;
				else
					sb.append(",");
				Double lng = node.getProperty(EntityNames.GEO_LONG).getDouble();
				Double lat = node.getProperty(EntityNames.GEO_LAT).getDouble();
				sb.append("new ol.Feature({ geometry:");
				sb.append("new ol.geom.Point(ol.proj.fromLonLat([");
				sb.append(lng).append(',').append(lat);
				sb.append("]))");
				sb.append(",path:'").append(node.getPath()).append("'");
				sb.append("})");
			}
		}
		sb.append("]");
		sb.append(" })");
		this.vectorSource = sb.toString();
		if (log.isTraceEnabled())
			log.trace("Vector source: " + vectorSource);
		renderVectorSource();
	}

	protected void renderVectorSource() {
		if (vectorSource == null)
			return;
		if (isRenderCompleted()) {
//			String style = ", style: new ol.style.Style({  image: new ol.style.Icon({ src: 'https://openlayers.org/en/latest/examples/data/icon.png' }) })";
			String style = "";
			String toEvaluate = "map.addLayer(new ol.layer.Vector({ source: " + vectorSource + style + "}));";
//			System.out.println(toEvaluate);
			browser.execute(toEvaluate);
		}
	}

	public void addPoint(Double lng, Double lat) {
		this.vectorSource = "new ol.source.Vector({ features: [ new ol.Feature({ geometry:"
				+ " new ol.geom.Point(ol.proj.fromLonLat([" + lng + ", " + lat + "])) }) ] })";
//		if (renderCompleted) {
//			browser.evaluate(
//					"map.addLayer(new ol.layer.Vector({ source: new ol.source.Vector({ features: [ new ol.Feature({ geometry:"
//							+ " new ol.geom.Point(ol.proj.fromLonLat([" + lng + ", " + lat + "])) }) ] }) }));");
//		}
		renderVectorSource();
	}

	public void addGpx(String path) {
		this.gpxSource = "new ol.source.Vector({ url: '" + path + "', format: new ol.format.GPX() })";
		renderGpxSource();
	}

	protected void renderGpxSource() {
		if (gpxSource == null)
			return;
		if (isRenderCompleted())
			browser.evaluate("map.addLayer(new ol.layer.Vector({ source: " + gpxSource + "}));");
	}

	public void addGeoJson(String path) {
		String geoJsonSource = "new ol.source.Vector({ url: '" + path + "', format: new ol.format.GeoJSON() })";
		geoJsonSources.add(geoJsonSource);
		renderGeoJsonSources();
	}

	protected void renderGeoJsonSources() {
		if (geoJsonSources.isEmpty())
			return;
		if (isRenderCompleted()) {
			for (String geoJson : geoJsonSources) {
				browser.evaluate("map.addLayer(new ol.layer.Vector({ source: " + geoJson + "}));");
			}
		}
	}

	private class RenderCompleted extends BrowserFunction {

		RenderCompleted(Browser browser, String name) {
			super(browser, name);
		}

		@Override
		public Object function(Object[] arguments) {
			try {
				if (!isRenderCompleted()) {
					setRenderCompleted(true);
					if (zoom != null)
						setZoom(zoom);
					if (centerLat != null && centerLng != null) {
						setCenter(centerLng, centerLat);
					}
					if (!geoJsonSources.isEmpty())
						renderGeoJsonSources();
					if (gpxSource != null)
						renderGpxSource();
					if (vectorSource != null)
						renderVectorSource();
				}
				return null;
			} catch (Exception e) {
				log.error("Cannot render map", e);
				return null;
			}
		}
	}

	private class OnFeatureSelect extends BrowserFunction {

		OnFeatureSelect(Browser browser, String name) {
			super(browser, name);
		}

		@Override
		public Object function(Object[] arguments) {
			if (arguments.length == 0)
				return null;
			String path = arguments[0].toString();
			Map<String, Object> properties = new HashMap<>();
			properties.put(SuiteEvent.NODE_PATH, path);
			properties.put(SuiteEvent.WORKSPACE, NodeConstants.SYS_WORKSPACE);
			cmsView.sendEvent(SuiteEvent.refreshPart.topic(), properties);
			return null;
		}
	}

	private class OnFeatureUnselect extends BrowserFunction {

		OnFeatureUnselect(Browser browser, String name) {
			super(browser, name);
		}

		@Override
		public Object function(Object[] arguments) {
			return null;
		}
	}

	private class OnFeatureClick extends BrowserFunction {

		OnFeatureClick(Browser browser, String name) {
			super(browser, name);
		}

		@Override
		public Object function(Object[] arguments) {
			return null;
		}
	}
}
