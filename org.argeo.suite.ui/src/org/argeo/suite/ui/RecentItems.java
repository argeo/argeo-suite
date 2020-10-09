package org.argeo.suite.ui;

import static org.argeo.eclipse.ui.EclipseUiUtils.notEmpty;

import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import org.argeo.api.NodeConstants;
import org.argeo.cms.ui.CmsTheme;
import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.cms.ui.CmsView;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.connect.ui.ConnectUiConstants;
import org.argeo.connect.ui.util.BasicNodeListContentProvider;
import org.argeo.connect.ui.widgets.DelayedText;
import org.argeo.connect.util.XPathUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.entity.EntityTypes;
import org.argeo.jcr.Jcr;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

/** List recent items. */
public class RecentItems implements CmsUiProvider {
	int SEARCH_TEXT_DELAY = 800;
	private CmsTheme theme;

	private String entityType;

	@Override
	public Control createUi(Composite parent, Node context) throws RepositoryException {
		theme = CmsTheme.getCmsTheme(parent);
		parent.setLayout(new GridLayout());

//		Composite top = new Composite(parent, SWT.NONE);
//		top.setLayoutData(CmsUiUtils.fillWidth());
//		top.setLayout(new GridLayout(2, false));

		Label lbl = new Label(parent, SWT.NONE);
//		search.setImage(SuiteIcon.search.getSmallIcon(theme));
		lbl.setText(SuiteMsg.recentItems.lead());
		CmsUiUtils.style(lbl, SuiteStyle.recentItems);

		if (context == null)
			return null;
		SingleEntityViewer entityViewer = new SingleEntityViewer(parent, SWT.NONE, context.getSession());
		entityViewer.setLayoutData(CmsUiUtils.fillAll());
		entityViewer.createUi();
		return entityViewer;

	}

	public void init(Map<String, String> properties) {
		entityType = properties.get(NodeConstants.DATA_TYPE);
	}

	class SingleEntityViewer extends Composite {
		private static final long serialVersionUID = -4712523256962131370L;
		Text filterTxt;
		TableViewer entityViewer;
		Session session;

		public SingleEntityViewer(Composite parent, int style, Session session) {
			super(parent, style);
			this.session = session;
		}

		public void createUi() {
			// MainLayout
			setLayout(new GridLayout());
			addFilterPanel(this);
			entityViewer = createListPart(this, new SingleEntityLabelProvider());
			refreshFilteredList();

			try {
				String[] nodeTypes = entityType != null && entityType.contains(":") ? new String[] { entityType }
						: null;
				session.getWorkspace().getObservationManager().addEventListener(new EventListener() {

					@Override
					public void onEvent(EventIterator events) {
						getDisplay().asyncExec(() -> refreshFilteredList());
					}
				}, Event.PROPERTY_CHANGED | Event.NODE_ADDED | Event.NODE_REMOVED | Event.PROPERTY_ADDED, "/", true,
						null, nodeTypes, false);
			} catch (RepositoryException e) {
				throw new IllegalStateException("Cannot add JCR observer", e);
			}

		}

