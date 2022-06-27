package org.argeo.app.core;

import java.io.IOException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.security.Privilege;

import org.argeo.api.acr.spi.ProvidedRepository;
import org.argeo.api.cms.CmsConstants;
import org.argeo.app.api.EntityType;
import org.argeo.jcr.JcrUtils;
import org.argeo.maintenance.AbstractMaintenanceService;

/** Initialises an Argeo Suite backend. */
public class SuiteMaintenanceService extends AbstractMaintenanceService {
	private ProvidedRepository contentRepository;

	@Override
	public void init() {
		super.init();

		for (SuiteContentTypes types : SuiteContentTypes.values()) {
			contentRepository.registerTypes(types.getDefaultPrefix(), types.getNamespace(),
					types.getResource() != null ? types.getResource().toExternalForm() : null);
		}
	}

	@Override
	public boolean prepareJcrTree(Session adminSession) throws RepositoryException, IOException {
		boolean modified = false;
		Node rootNode = adminSession.getRootNode();
		if (!rootNode.hasNode(EntityType.user.name())) {
			rootNode.addNode(EntityType.user.name(), NodeType.NT_UNSTRUCTURED);
			modified = true;
		}
		if (modified)
			adminSession.save();
		return modified;
	}

	@Override
	public void configurePrivileges(Session adminSession) throws RepositoryException {
		JcrUtils.addPrivilege(adminSession, EntityType.user.basePath(), CmsConstants.ROLE_USER_ADMIN,
				Privilege.JCR_ALL);
		// JcrUtils.addPrivilege(adminSession, "/", SuiteRole.coworker.dn(),
		// Privilege.JCR_READ);
	}

	public void setContentRepository(ProvidedRepository contentRepository) {
		this.contentRepository = contentRepository;
	}

}
