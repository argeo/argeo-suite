package org.argeo.suite.workbench.parts;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.activities.ActivitiesNames;
import org.argeo.activities.ActivitiesService;
import org.argeo.cms.auth.CurrentUser;
import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.workbench.Refreshable;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
import org.argeo.node.NodeUtils;
import org.argeo.suite.workbench.AsUiPlugin;
import org.argeo.tracker.TrackerNames;
import org.argeo.tracker.TrackerService;
import org.argeo.tracker.ui.TaskListLabelProvider;
import org.argeo.tracker.ui.TaskVirtualListComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/** Argeo Suite Default Dashboard */
public class DefaultDashboardEditor extends AbstractSuiteDashboard implements Refreshable {
	final static Log log = LogFactory.getLog(DefaultDashboardEditor.class);
	public final static String ID = AsUiPlugin.PLUGIN_ID + ".defaultDashboardEditor";

	private ActivitiesService activitiesService;
	private TrackerService trackerService;

	private Composite headerCmp;
	private Composite taskListCmp;
	private TaskVirtualListComposite tvlc;

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());
		Composite bodyCmp = new Composite(parent, SWT.NO_FOCUS);
		bodyCmp.setLayoutData(EclipseUiUtils.fillAll());
		bodyCmp.setLayout(new GridLayout());

		// Header
		try {
			// Control overviewCmp =
			createUi(bodyCmp, NodeUtils.getUserHome(getSession()));
		} catch (RepositoryException e) {
			e.printStackTrace();
		}

		taskListCmp = new Composite(bodyCmp, SWT.NO_FOCUS);
		taskListCmp.setLayoutData(EclipseUiUtils.fillAll());

		populateTaskListCmp();
	}

	private void populateTaskListCmp() {
		CmsUtils.clear(taskListCmp);
		taskListCmp.setLayout(EclipseUiUtils.noSpaceGridLayout());
		// Composite innerCmp = new Composite(taskListCmp, SWT.NO_FOCUS);
		// innerCmp.setLayoutData(EclipseUiUtils.fillAll());

		TaskListLabelProvider labelProvider = new TaskListLabelProvider(trackerService);
		tvlc = new TaskVirtualListComposite(taskListCmp, SWT.NO_FOCUS, labelProvider, 54);
		tvlc.setLayoutData(EclipseUiUtils.fillAll());
		forceRefresh(null);
	}

	@Override
	public void forceRefresh(Object object) {
		NodeIterator nit = activitiesService.getMyTasks(getSession(), true);
		tvlc.getTableViewer().setInput(JcrUtils.nodeIteratorToList(nit).toArray());
	}

	private Control createUi(Composite parent, Node context) throws RepositoryException {
		Composite bodyCmp = new Composite(parent, SWT.NO_FOCUS);
		bodyCmp.setLayout(new GridLayout());

		// Title
		Label titleLbl = new Label(bodyCmp, SWT.WRAP | SWT.LEAD);
		CmsUtils.markup(titleLbl);
		String titleStr = "<big><b> Hello " + CurrentUser.getDisplayName() + " </b></big>";
		titleLbl.setText(titleStr);
		GridData gd = new GridData(SWT.CENTER, SWT.BOTTOM, false, false);
		gd.verticalIndent = 5;
		gd.horizontalIndent = 10;
		titleLbl.setLayoutData(gd);

		Calendar now = GregorianCalendar.getInstance();

		NodeIterator nit = activitiesService.getMyTasks(getSession(), true);
		if (nit.hasNext()) {
			List<Node> overdueTasks = new ArrayList<>();
			while (nit.hasNext()) {
				Node currNode = nit.nextNode();
				if (currNode.hasProperty(ActivitiesNames.ACTIVITIES_DUE_DATE)
						&& currNode.getProperty(ActivitiesNames.ACTIVITIES_DUE_DATE).getDate().before(now))
					overdueTasks.add(currNode);
			}
			if (!overdueTasks.isEmpty()) {
				Label overdueLbl = new Label(bodyCmp, SWT.WRAP | SWT.LEAD);
				CmsUtils.markup(overdueLbl);
				long size = overdueTasks.size();
				String overdueStr = "You have " + size + " overdue task" + (size > 1 ? "s" : "") + ".";
				overdueLbl.setText(overdueStr);
			}
		}

		nit = trackerService.getMyMilestones(getSession(), true);
		if (nit.hasNext()) {
			List<Node> overdueMilestones = new ArrayList<>();
			while (nit.hasNext()) {
				Node currNode = nit.nextNode();
				if (currNode.hasProperty(TrackerNames.TRACKER_TARGET_DATE)
						&& currNode.getProperty(TrackerNames.TRACKER_TARGET_DATE).getDate().before(now))
					overdueMilestones.add(currNode);
			}
			if (!overdueMilestones.isEmpty()) {
				Label overdueLbl = new Label(bodyCmp, SWT.WRAP | SWT.LEAD);
				CmsUtils.markup(overdueLbl);
				long size = overdueMilestones.size();
				String overdueStr = "You have " + size + " overdue milestone" + (size > 1 ? "s" : "") + ".";
				overdueLbl.setText(overdueStr);
			}
		}
		return bodyCmp;
	}

	@Override
	public void setFocus() {
		// refreshDocListGadget();
	}

	public void setActivitiesService(ActivitiesService activitiesService) {
		this.activitiesService = activitiesService;
	}

	public void setTrackerService(TrackerService trackerService) {
		this.trackerService = trackerService;
	}
}
