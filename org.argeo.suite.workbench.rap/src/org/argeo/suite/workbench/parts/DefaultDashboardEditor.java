package org.argeo.suite.workbench.parts;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.activities.ActivitiesNames;
import org.argeo.activities.ActivitiesService;
import org.argeo.cms.auth.CurrentUser;
import org.argeo.cms.ui.workbench.util.CommandUtils;
import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.ConnectNames;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.workbench.Refreshable;
import org.argeo.connect.workbench.commands.OpenEntityEditor;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
import org.argeo.node.NodeUtils;
import org.argeo.suite.SuiteException;
import org.argeo.suite.workbench.AsUiPlugin;
import org.argeo.tracker.TrackerNames;
import org.argeo.tracker.TrackerService;
import org.argeo.tracker.core.TrackerUtils;
import org.argeo.tracker.ui.TaskListLabelProvider;
import org.argeo.tracker.ui.TaskVirtualListComposite;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;

/** Argeo Suite Default Dashboard */
public class DefaultDashboardEditor extends AbstractSuiteDashboard implements Refreshable {
	final static Log log = LogFactory.getLog(DefaultDashboardEditor.class);
	public final static String ID = AsUiPlugin.PLUGIN_ID + ".defaultDashboardEditor";

	private ActivitiesService activitiesService;
	private TrackerService trackerService;

