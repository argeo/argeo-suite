package org.argeo.entity.core;

import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.api.NodeUtils;
import org.argeo.entity.EntityConstants;
import org.argeo.entity.EntityDefinition;
import org.argeo.entity.EntityNames;
import org.argeo.entity.EntityTypes;
import org.argeo.jcr.Jcr;
import org.argeo.jcr.JcrUtils;
import org.osgi.framework.BundleContext;

/** An entity definition based on a JCR data structure. */
public class JcrEntityDefinition implements EntityDefinition {
	private Repository repository;

	private String type;
	private String defaultEditoryId;

	public void init(BundleContext bundleContext, Map<String, String> properties) throws RepositoryException {
		Session adminSession = NodeUtils.openDataAdminSession(repository, null);
		try {
			type = properties.get(EntityConstants.TYPE);
			if (type == null)
				throw new IllegalArgumentException("Entity type property " + EntityConstants.TYPE + " must be set.");
			defaultEditoryId = properties.get(EntityConstants.DEFAULT_EDITORY_ID);
			String definitionPath = EntityNames.ENTITY_DEFINITIONS_PATH + '/' + type;
			if (!adminSession.itemExists(definitionPath)) {
				Node entityDefinition = JcrUtils.mkdirs(adminSession, definitionPath);
				entityDefinition.addMixin(EntityTypes.ENTITY_DEFINITION);
				adminSession.save();
			}
			initJcr(adminSession);
		} finally {
			Jcr.logout(adminSession);
		}
	}

	/** To be overridden in order to perform additional initialisations. */
	protected void initJcr(Session adminSession) throws RepositoryException {

	}

	public void destroy(BundleContext bundleContext, Map<String, String> properties) throws RepositoryException {

	}

	@Override
	public String getEditorId(Node entity) {
		return defaultEditoryId;
	}

	@Override
	public String getType() {
		return type;
	}

	protected Repository getRepository() {
		return repository;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public String toString() {
		return "Entity Definition " + getType();
	}

}
