package org.argeo.app.ui.openlayers;

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
import org.argeo.api.cms.CmsView;
import org.argeo.app.api.EntityNames;
import org.argeo.app.api.EntityType;
import org.argeo.app.ui.SuiteEvent;
import org.argeo.api.cms.CmsLog;
import org.argeo.api.cms.CmsConstants;
import org.argeo.cms.swt.CmsSwtUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/** Display a map. */
public class OpenLayersMap extends Composite {
	private static final long serialVersionUID = 1055893020490283622L;

	private final static CmsLog log = CmsLog.getLog(OpenLayersMap.class);

	private Browser browser;
	private boolean renderCompleted = false;

	private Double centerLng = null, centerLat = null;
	private Integer zoom = null;
	private String vectorSource = null;
	private String gpxSource = null;

	private String vectorSourceStyle;

	private List<String> geoJsonSources = new ArrayList<>();
	private Map<String, String> vectorSources = new HashMap<>();
	private Map<String, String> layerStyles = new HashMap<>();

	private CmsView cmsView;

	public OpenLayersMap(Composite parent, int style, URL mapHtml) {
		super(parent, style);
		cmsView = CmsSwtUtils.getCmsView(parent);
		setLayout(new GridLayout());

		browser = new Browser(this, SWT.BORDER);
		browser.setLayoutData(CmsSwtUtils.fillAll());
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

	protected String asVectorSource(List<Node> geoPoints) throws RepositoryException {
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
				sb.append(",path:\"").append(node.getPath()).append("\"");
				sb.append(",name:\"").append(node.getName()).append("\"");
				String entityType = null;
				if (node.isNodeType(EntityType.local.get())) {
					entityType = node.getProperty(EntityNames.ENTITY_TYPE).getString();
					sb.append(", type:'").append(entityType).append("'");
				}
				enrichFeature(node, sb);
				sb.append("})");
			}
		}
		sb.append("]");
		sb.append(" })");
		return sb.toString();
	}

	protected void enrichFeature(Node node, StringBuffer sb) throws RepositoryException {

	}

	public void addPoints(List<Node> geoPoints) throws RepositoryException {
		this.vectorSource = asVectorSource(geoPoints);
		if (log.isTraceEnabled())
			log.trace("Vector source: " + vectorSource);
		renderVectorSource();
	}

	public void addPoints(String layerName, List<Node> geoPoints, String style) throws RepositoryException {
		this.vectorSources.put(layerName, asVectorSource(geoPoints));
		if (style != null) {
			layerStyles.put(layerName, style);
		}
		renderVectorSources();
	}

	protected void renderVectorSource() {
		if (vectorSource == null)
			return;
		if (isRenderCompleted()) {
//			String style = ", style: new ol.style.Style({  image: new ol.style.Icon({ src: '/pkg/org.djapps.on.openheritage.ui/map_oc.png' }) })";
			String style = vectorSourceStyle != null ? ", style: " + vectorSourceStyle : "";
//			String style = "";
			String toEvaluate = "map.addLayer(new ol.layer.Vector({ source: " + vectorSource + style + "}));";
//			System.out.println(toEvaluate);
			browser.execute(toEvaluate);
		}
	}

	protected void renderVectorSources() {
		if (vectorSources.isEmpty())
			return;
		if (isRenderCompleted()) {
			StringBuilder toExecute = new StringBuilder();
			for (String name : vectorSources.keySet()) {
				String style = layerStyles.containsKey(name) ? ", style: " + layerStyles.get(name) : "";
				String toEvaluate = "map.addLayer(new ol.layer.Vector({ source: " + vectorSources.get(name) + style
						+ ",name: '" + name + "'}));";
				toExecute.append(toEvaluate);
			}
			System.out.println(toExecute);
			browser.execute(toExecute.toString());
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

	public void setVectorSourceStyle(String vectorSourceStyle) {
		this.vectorSourceStyle = vectorSourceStyle;
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
					if (!vectorSources.isEmpty())
						renderVectorSources();
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
//			properties.put(SuiteEvent.NODE_PATH, path);
//			properties.put(SuiteEvent.WORKSPACE, CmsConstants.SYS_WORKSPACE);
			properties.put(SuiteEvent.CONTENT_PATH, '/' + CmsConstants.SYS_WORKSPACE + path);
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