		private void addFilterPanel(Composite parent) {
			// Use a delayed text: the query won't be done until the user stop
			// typing for 800ms
			int style = SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL;
			DelayedText delayedText = new DelayedText(parent, style, ConnectUiConstants.SEARCH_TEXT_DELAY);
			filterTxt = delayedText.getText();
			filterTxt.setLayoutData(EclipseUiUtils.fillWidth());

			// final ServerPushSession pushSession = new ServerPushSession();
			delayedText.addDelayedModifyListener(null, new ModifyListener() {
				private static final long serialVersionUID = 5003010530960334977L;

				public void modifyText(ModifyEvent event) {
					delayedText.getText().getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							refreshFilteredList();
						}
					});
					// pushSession.stop();
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
//					Object first = entityViewer.getElementAt(0);
//					if (first != null) {
//						entityViewer.getTable().setFocus();
//						entityViewer.setSelection(new StructuredSelection(first), true);
//					}
						e.doit = false;
					}
				}
			});

			addDisposeListener((e) -> {
				delayedText.close();
			});
		}

		protected TableViewer createListPart(Composite parent, ILabelProvider labelProvider) {
			parent.setLayout(new GridLayout());

			Composite tableComposite = new Composite(parent, SWT.NONE);
			GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_VERTICAL
					| GridData.VERTICAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
			tableComposite.setLayoutData(gd);

			TableViewer viewer = new TableViewer(tableComposite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
			viewer.setLabelProvider(labelProvider);

			TableColumn singleColumn = new TableColumn(viewer.getTable(), SWT.V_SCROLL);
			TableColumnLayout tableColumnLayout = new TableColumnLayout();
			tableColumnLayout.setColumnData(singleColumn, new ColumnWeightData(85));
			tableComposite.setLayout(tableColumnLayout);

			// Corresponding table & style
			Table table = viewer.getTable();
//			Listener[] mouseDownListeners = table.getListeners(SWT.MouseDown);
//			for (Listener listener :  table.getListeners(SWT.MouseDown))
//				table.removeListener(SWT.MouseDown, listener);
//			for (Listener listener :  table.getListeners(SWT.MouseUp))
//				table.removeListener(SWT.MouseUp, listener);
//			for (Listener listener :  table.getListeners(SWT.MouseDoubleClick))
//				table.removeListener(SWT.MouseDoubleClick, listener);
//			
//			table.addMouseListener(new MouseListener() {
//
//				@Override
//				public void mouseUp(MouseEvent e) {
//					System.out.println("Mouse up: "+e);
//				}
//
//				@Override
//				public void mouseDown(MouseEvent e) {
//					System.out.println("Mouse down: "+e);
//				}
//
//				@Override
//				public void mouseDoubleClick(MouseEvent e) {
//					System.out.println("Mouse double: "+e);
//
//				}
//			});
			table.setLinesVisible(true);
			table.setHeaderVisible(false);
			CmsUiUtils.markup(table);
			CmsUiUtils.setItemHeight(table, 26);

			viewer.setContentProvider(new BasicNodeListContentProvider());
			viewer.addDoubleClickListener(new IDoubleClickListener() {

				@Override
				public void doubleClick(DoubleClickEvent event) {
					Node node = (Node) viewer.getStructuredSelection().getFirstElement();
					if (node != null)
						CmsView.getCmsView(parent).sendEvent(SuiteEvent.openNewPart.topic(), SuiteEvent.NODE_ID,
								Jcr.getIdentifier(node));

				}
			});
			// v.addDoubleClickListener(new
			// JcrViewerDClickListener(systemWorkbenchService));
			viewer.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
//					IStructuredSelection selection = (IStructuredSelection) event.getSelection();
					Node node = (Node) viewer.getStructuredSelection().getFirstElement();
					if (node != null)
						CmsView.getCmsView(parent).sendEvent(SuiteEvent.refreshPart.topic(), SuiteEvent.NODE_ID,
								Jcr.getIdentifier(node));
//				if (lst != null && !lst.isEmpty())
//					selectionService.setSelection(selection.toList());
//				else
//					selectionService.setSelection(null);
				}
			});
			return viewer;
		}

//	public void dispose() {
//		JcrUtils.logoutQuietly(session);
//	}

		public boolean setFocus() {
			refreshFilteredList();
			return super.setFocus();
//		filterTxt.setFocus();
		}

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
				String xpathQueryStr;
				if (entityType != null)
					xpathQueryStr = entityType.contains(":") ? "//element(*, " + entityType + ")"
							: "//element(*, " + EntityTypes.ENTITY_ENTITY + ")[@entity:type='" + entityType + "']";
				else
					xpathQueryStr = "//element(*, " + EntityTypes.ENTITY_ENTITY + ")";
//			String xpathQueryStr = "//element(*, " + ConnectTypes.CONNECT_ENTITY + ")";
				String xpathFilter = XPathUtils.getFreeTextConstraint(filter);
				if (notEmpty(xpathFilter))
					xpathQueryStr += "[" + xpathFilter + "]";

//				long begin = System.currentTimeMillis();
				// session.refresh(false);
				Query xpathQuery = XPathUtils.createQuery(session, xpathQueryStr);

				xpathQuery.setLimit(ConnectUiConstants.SEARCH_DEFAULT_LIMIT);
				QueryResult result = xpathQuery.execute();

				NodeIterator nit = result.getNodes();
				entityViewer.setInput(JcrUtils.nodeIteratorToList(nit));
//				if (log.isTraceEnabled()) {
//					long end = System.currentTimeMillis();
//					log.trace("Quick Search - Found: " + nit.getSize() + " in " + (end - begin)
//							+ " ms by executing XPath query (" + xpathQueryStr + ").");
//				}
			} catch (RepositoryException e) {
				throw new IllegalStateException("Unable to list entities", e);
			}
		}

		class SingleEntityLabelProvider extends ColumnLabelProvider {
			private static final long serialVersionUID = -2209337675781795677L;

			@Override
			public String getText(Object element) {
				return Jcr.getTitle((Node) element);
			}

		}
	}
}
