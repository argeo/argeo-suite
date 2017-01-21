package org.argeo.suite.people;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.security.Privilege;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ResourceService;
import org.argeo.connect.people.core.PeopleServiceImpl;
import org.argeo.jcr.JcrUtils;
import org.argeo.node.NodeConstants;
import org.argeo.suite.SuiteException;

/** Default implementation of an Argeo Suite specific People Backend */
public class PeopleSuiteServiceImpl extends PeopleServiceImpl implements PeopleService, PeopleConstants {
	private final static Log log = LogFactory.getLog(PeopleSuiteServiceImpl.class);

	/* DEPENDENCY INJECTION */
	private Repository repository;
	private String workspaceName;
	// private UserAdminService userAdminService;

	public void init() {
		super.init();
		Session adminSession = null;
		try {
			adminSession = repository.login(workspaceName);
			initialiseModel(adminSession);
		} catch (Exception e) {
			throw new SuiteException("Cannot initialise model", e);
		} finally {
			JcrUtils.logoutQuietly(adminSession);
		}
	}

	// HELPERS

	// TODO Hard-coded creation of default public and shared file directories
	// To be cleaned once first init and configuration mechanisms have been
	// implemented
	private final static String publicPath = "/public";
	private final static String sharedFilePath = "/sharedFiles";

	@Override
	protected void initialiseModel(Session adminSession) throws RepositoryException {
		super.initialiseModel(adminSession);

		JcrUtils.mkdirs(adminSession, publicPath, NodeType.NT_UNSTRUCTURED);
		JcrUtils.mkdirs(adminSession, sharedFilePath, NodeType.NT_FOLDER);
		initModelResources(adminSession);
		if (adminSession.hasPendingChanges()) {
			adminSession.save();
			log.info("Repository has been initialized " + "with People's model");
			configureACL(adminSession);
		}
	}

	// First draft of configuration of the people specific rights
	private void configureACL(Session session) throws RepositoryException {
		String memberGroupDn = "cn=" + PeopleConstants.ROLE_MEMBER + ",ou=roles,ou=node";
		JcrUtils.addPrivilege(session, getBasePath(null), memberGroupDn, Privilege.JCR_ALL);
		JcrUtils.addPrivilege(session, "/", NodeConstants.ROLE_ADMIN, Privilege.JCR_ALL);
		JcrUtils.addPrivilege(session, publicPath, NodeConstants.ROLE_USER, Privilege.JCR_READ);
		JcrUtils.addPrivilege(session, publicPath, "anonymous", Privilege.JCR_READ);
		JcrUtils.addPrivilege(session, publicPath, NodeConstants.ROLE_ANONYMOUS, Privilege.JCR_READ);
		JcrUtils.addPrivilege(session, sharedFilePath, NodeConstants.ROLE_USER, Privilege.JCR_ALL);
		session.save();
		log.info("Access control configured");
	}

	/**
	 * Initialises People resource model and optionally imports legacy resources
	 */
	protected void initModelResources(Session adminSession) throws RepositoryException {
		// initialisation
		ResourceService resourceService = getResourceService();
		resourceService.initialiseResources(adminSession);

		// Resource resource = initResources.get("Countries");
		// if (resourceService.getTagLikeResourceParent(adminSession,
		// PeopleConstants.RESOURCE_COUNTRY) == null
		// && resource != null) {
		// resourceService.createTagLikeResourceParent(adminSession,
		// PeopleConstants.RESOURCE_COUNTRY,
		// PeopleTypes.PEOPLE_TAG_ENCODED_INSTANCE, PeopleNames.PEOPLE_CODE,
		// getBasePath(null),
		// JcrUiUtils.getLocalJcrItemName(NodeType.NT_UNSTRUCTURED), new
		// ArrayList<String>());
		// String EN_SHORT_NAME = "English short name (upper-lower case)";
		// String ISO_CODE = "Alpha-2 code";
		// new EncodedTagCsvFileParser(resourceService, adminSession,
		// PeopleConstants.RESOURCE_COUNTRY, ISO_CODE,
		// EN_SHORT_NAME).parse(resource.getInputStream(), "UTF-8");
		// }
		//
		// resource = initResources.get("Languages");
		// if (resourceService.getTagLikeResourceParent(adminSession,
		// PeopleConstants.RESOURCE_LANG) == null
		// && resource != null) {
		// resourceService.createTagLikeResourceParent(adminSession,
		// PeopleConstants.RESOURCE_LANG,
		// PeopleTypes.PEOPLE_TAG_ENCODED_INSTANCE, PeopleNames.PEOPLE_CODE,
		// getBasePath(null),
		// JcrUiUtils.getLocalJcrItemName(NodeType.NT_UNSTRUCTURED), new
		// ArrayList<String>());
		// String EN_SHORT_NAME = "Language name";
		// String ISO_CODE = "639-1";
		// new EncodedTagCsvFileParser(resourceService, adminSession,
		// PeopleConstants.RESOURCE_LANG, ISO_CODE,
		// EN_SHORT_NAME).parse(resource.getInputStream(), "UTF-8");
		// }

		// Create tag & mailing list parents
		if (resourceService.getTagLikeResourceParent(adminSession, PeopleConstants.RESOURCE_TAG) == null)
			resourceService.createTagLikeResourceParent(adminSession, PeopleConstants.RESOURCE_TAG,
					PeopleTypes.PEOPLE_TAG_INSTANCE, null, getBasePath(null), PeopleTypes.PEOPLE_ENTITY,
					PeopleNames.PEOPLE_TAGS);
		if (resourceService.getTagLikeResourceParent(adminSession, PeopleTypes.PEOPLE_MAILING_LIST) == null)
			resourceService.createTagLikeResourceParent(adminSession, null, PeopleTypes.PEOPLE_MAILING_LIST, null,
					getBasePath(null), PeopleTypes.PEOPLE_ENTITY, PeopleNames.PEOPLE_MAILING_LISTS);

		if (adminSession.hasPendingChanges()) {
			adminSession.save();
			log.info("Resources have been added to People's model");
		}
	}

	/* DEPENDENCY INJECTION */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setWorkspaceName(String workspaceName) {
		this.workspaceName = workspaceName;
	}
}
