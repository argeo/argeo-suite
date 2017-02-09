package org.argeo.suite.people;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

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
import org.argeo.connect.people.core.imports.EncodedTagCsvFileParser;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.jcr.JcrUtils;
import org.argeo.node.NodeConstants;
import org.argeo.suite.ArgeoSuiteRole;
import org.argeo.suite.SuiteException;
import org.springframework.core.io.Resource;

/**
 * Default implementation of an Argeo Suite specific People Backend
 * 
 * TODO refactor and clean init process and service dependencies
 */
public class PeopleSuiteServiceImpl extends PeopleServiceImpl implements PeopleService, PeopleConstants {
	private final static Log log = LogFactory.getLog(PeopleSuiteServiceImpl.class);

	/* DEPENDENCY INJECTION */
	private Repository repository;
	private String workspaceName;
	private Map<String, Resource> initResources = null;

	public void init() {
		super.init();
		Session adminSession = null;
		try {
			adminSession = repository.login(workspaceName);
			initialiseModel(adminSession);
			initModelResources(adminSession);
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
	protected void initialiseModel(Session adminSession) throws RepositoryException {
		super.initialiseModel(adminSession);

		JcrUtils.mkdirs(adminSession, publicPath, NodeType.NT_UNSTRUCTURED);
		if (adminSession.hasPendingChanges()) {
			adminSession.save();
			configureACL(adminSession);
			log.info("Repository has been initialised with Argeo Suite model");
		}
	}

	// First draft of configuration of the people specific rights
	private void configureACL(Session session) throws RepositoryException {

		// Initialise people
		JcrUtils.addPrivilege(session, getBasePath(null), ArgeoSuiteRole.coworker.dn(), Privilege.JCR_ALL);
		JcrUtils.addPrivilege(session, jackRabbitVersionSystemPath, ArgeoSuiteRole.coworker.dn(), Privilege.JCR_READ);

		// Default configuration of the workspace
		JcrUtils.addPrivilege(session, "/", NodeConstants.ROLE_ADMIN, Privilege.JCR_ALL);
		JcrUtils.addPrivilege(session, publicPath, NodeConstants.ROLE_USER, Privilege.JCR_READ);
		JcrUtils.addPrivilege(session, publicPath, "anonymous", Privilege.JCR_READ);
		JcrUtils.addPrivilege(session, publicPath, NodeConstants.ROLE_ANONYMOUS, Privilege.JCR_READ);

		session.save();
		log.info("Access control configured");
	}

	/**
	 * Initialises People resource model and optionally imports legacy resources
	 */
	protected void initModelResources(Session adminSession) {
		try {
			// initialisation
			ResourceService resourceService = getResourceService();
			resourceService.initialiseResources(adminSession);

			Resource resource = initResources.get("Countries");
			if (resourceService.getTagLikeResourceParent(adminSession, PeopleConstants.RESOURCE_COUNTRY) == null
					&& resource != null) {
				resourceService.createTagLikeResourceParent(adminSession, PeopleConstants.RESOURCE_COUNTRY,
						PeopleTypes.PEOPLE_TAG_ENCODED_INSTANCE, PeopleNames.PEOPLE_CODE, getBasePath(null),
						ConnectJcrUtils.getLocalJcrItemName(NodeType.NT_UNSTRUCTURED), new ArrayList<String>());
				String EN_SHORT_NAME = "English short name (upper-lower case)";
				String ISO_CODE = "Alpha-2 code";
				new EncodedTagCsvFileParser(resourceService, adminSession, PeopleConstants.RESOURCE_COUNTRY, ISO_CODE,
						EN_SHORT_NAME).parse(resource.getInputStream(), "UTF-8");
			}

			resource = initResources.get("Languages");
			if (resourceService.getTagLikeResourceParent(adminSession, PeopleConstants.RESOURCE_LANG) == null
					&& resource != null) {
				resourceService.createTagLikeResourceParent(adminSession, PeopleConstants.RESOURCE_LANG,
						PeopleTypes.PEOPLE_TAG_ENCODED_INSTANCE, PeopleNames.PEOPLE_CODE, getBasePath(null),
						ConnectJcrUtils.getLocalJcrItemName(NodeType.NT_UNSTRUCTURED), new ArrayList<String>());
				String EN_SHORT_NAME = "Language name";
				String ISO_CODE = "639-1";
				new EncodedTagCsvFileParser(resourceService, adminSession, PeopleConstants.RESOURCE_LANG, ISO_CODE,
						EN_SHORT_NAME).parse(resource.getInputStream(), "UTF-8");
			}

			// Create tag & mailing list parents
			if (resourceService.getTagLikeResourceParent(adminSession, PeopleConstants.RESOURCE_TAG) == null)
				resourceService.createTagLikeResourceParent(adminSession, PeopleConstants.RESOURCE_TAG,
						PeopleTypes.PEOPLE_TAG_INSTANCE, null, getBasePath(null), PeopleTypes.PEOPLE_ENTITY,
						PeopleNames.PEOPLE_TAGS);
			if (resourceService.getTagLikeResourceParent(adminSession, PeopleTypes.PEOPLE_MAILING_LIST) == null)
				resourceService.createTagLikeResourceParent(adminSession, null, PeopleTypes.PEOPLE_MAILING_LIST, null,
						getBasePath(null), PeopleTypes.PEOPLE_ENTITY, PeopleNames.PEOPLE_MAILING_LISTS);

			// Initialise catalogues
			importCatalogue(adminSession, initResources.get("SimpleTasks"), PeopleTypes.PEOPLE_TASK);

			if (adminSession.hasPendingChanges()) {
				adminSession.save();
				log.info("Resources have been added to Argeo Suite model");
			}
		} catch (IOException | RepositoryException e) {
			throw new SuiteException("Cannot initialise resources ", e);
		}
	}

	/** Give access to the repository to extending classes */
	protected Repository getRepository() {
		return repository;
	}

	protected String getWorkspaceName() {
		return workspaceName;
	}

	/* DEPENDENCY INJECTION */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setWorkspaceName(String workspaceName) {
		this.workspaceName = workspaceName;
	}

	public void setInitResources(Map<String, Resource> initResources) {
		this.initResources = initResources;
	}
}
