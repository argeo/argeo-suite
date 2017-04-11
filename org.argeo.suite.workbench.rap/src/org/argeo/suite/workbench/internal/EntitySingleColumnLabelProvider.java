package org.argeo.suite.workbench.internal;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.activities.ActivitiesService;
import org.argeo.activities.ActivitiesTypes;
import org.argeo.activities.ui.ActivityListLabelProvider;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.ui.ConnectUiConstants;
import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.ui.util.TagLabelProvider;
import org.argeo.connect.workbench.SystemWorkbenchService;
import org.argeo.people.PeopleException;
import org.argeo.people.PeopleNames;
import org.argeo.people.PeopleService;
import org.argeo.people.PeopleTypes;
import org.argeo.people.workbench.rap.providers.GroupLabelProvider;
import org.argeo.people.workbench.rap.providers.OrgListLabelProvider;
import org.argeo.people.workbench.rap.providers.PersonListLabelProvider;
import org.argeo.tracker.TrackerTypes;
import org.argeo.tracker.ui.TrackerSingleColLP;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Provide a single column label provider for entity lists. Icon and displayed
 * text vary with the element node type
 */
public class EntitySingleColumnLabelProvider extends LabelProvider implements PeopleNames {
	private static final long serialVersionUID = 3111885324210673320L;

	private SystemWorkbenchService systemWorkbenchService;

	private ActivityListLabelProvider activityLP;
	private TrackerSingleColLP trackerLP;
	private OrgListLabelProvider orgLp;
	private PersonListLabelProvider personLp;
	private GroupLabelProvider groupLp = new GroupLabelProvider(ConnectUiConstants.LIST_TYPE_SMALL);
	private TagLabelProvider mlInstanceLp;

	public EntitySingleColumnLabelProvider(ResourcesService resourceService, ActivitiesService activitiesService,
			PeopleService peopleService, SystemWorkbenchService systemWorkbenchService) {
		this.systemWorkbenchService = systemWorkbenchService;
		activityLP = new ActivityListLabelProvider(activitiesService);
		trackerLP = new TrackerSingleColLP(activitiesService);
		personLp = new PersonListLabelProvider(peopleService);
		orgLp = new OrgListLabelProvider(resourceService, peopleService);
		mlInstanceLp = new TagLabelProvider(resourceService, ConnectUiConstants.LIST_TYPE_SMALL);
	}

	@Override
	public String getText(Object element) {
		try {
			Node entity = (Node) element;
			String result;

			if (entity.isNodeType(TrackerTypes.TRACKER_TASK) || entity.isNodeType(TrackerTypes.TRACKER_PROJECT)
					|| entity.isNodeType(TrackerTypes.TRACKER_MILESTONE))
				result = trackerLP.getText(element);
			else if (entity.isNodeType(ActivitiesTypes.ACTIVITIES_ACTIVITY))
				result = activityLP.getText(element);
			else if (entity.isNodeType(PeopleTypes.PEOPLE_PERSON))
				result = personLp.getText(element);
			else if (entity.isNodeType(PeopleTypes.PEOPLE_ORG))
				result = orgLp.getText(element);
			else if (entity.isNodeType(PeopleTypes.PEOPLE_MAILING_LIST))
				result = mlInstanceLp.getText(element);
			else if (entity.isNodeType(PeopleTypes.PEOPLE_GROUP))
				result = groupLp.getText(element);
			else
				result = "";
			return ConnectUiUtils.replaceAmpersand(result);
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to get formatted value for node", re);
		}
	}

	/** Overwrite this method to provide project specific images */
	@Override
	public Image getImage(Object element) {
		return systemWorkbenchService.getIconForType((Node) element);
	}
}
