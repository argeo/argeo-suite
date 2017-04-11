package org.argeo.suite.core;

import java.util.List;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.security.Privilege;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.AppMaintenanceService;
import org.argeo.connect.SystemMaintenanceService;
import org.argeo.jcr.JcrUtils;
import org.argeo.node.NodeConstants;
import org.argeo.suite.ArgeoSuiteRole;
import org.argeo.suite.SuiteException;

/** Make the DJay-ing to provide a full running Suite platform */
public class DefaultSuiteMaintenanceService implements SystemMaintenanceService {
	private final static Log log = LogFactory.getLog(DefaultSuiteMaintenanceService.class);

	/* DEPENDENCY INJECTION */
	private Repository repository;
	private String workspaceName;
	private List<AppMaintenanceService> maintenanceServices;

	public void init() {
		Session adminSession = null;
		try {
			adminSession = repository.login(workspaceName);
			if (prepareJcrTree(adminSession)) {
				configurePrivileges(adminSession);
			}
		} catch (Exception e) {
			throw new SuiteException("Cannot initialise model", e);
		} finally {
			JcrUtils.logoutQuietly(adminSession);
		}
	}

	// To be cleaned once first init and config mechanisms have been implemented
	// private final static String publicPath = "/public";
	// FIXME Users must have read access on the jcr:system/jcr:versionStorage
	// node under JackRabbit to be able to manage versions
	private final static String jackRabbitVersionSystemPath = "/jcr:system";

	@Override
	public boolean prepareJcrTree(Session session) {
		boolean hasCHanged = false;
		try {
			// JcrUtils.mkdirs(session, publicPath, NodeType.NT_UNSTRUCTURED);
			if (session.hasPendingChanges()) {
				session.save();
				hasCHanged = true;
			}
		} catch (RepositoryException e) {
			throw new SuiteException("Cannot build model", e);
		}
		for (AppMaintenanceService service : maintenanceServices)
			hasCHanged |= service.prepareJcrTree(session);
		if (hasCHanged)
			log.info("Repository has been initialised with Argeo Suite model");
		return hasCHanged;
	}

	@Override
	public void configurePrivileges(Session session) {
		try {
			// Remove unused default JCR rights
			JcrUtils.clearAccessControList(session, "/", "everyone");

			JcrUtils.addPrivilege(session, jackRabbitVersionSystemPath, ArgeoSuiteRole.coworker.dn(),
					Privilege.JCR_READ);
			// Default configuration of the workspace
			JcrUtils.addPrivilege(session, "/", NodeConstants.ROLE_ADMIN, Privilege.JCR_ALL);
			// JcrUtils.addPrivilege(session, publicPath, NodeConstants.ROLE_USER, Privilege.JCR_READ);
			// JcrUtils.addPrivilege(session, publicPath, "anonymous", Privilege.JCR_READ);
			// JcrUtils.addPrivilege(session, publicPath, NodeConstants.ROLE_ANONYMOUS, Privilege.JCR_READ);

			session.save();
		} catch (RepositoryException e) {
			throw new SuiteException("Cannot build model", e);
		}
		for (AppMaintenanceService service : maintenanceServices)
			service.configurePrivileges(session);
		log.info("Access control configured");
	}

	public void destroy() {
	}

	/* DEPENDENCY INJECTION */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setWorkspaceName(String workspaceName) {
		this.workspaceName = workspaceName;
	}

	public void setMaintenanceServices(List<AppMaintenanceService> maintenanceServices) {
		this.maintenanceServices = maintenanceServices;
	}
}
