package org.argeo.app.ui;

import static org.argeo.eclipse.ui.EclipseUiUtils.notEmpty;

import java.util.List;
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

import org.argeo.api.cms.CmsTheme;
import org.argeo.app.api.EntityType;
import org.argeo.app.core.XPathUtils;
import org.argeo.app.ui.widgets.DelayedText;
import org.argeo.cms.swt.CmsSwtUtils;
import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.Jcr;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/** List recent items. */
public class RecentItems implements CmsUiProvider {
	private final static int SEARCH_TEXT_DELAY = 800;
	private final static int SEARCH_DEFAULT_LIMIT = 100;

	private CmsTheme theme;

	private String entityType;

	static enum Property {
		entityTypes;
	}

	@Override
	public Control createUi(Composite parent, Node context) throws RepositoryException {
		theme = CmsSwtUtils.getCmsTheme(parent);
		parent.setLayout(new GridLayout());
//		parent.setLayout(CmsUiUtils.noSpaceGridLayout());
		parent.setLayout(new GridLayout());

//		Composite top = new Composite(parent, SWT.BORDER);
//		CmsUiUtils.style(top, SuiteStyle.recentItems);
//		top.setLayoutData(CmsUiUtils.fillWidth());
//		top.setLayout(CmsUiUtils.noSpaceGridLayout(2));
//		Label lbl = new Label(top, SWT.FLAT);
//		lbl.setLayoutData(CmsUiUtils.fillWidth());
//		lbl.setText(SuiteMsg.recentItems.lead());
//		CmsUiUtils.style(lbl, SuiteStyle.recentItems);
//
//		ToolBar topToolBar = new ToolBar(top, SWT.NONE);
//		ToolItem addItem = new ToolItem(topToolBar, SWT.FLAT);
////		CmsUiUtils.style(addItem, SuiteStyle.recentItems);
//		addItem.setImage(SuiteIcon.add.getSmallIcon(theme));

		if (context == null)
			return null;
		SingleEntityViewer entityViewer = new SingleEntityViewer(parent, SWT.NONE, context.getSession());
		entityViewer.createUi();
		entityViewer.getViewer().getTable().setLayoutData(CmsSwtUtils.fillAll());

		Composite bottom = new Composite(parent, SWT.NONE);
		bottom.setLayoutData(CmsSwtUtils.fillWidth());
		bottom.setLayout(CmsSwtUtils.noSpaceGridLayout());
		ToolBar bottomToolBar = new ToolBar(bottom, SWT.NONE);
		bottomToolBar.setLayoutData(new GridData(SWT.END, SWT.FILL, true, false));
		ToolItem deleteItem = new ToolItem(bottomToolBar, SWT.FLAT);
		deleteItem.setEnabled(false);
//		CmsUiUtils.style(deleteItem, SuiteStyle.recentItems);
		deleteItem.setImage(SuiteIcon.delete.getSmallIcon(theme));
		ToolItem addItem = new ToolItem(bottomToolBar, SWT.FLAT);
		addItem.setImage(SuiteIcon.add.getSmallIcon(theme));
		entityViewer.getViewer().addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				Node node = (Node) entityViewer.getViewer().getStructuredSelection().getFirstElement();
				if (node != null)
					CmsSwtUtils.getCmsView(parent).sendEvent(SuiteEvent.openNewPart.topic(),
							SuiteEvent.eventProperties(node));

			}
		});
		entityViewer.getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				Node node = (Node) entityViewer.getViewer().getStructuredSelection().getFirstElement();
				if (node != null) {
					CmsSwtUtils.getCmsView(parent).sendEvent(SuiteEvent.refreshPart.topic(),
							SuiteEvent.eventProperties(node));
					deleteItem.setEnabled(true);
				} else {
					deleteItem.setEnabled(false);
				}
			}
		});

		return entityViewer.filterTxt;

	}

	public void init(Map<String, String> properties) {
		// TODO manage multiple entities
		entityType = properties.get(Property.entityTypes.name());
	}

	class SingleEntityViewer {
		Composite parent;
		Text filterTxt;
		TableViewer viewer;
		Session session;

		public SingleEntityViewer(Composite parent, int style, Session session) {
			this.parent = parent;
			this.session = session;
		}

		public void createUi() {
			// MainLayout
			addFilterPanel(parent);
			viewer = createListPart(parent, new SingleEntityLabelProvider());
			refreshFilteredList();

			try {
				String[] nodeTypes = entityType != null && entityType.contains(":") ? new String[] { entityType }
						: null;
				session.getWorkspace().getObservationManager().addEventListener(new EventListener() {

					@Override
					public void onEvent(EventIterator events) {
						parent.getDisplay().asyncExec(() -> refreshFilteredList());
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
			DelayedText delayedText = new DelayedText(parent, style, SEARCH_TEXT_DELAY);
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

			parent.addDisposeListener((e) -> {
				delayedText.close();
			});
		}

		protected TableViewer createListPart(Composite parent, ILabelProvider labelProvider) {
//			parent.setLayout(new GridLayout());
//			parent.setLayout(CmsUiUtils.noSpaceGridLayout());

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
			// CmsUiUtils.markup(table);
			// CmsUiUtils.setItemHeight(table, 26);

			viewer.setContentProvider(new BasicNodeListContentProvider());
			return viewer;
		}

//		public boolean setFocus() {
//			refreshFilteredList();
//			return parent.setFocus();
//		}

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
				if (entityType != null) {
					int indexColumn = entityType.indexOf(':');
					if (indexColumn > 0) {// JCR node type
						xpathQueryStr = "//element(*, " + entityType + ") order by @jcr:created descending";
					} else {
						xpathQueryStr = entityType.contains(":") ? "//element(*, " + entityType + ")"
								: "//element(*, " + EntityType.entity.get() + ")[@entity:type='" + entityType + "']";
					}
				} else {
					xpathQueryStr = "//element(*, " + EntityType.entity.get() + ")";
				}
//			String xpathQueryStr = "//element(*, " + ConnectTypes.CONNECT_ENTITY + ")";
				String xpathFilter = XPathUtils.getFreeTextConstraint(filter);
				if (notEmpty(xpathFilter))
					xpathQueryStr += "[" + xpathFilter + "]";

//				long begin = System.currentTimeMillis();
				// session.refresh(false);
				Query xpathQuery = XPathUtils.createQuery(session, xpathQueryStr);

				xpathQuery.setLimit(SEARCH_DEFAULT_LIMIT);
				QueryResult result = xpathQuery.execute();

				NodeIterator nit = result.getNodes();
				viewer.setInput(JcrUtils.nodeIteratorToList(nit));
//				if (log.isTraceEnabled()) {
//					long end = System.currentTimeMillis();
//					log.trace("Quick Search - Found: " + nit.getSize() + " in " + (end - begin)
//							+ " ms by executing XPath query (" + xpathQueryStr + ").");
//				}
			} catch (RepositoryException e) {
				throw new IllegalStateException("Unable to list entities", e);
			}
		}

		public TableViewer getViewer() {
			return viewer;
		}

		class SingleEntityLabelProvider extends ColumnLabelProvider {
			private static final long serialVersionUID = -2209337675781795677L;

			@Override
			public String getText(Object element) {
				return Jcr.getTitle((Node) element);
			}

		}

		class BasicNodeListContentProvider implements IStructuredContentProvider {
			private static final long serialVersionUID = 1L;
			// keep a cache of the Nodes in the content provider to be able to
			// manage long request
			private List<Node> nodes;

			public void dispose() {
			}

			/** Expects a list of nodes as a new input */
			@SuppressWarnings("unchecked")
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				nodes = (List<Node>) newInput;
			}

			public Object[] getElements(Object arg0) {
				return nodes.toArray();
			}
		}
	}
}