	private String datePattern = "dd MMM yyyy";

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
			headerCmp = createHeaderPart(bodyCmp, NodeUtils.getUserHome(getSession()));
			headerCmp.setLayoutData(EclipseUiUtils.fillWidth());

		} catch (RepositoryException e) {
			throw new SuiteException("Cannot create dashboard overview", e);
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

	private Composite createHeaderPart(Composite parent, Node context) throws RepositoryException {
		Composite bodyCmp = new Composite(parent, SWT.NO_FOCUS);
		bodyCmp.setLayout(EclipseUiUtils.noSpaceGridLayout(new GridLayout(2, true)));

		Composite leftCmp = new Composite(bodyCmp, SWT.NO_FOCUS);
		leftCmp.setLayout(new GridLayout());
		leftCmp.setLayoutData(EclipseUiUtils.fillWidth());
		Composite rightCmp = new Composite(bodyCmp, SWT.NO_FOCUS);
		rightCmp.setLayout(new GridLayout());
		rightCmp.setLayoutData(EclipseUiUtils.fillWidth());

		// Title
		Label titleLbl = new Label(leftCmp, SWT.WRAP | SWT.LEAD);
		CmsUtils.markup(titleLbl);
		String titleStr = "<big><b> Hello " + CurrentUser.getDisplayName() + " </b></big>";
		titleLbl.setText(titleStr);
		GridData gd = new GridData(SWT.TOP, SWT.BOTTOM, false, false);
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
				Composite overdueCmp = new Composite(leftCmp, SWT.NO_FOCUS);
				long size = overdueTasks.size();
				String overdueStr = "You have " + size + " overdue task" + (size > 1 ? "s" : "") + ": ";
				populateMuliValueClickableList(overdueCmp, overdueTasks.toArray(new Node[0]), new TaskLp(), overdueStr);
			}
		}

		nit = trackerService.getMyMilestones(getSession(), true);
		List<Node> openMilestones = new ArrayList<>();

		if (nit.hasNext()) {
			List<Node> overdueMilestones = new ArrayList<>();
			while (nit.hasNext()) {
				Node currNode = nit.nextNode();
				openMilestones.add(currNode);
				if (currNode.hasProperty(TrackerNames.TRACKER_TARGET_DATE)
						&& currNode.getProperty(TrackerNames.TRACKER_TARGET_DATE).getDate().before(now))
					overdueMilestones.add(currNode);
			}
			if (!overdueMilestones.isEmpty()) {
				Composite overdueCmp = new Composite(leftCmp, SWT.NO_FOCUS);
				long size = overdueMilestones.size();
				String overdueStr = "You have " + size + " overdue milestone" + (size > 1 ? "s" : "") + ": ";
				populateMuliValueClickableList(overdueCmp, overdueMilestones.toArray(new Node[0]), new MilestoneLp(),
						overdueStr);
			}
		}

		// My projects
		List<Node> openProjects = JcrUtils.nodeIteratorToList(trackerService.getMyProjects(getSession(), true));
		if (!openProjects.isEmpty()) {
			Group myProjectsGp = new Group(rightCmp, SWT.NO_FOCUS);
			myProjectsGp.setText("My open projects");
			myProjectsGp.setLayoutData(EclipseUiUtils.fillWidth());
			populateMuliValueClickableList(myProjectsGp, openProjects.toArray(new Node[0]), new ProjectLp(), null);
		}

		// My Milestones
		if (!openMilestones.isEmpty()) {
			Group myMilestoneGp = new Group(rightCmp, SWT.NO_FOCUS);
			myMilestoneGp.setText("My open milestones");
			myMilestoneGp.setLayoutData(EclipseUiUtils.fillWidth());
			populateMuliValueClickableList(myMilestoneGp, openMilestones.toArray(new Node[0]), new MilestoneLp(), null);
		}
		return bodyCmp;
	}

	private class ProjectLp extends ColumnLabelProvider {
		private static final long serialVersionUID = 7231233932794865555L;

		@Override
		public String getText(Object element) {
			Node project = (Node) element;

			String percent;
			NodeIterator nit = TrackerUtils.getIssues(project, null, null, null, true);
			long openNb = nit.getSize();

			nit = TrackerUtils.getIssues(project, null, null, null, false);
			long allNb = nit.getSize();

			if (allNb < 1)
				percent = "empty";
			else {
				double result = (allNb - openNb) / allNb * 100;
				percent = String.format("%.1f", result) + "% done";
			}
			StringBuilder builder = new StringBuilder();
			builder.append("<a>").append(ConnectJcrUtils.get(project, Property.JCR_TITLE)).append("</a>");
			builder.append(" (").append(percent).append(")");

			return builder.toString();
		}
	}

	private class MilestoneLp extends ColumnLabelProvider {
		private static final long serialVersionUID = 7231233932794865555L;

		@Override
		public String getText(Object element) {
			Node milestone = (Node) element;
			Node project = TrackerUtils.getRelatedProject(trackerService, milestone);
			String dueDate = ConnectJcrUtils.getDateFormattedAsString(milestone, TrackerNames.TRACKER_TARGET_DATE,
					datePattern);

			String percent;
			String propName = TrackerNames.TRACKER_MILESTONE_UID;
			String muid = ConnectJcrUtils.get(milestone, ConnectNames.CONNECT_UID);
			NodeIterator nit = TrackerUtils.getIssues(project, null, propName, muid, true);
			long openNb = nit.getSize();

			nit = TrackerUtils.getIssues(project, null, propName, muid, false);
			long allNb = nit.getSize();

			if (allNb < 1)
				percent = "empty";
			else {
				double result = (allNb - openNb) / allNb * 100;
				percent = String.format("%.1f", result) + "% done";
			}
			StringBuilder builder = new StringBuilder();
			builder.append("<a>").append(ConnectJcrUtils.get(milestone, Property.JCR_TITLE)).append("</a>");
			builder.append(" (");
			if (EclipseUiUtils.notEmpty(dueDate))
				builder.append("due to ").append(dueDate).append(", ");

			builder.append(percent).append(")");
			return builder.toString();
		}

		@Override
		public Color getForeground(Object element) {
			Node milestone = (Node) element;
			Calendar dueDate = ConnectJcrUtils.getDateValue(milestone, TrackerNames.TRACKER_TARGET_DATE);
			if (dueDate != null && dueDate.before(Calendar.getInstance()))
				return Display.getCurrent().getSystemColor(SWT.COLOR_RED);
			return null;
		}
	}

	private class TaskLp extends ColumnLabelProvider {
		private static final long serialVersionUID = 7231233932794865555L;

		@Override
		public String getText(Object element) {
			Node task = (Node) element;
			String dueDate = ConnectJcrUtils.getDateFormattedAsString(task, ActivitiesNames.ACTIVITIES_DUE_DATE,
					datePattern);

			StringBuilder builder = new StringBuilder();
			builder.append("<a>").append(ConnectJcrUtils.get(task, Property.JCR_TITLE)).append("</a>");
			if (EclipseUiUtils.notEmpty(dueDate))
				builder.append(" (").append("due to ").append(dueDate).append(")");
			return builder.toString();
		}

		@Override
		public Color getForeground(Object element) {
			Node milestone = (Node) element;
			Calendar dueDate = ConnectJcrUtils.getDateValue(milestone, TrackerNames.TRACKER_TARGET_DATE);
			if (dueDate != null && dueDate.before(Calendar.getInstance()))
				return Display.getCurrent().getSystemColor(SWT.COLOR_RED);
			return null;
		}
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

	// LOCAL HELPERS
	private void populateMuliValueClickableList(Composite parent, Node[] nodes, ColumnLabelProvider lp,
			String listLabel) {
		CmsUtils.clear(parent);
		RowLayout rl = new RowLayout(SWT.HORIZONTAL);
		rl.wrap = true;
		rl.marginLeft = rl.marginTop = rl.marginBottom = 0;
		rl.marginRight = 8;
		parent.setLayout(rl);

		if (EclipseUiUtils.notEmpty(listLabel)) {
			Link link = new Link(parent, SWT.NONE);
			link.setText(listLabel);
			link.setFont(EclipseUiUtils.getBoldFont(parent));
		}

		int i = 1;
		for (Node node : nodes) {
			Link link = new Link(parent, SWT.NONE);
			CmsUtils.markup(link);
			link.setText(lp.getText(node) + (i != nodes.length ? ", " : ""));
			i++;
//			Color fc = lp.getForeground(node);
//			if (fc != null)
//				link.setForeground(fc);

			link.addSelectionListener(new SelectionAdapter() {
				private static final long serialVersionUID = 1L;

				@Override
				public void widgetSelected(final SelectionEvent event) {
					CommandUtils.callCommand(getSystemWorkbenchService().getOpenEntityEditorCmdId(),
							OpenEntityEditor.PARAM_JCR_ID, ConnectJcrUtils.getIdentifier(node));
				}
			});
		}
	}

}
