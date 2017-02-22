package org.argeo.suite.core;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.argeo.connect.AppService;
import org.argeo.connect.activities.ActivitiesService;
import org.argeo.connect.activities.ActivitiesTypes;
import org.argeo.connect.documents.DocumentsService;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.resources.ResourcesTypes;
import org.argeo.connect.tracker.TrackerService;
import org.argeo.connect.tracker.TrackerTypes;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.suite.SuiteConstants;

public class DefaultSuiteAppService implements AppService {

	private ResourcesService resourcesService;
	private ActivitiesService activitiesService;
	private PeopleService peopleService;
	private DocumentsService documentsService;
	private TrackerService trackerService;

	@Override
	public String getAppBaseName() {
		return SuiteConstants.SUITE_APP_BASE_NAME;
	}

	@Override
	public String getDefaultRelPath(Node entity) throws RepositoryException {
		if (ConnectJcrUtils.isNodeType(entity, ResourcesTypes.RESOURCES_TAG_PARENT)
				|| ConnectJcrUtils.isNodeType(entity, ResourcesTypes.RESOURCES_NODE_TEMPLATE)
				|| ConnectJcrUtils.isNodeType(entity, ResourcesTypes.RESOURCES_ENCODED_TAG)
				|| ConnectJcrUtils.isNodeType(entity, ResourcesTypes.RESOURCES_TAG))
			return resourcesService.getDefaultRelPath(entity);
		else if (ConnectJcrUtils.isNodeType(entity, TrackerTypes.TRACKER_PROJECT))
			return trackerService.getDefaultRelPath(entity);
		else if (ConnectJcrUtils.isNodeType(entity, ActivitiesTypes.ACTIVITIES_TASK)
				|| ConnectJcrUtils.isNodeType(entity, ActivitiesTypes.ACTIVITIES_ACTIVITY))
			return activitiesService.getDefaultRelPath(entity);
		else if (ConnectJcrUtils.isNodeType(entity, PeopleTypes.PEOPLE_PERSON)
				|| ConnectJcrUtils.isNodeType(entity, PeopleTypes.PEOPLE_ORG))
			return peopleService.getDefaultRelPath(entity);
		else if (ConnectJcrUtils.isNodeType(entity, NodeType.NT_FILE)
				|| ConnectJcrUtils.isNodeType(entity, NodeType.NT_FOLDER))
			return documentsService.getDefaultRelPath(entity);
		else
			return null;
	}

	@Override
	public String getDefaultRelPath(String id) {
		return null;
	}

	/** Insures the correct service is called on save */
	@Override
	public Node saveEntity(Node entity, boolean publish) {
		if (ConnectJcrUtils.isNodeType(entity, ResourcesTypes.RESOURCES_TAG_PARENT)
				|| ConnectJcrUtils.isNodeType(entity, ResourcesTypes.RESOURCES_NODE_TEMPLATE)
				|| ConnectJcrUtils.isNodeType(entity, ResourcesTypes.RESOURCES_ENCODED_TAG)
				|| ConnectJcrUtils.isNodeType(entity, ResourcesTypes.RESOURCES_TAG))
			return resourcesService.saveEntity(entity, publish);
		else if (ConnectJcrUtils.isNodeType(entity, TrackerTypes.TRACKER_PROJECT))
			return trackerService.saveEntity(entity, publish);
		else if (ConnectJcrUtils.isNodeType(entity, ActivitiesTypes.ACTIVITIES_TASK)
				|| ConnectJcrUtils.isNodeType(entity, ActivitiesTypes.ACTIVITIES_ACTIVITY))
			return activitiesService.saveEntity(entity, publish);
		else if (ConnectJcrUtils.isNodeType(entity, PeopleTypes.PEOPLE_PERSON)
				|| ConnectJcrUtils.isNodeType(entity, PeopleTypes.PEOPLE_ORG))
			return peopleService.saveEntity(entity, publish);
		else if (ConnectJcrUtils.isNodeType(entity, NodeType.NT_FILE)
				|| ConnectJcrUtils.isNodeType(entity, NodeType.NT_FOLDER))
			return documentsService.saveEntity(entity, publish);
		else
			return AppService.super.saveEntity(entity, publish);
	}

	public void setResourcesService(ResourcesService resourcesService) {
		this.resourcesService = resourcesService;
	}

	public void setActivitiesService(ActivitiesService activitiesService) {
		this.activitiesService = activitiesService;
	}

	public void setPeopleService(PeopleService peopleService) {
		this.peopleService = peopleService;
	}

	public void setDocumentsService(DocumentsService documentsService) {
		this.documentsService = documentsService;
	}

	public void setTrackerService(TrackerService trackerService) {
		this.trackerService = trackerService;
	}
}
