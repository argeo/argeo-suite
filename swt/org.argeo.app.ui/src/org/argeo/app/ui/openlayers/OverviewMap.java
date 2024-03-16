package org.argeo.app.ui.openlayers;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.query.Query;

import org.argeo.api.acr.Content;
import org.argeo.api.app.EntityType;
import org.argeo.api.cms.CmsConstants;
import org.argeo.cms.jcr.acr.JcrContentProvider;
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
	private JcrContentProvider jcrContentProvider;

	@Override
	public Control createUiPart(Composite parent, Content context) {
		parent.setLayout(new GridLayout());
		Session session = jcrContentProvider.getJcrSession(context, CmsConstants.SYS_WORKSPACE);

		try {
			refreshUi(parent, session);
			String[] nodeTypes = { EntityType.geopoint.get() };
			session.getWorkspace().getObservationManager().addEventListener(new EventListener() {

				@Override
				public void onEvent(EventIterator events) {
					if (!parent.isDisposed())
						parent.getDisplay().asyncExec(() -> {
							try {
								refreshUi(parent, session);
							} catch (RepositoryException e) {
								throw new JcrException(e);
							}
						});
				}
			}, Event.PROPERTY_CHANGED | Event.NODE_ADDED | Event.NODE_REMOVED | Event.PROPERTY_ADDED, "/", true, null,
					nodeTypes, false);
		} catch (RepositoryException e) {
			throw new JcrException("Cannot add JCR observer", e);
		}

		return parent;
	}

	protected void refreshUi(Composite parent, Session session) throws RepositoryException {
		CmsSwtUtils.clear(parent);
		Query query = session.getWorkspace().getQueryManager()
				.createQuery("SELECT * FROM [" + EntityType.geopoint.get() + "]", Query.JCR_SQL2);
		List<Node> geoPoints = JcrUtils.nodeIteratorToList(query.execute().getNodes());
		OpenLayersMap map = new OpenLayersMap(parent, SWT.NONE, getClass().getResource("map-osm.html"));
		map.setLayoutData(CmsSwtUtils.fillAll());

		// apafMap.setZoom(7);
		// apafMap.setCenter(-2.472, 8.010);
		map.addPoints(geoPoints);
	}

	public void setJcrContentProvider(JcrContentProvider jcrContentProvider) {
		this.jcrContentProvider = jcrContentProvider;
	}

}
