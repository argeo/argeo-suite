package org.argeo.suite;

import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.security.Privilege;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.AppMaintenanceService;
import org.argeo.connect.resources.ResourceService;
import org.argeo.jcr.JcrUtils;
import org.argeo.node.NodeConstants;

/** Make the DJay-ing to provide a full running Suite platform */
public class SuiteMaintenanceService implements AppMaintenanceService {
	private final static Log log = LogFactory.getLog(SuiteMaintenanceService.class);

	/* DEPENDENCY INJECTION */
	private Repository repository;
	private String workspaceName;
	private ResourceService resourceService;
	private List<AppMaintenanceService> maintenanceServices;
	private Map<String, URI> initResources = null;
	private Map<String, URI> legacyResources = null;

	public void init() {
		Session adminSession = null;
		try {
			adminSession = repository.login(workspaceName);
			if (prepareJcrTree(adminSession)) {
				configurePrivileges(adminSession);
				importResources(adminSession, null);
			}
		} catch (Exception e) {
			throw new SuiteException("Cannot initialise model", e);
		} finally {
			JcrUtils.logoutQuietly(adminSession);
		}
	}

	// TODO Hard-coded model initialisation
	// To be cleaned once first init and config mechanisms have been implemented
	private final static String publicPath = "/public";
	// FIXME Users must have read access on the jcr:system/jcr:versionStorage
	// node under JackRabbit to be able to manage versions
	private final static String jackRabbitVersionSystemPath = "/jcr:system";

	@Override
	public boolean prepareJcrTree(Session session) {
		boolean hasCHanged = false;
		try {
			JcrUtils.mkdirs(session, publicPath, NodeType.NT_UNSTRUCTURED);
			if (session.hasPendingChanges()) {
				session.save();
				hasCHanged = true;
			}
		} catch (RepositoryException e) {
			throw new SuiteException("Cannot build model", e);
		}
		for (AppMaintenanceService service : maintenanceServices)
			hasCHanged |= service.prepareJcrTree(session);
		log.info("Repository has been initialised with Argeo Suite model");
		return hasCHanged;
	}

	@Override
	public void configurePrivileges(Session session) {
		// TODO check if first init.
		try {
			JcrUtils.addPrivilege(session, jackRabbitVersionSystemPath, ArgeoSuiteRole.coworker.dn(),
					Privilege.JCR_READ);
			// Default configuration of the workspace
			JcrUtils.addPrivilege(session, "/", NodeConstants.ROLE_ADMIN, Privilege.JCR_ALL);
			JcrUtils.addPrivilege(session, publicPath, NodeConstants.ROLE_USER, Privilege.JCR_READ);
			JcrUtils.addPrivilege(session, publicPath, "anonymous", Privilege.JCR_READ);
			JcrUtils.addPrivilege(session, publicPath, NodeConstants.ROLE_ANONYMOUS, Privilege.JCR_READ);
			session.save();
		} catch (RepositoryException e) {
			throw new SuiteException("Cannot build model", e);
		}
		for (AppMaintenanceService service : maintenanceServices)
			service.configurePrivileges(session);
		log.info("Access control configured");
	}

	@Override
	public void importResources(Session session, Map<String, URI> initialResources) {
		for (AppMaintenanceService service : maintenanceServices)
			service.prepareJcrTree(session);
	}

	@Override
	public void importData(Session session, URI uri, Map<String, URI> resources) {
		for (AppMaintenanceService service : maintenanceServices)
			service.prepareJcrTree(session);

	}

	@Override
	public void doBackup(Session session, URI uri, Object resource) {
		for (AppMaintenanceService service : maintenanceServices)
			service.prepareJcrTree(session);
	}

	/* DEPENDENCY INJECTION */
	public void setMaintenanceServices(List<AppMaintenanceService> maintenanceServices) {
		this.maintenanceServices = maintenanceServices;
	}
}
