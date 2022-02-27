package org.argeo.app.ui.openlayers;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.query.Query;

import org.argeo.app.api.EntityType;
import org.argeo.cms.swt.CmsSwtUtils;
import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.jcr.JcrException;
import org.argeo.jcr.JcrUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/** Displays an overview map. */
public class OverviewMap implements CmsUiProvider {

	@Override
	public Control createUi(Composite parent, Node context) throws RepositoryException {
		parent.setLayout(new GridLayout());
		refreshUi(parent, context);

		try {
			String[] nodeTypes = { EntityType.geopoint.get() };
			context.getSession().getWorkspace().getObservationManager().addEventListener(new EventListener() {

				@Override
				public void onEvent(EventIterator events) {
					if (!parent.isDisposed())
						parent.getDisplay().asyncExec(() -> {
							try {
								refreshUi(parent, context);
							} catch (RepositoryException e) {
								throw new JcrException(e);
							}
						});
				}
			}, Event.PROPERTY_CHANGED | Event.NODE_ADDED | Event.NODE_REMOVED | Event.PROPERTY_ADDED, "/", true, null,
					nodeTypes, false);
		} catch (RepositoryException e) {
			throw new IllegalStateException("Cannot add JCR observer", e);
		}

		return parent;
	}

	protected void refreshUi(Composite parent, Node context) throws RepositoryException {
		CmsSwtUtils.clear(parent);
		Query query = context.getSession().getWorkspace().getQueryManager()
				.createQuery("SELECT * FROM [" + EntityType.geopoint.get() + "]", Query.JCR_SQL2);
		List<Node> geoPoints = JcrUtils.nodeIteratorToList(query.execute().getNodes());
		OpenLayersMap map = new OpenLayersMap(parent, SWT.NONE, getClass().getResource("map-osm.html"));
		map.setLayoutData(CmsSwtUtils.fillAll());

		// apafMap.setZoom(7);
		// apafMap.setCenter(-2.472, 8.010);
		map.addPoints(geoPoints);
	}
}
