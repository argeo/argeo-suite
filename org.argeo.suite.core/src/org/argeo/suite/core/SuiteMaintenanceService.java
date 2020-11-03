package org.argeo.suite.core;

import java.io.IOException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.entity.EntityNames;
import org.argeo.entity.EntityType;
import org.argeo.maintenance.AbstractMaintenanceService;

/** Initialises an Argeo Suite backend. */
public class SuiteMaintenanceService extends AbstractMaintenanceService {

	@Override
	public boolean prepareJcrTree(Session adminSession) throws RepositoryException, IOException {
		boolean modified = false;
		Node rootNode = adminSession.getRootNode();
		if (!rootNode.hasNode(EntityNames.TERM_BASE)) {
			rootNode.addNode(EntityNames.TERM_BASE, EntityType.typologies.get());
			modified = true;
		}
		if (modified)
			adminSession.save();
		return modified;
	}

}
