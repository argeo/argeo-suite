package org.argeo.suite.core;

import java.io.IOException;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.maintenance.AbstractMaintenanceService;

/** Initialises an Argeo Suite backend. */
public class SuiteMaintenanceService extends AbstractMaintenanceService {

	@Override
	public boolean prepareJcrTree(Session adminSession) throws RepositoryException, IOException {
		//EntityJcrUtils.getOrAddFormFolder(adminSession.getRootNode(), EntityNames.FORM_BASE);
		return adminSession.hasPendingChanges();
	}

}
