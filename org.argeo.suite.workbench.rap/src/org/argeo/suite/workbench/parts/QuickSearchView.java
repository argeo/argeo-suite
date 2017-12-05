package org.argeo.suite.workbench.parts;

import static org.argeo.eclipse.ui.EclipseUiUtils.notEmpty;

import javax.jcr.NodeIterator;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.activities.ActivitiesService;
import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.ConnectTypes;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.ui.ConnectImages;
import org.argeo.connect.ui.ConnectUiConstants;
import org.argeo.connect.ui.util.BasicNodeListContentProvider;
import org.argeo.connect.ui.widgets.DelayedText;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.util.XPathUtils;
import org.argeo.connect.workbench.Refreshable;
import org.argeo.connect.workbench.SystemWorkbenchService;
import org.argeo.connect.workbench.util.JcrViewerDClickListener;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
import org.argeo.people.PeopleService;
import org.argeo.suite.workbench.AsUiPlugin;
import org.argeo.suite.workbench.SuiteWorkbenchException;
import org.argeo.suite.workbench.internal.EntitySingleColumnLabelProvider;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.rap.rwt.service.ServerPushSession;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

/** A table with a quick search field. */
public class QuickSearchView extends ViewPart implements Refreshable {
	private final static Log log = LogFactory.getLog(QuickSearchView.class);
	public static final String ID = AsUiPlugin.PLUGIN_ID + ".quickSearchView";

	/* DEPENDENCY INJECTION */
	private Repository repository;
	private Session session;
	private ResourcesService resourcesService;
	private ActivitiesService activitiesService;
	private PeopleService peopleService;
	private SystemWorkbenchService systemWorkbenchService;

	// This page widgets
	private TableViewer entityViewer;
	private Text filterTxt;

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		setTitleImage(ConnectImages.SEARCH);
	}

	@Override
	public void createPartControl(Composite parent) {
		session = ConnectJcrUtils.login(repository);
		// MainLayout
		parent.setLayout(new GridLayout());
		addFilterPanel(parent);
		entityViewer = createListPart(parent, new EntitySingleColumnLabelProvider(resourcesService, activitiesService,
				peopleService, systemWorkbenchService));
		refreshFilteredList();
	}

	public void addFilterPanel(Composite parent) {
		// Use a delayed text: the query won't be done until the user stop
		// typing for 800ms
		int style = SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL;
		DelayedText delayedText = new DelayedText(parent, style, ConnectUiConstants.SEARCH_TEXT_DELAY);
		filterTxt = delayedText.getText();
		filterTxt.setLayoutData(EclipseUiUtils.fillWidth());

		final ServerPushSession pushSession = new ServerPushSession();
		delayedText.addDelayedModifyListener(pushSession, new ModifyListener() {
			private static final long serialVersionUID = 5003010530960334977L;

			public void modifyText(ModifyEvent event) {
				delayedText.getText().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						refreshFilteredList();
					}
				});
				pushSession.stop();
			}
		});

		// Jump to the first item of the list using the down arrow
		filterTxt.addKeyListener(new KeyListener() {
			private static final long serialVersionUID = -4523394262771183968L;

			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// boolean shiftPressed = (e.stateMask & SWT.SHIFT) != 0;
				// boolean altPressed = (e.stateMask & SWT.ALT) != 0;
				if (e.keyCode == SWT.ARROW_DOWN || e.keyCode == SWT.TAB) {
					Object first = entityViewer.getElementAt(0);
					if (first != null) {
						entityViewer.getTable().setFocus();
						entityViewer.setSelection(new StructuredSelection(first), true);
					}
					e.doit = false;
				}
			}
		});
	}

	protected TableViewer createListPart(Composite parent, ILabelProvider labelProvider) {
		parent.setLayout(new GridLayout());

		Composite tableComposite = new Composite(parent, SWT.NONE);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_VERTICAL
				| GridData.VERTICAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		tableComposite.setLayoutData(gd);

		TableViewer v = new TableViewer(tableComposite);
		v.setLabelProvider(labelProvider);

		TableColumn singleColumn = new TableColumn(v.getTable(), SWT.V_SCROLL);
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tableColumnLayout.setColumnData(singleColumn, new ColumnWeightData(85));
		tableComposite.setLayout(tableColumnLayout);

		// Corresponding table & style
		Table table = v.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(false);
		CmsUtils.markup(table);
		CmsUtils.setItemHeight(table, 26);

		v.setContentProvider(new BasicNodeListContentProvider());
		v.addDoubleClickListener(new JcrViewerDClickListener());
		return v;
	}

	@Override
	public void dispose() {
		JcrUtils.logoutQuietly(session);
		super.dispose();
	}

	@Override
	public void setFocus() {
		refreshFilteredList();
		filterTxt.setFocus();
	}

	@Override
	public void forceRefresh(Object object) {
		refreshFilteredList();
	}

	protected void refreshFilteredList() {
		try {
			String filter = filterTxt.getText();
			// Prevents the query on the full repository
			// if (isEmpty(filter)) {
			// entityViewer.setInput(null);
			// return;
			// }

			// XPATH Query
			String xpathQueryStr = "//element(*, " + ConnectTypes.CONNECT_ENTITY + ")";
			String xpathFilter = XPathUtils.getFreeTextConstraint(filter);
			if (notEmpty(xpathFilter))
				xpathQueryStr += "[" + xpathFilter + "]";

			// boolean doOrder = orderResultsBtn != null
			// && !(orderResultsBtn.isDisposed())
			// && orderResultsBtn.getSelection();
			// if (doOrder) {
			// xpathQueryStr += " order by jcr:title";
			// }

			long begin = System.currentTimeMillis();
			Query xpathQuery = XPathUtils.createQuery(session, xpathQueryStr);

			xpathQuery.setLimit(ConnectUiConstants.SEARCH_DEFAULT_LIMIT);
			QueryResult result = xpathQuery.execute();

			NodeIterator nit = result.getNodes();
			entityViewer.setInput(JcrUtils.nodeIteratorToList(nit));
			if (log.isDebugEnabled()) {
				long end = System.currentTimeMillis();
				log.debug("Quick Search - Found: " + nit.getSize() + " in " + (end - begin)
						+ " ms by executing XPath query (" + xpathQueryStr + ").");
			}
		} catch (RepositoryException e) {
			throw new SuiteWorkbenchException("Unable to list entities", e);
		}
	}

	/* DEPENDENCY INJECTION */
	public void setRepository(Repository repository) {
		this.repository = repository;
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

	public void setSystemWorkbenchService(SystemWorkbenchService systemWorkbenchService) {
		this.systemWorkbenchService = systemWorkbenchService;
	}
}
