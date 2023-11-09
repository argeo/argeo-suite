package org.argeo.app.jcr;

import java.util.Map;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.app.api.EntityConstants;
import org.argeo.app.api.EntityDefinition;
import org.argeo.cms.jcr.CmsJcrUtils;
import org.argeo.jcr.Jcr;
import org.osgi.framework.BundleContext;

/** An entity definition based on a JCR data structure. */
@Deprecated
public class JcrEntityDefinition implements EntityDefinition {
	private Repository repository;

	private String type;
//	private String defaultEditorId;

	public void init(BundleContext bundleContext, Map<String, String> properties) throws RepositoryException {
		Session adminSession = CmsJcrUtils.openDataAdminSession(repository, null);
		try {
			type = properties.get(EntityConstants.TYPE);
			if (type == null)
				throw new IllegalArgumentException("Entity type property " + EntityConstants.TYPE + " must be set.");
//			defaultEditorId = properties.get(EntityConstants.DEFAULT_EDITOR_ID);
//			String definitionPath = EntityNames.ENTITY_DEFINITIONS_PATH + '/' + type;
//			if (!adminSession.itemExists(definitionPath)) {
//				Node entityDefinition = JcrUtils.mkdirs(adminSession, definitionPath, EntityTypes.ENTITY_DEFINITION);
////				entityDefinition.addMixin(EntityTypes.ENTITY_DEFINITION);
//				adminSession.save();
//			}
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

//	@Override
//	public String getEditorId(Node entity) {
//		return defaultEditorId;
//	}

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
