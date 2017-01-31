/*
 * Copyright (C) 2007-2012 Argeo GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.argeo.suite.workbench.parts;

import static org.argeo.eclipse.ui.EclipseUiUtils.notEmpty;

import java.nio.file.spi.FileSystemProvider;

import javax.jcr.NodeIterator;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.ConnectConstants;
import org.argeo.connect.documents.DocumentsService;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.workbench.rap.providers.BasicNodeListContentProvider;
import org.argeo.connect.ui.ConnectUiConstants;
import org.argeo.connect.ui.widgets.DelayedText;
import org.argeo.connect.ui.workbench.AppWorkbenchService;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.util.XPathUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.eclipse.ui.fs.FsTableViewer;
import org.argeo.jcr.JcrUtils;
import org.argeo.suite.workbench.AsUiPlugin;
import org.argeo.suite.workbench.fs.FsSingleColumnLabelProvider;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;

/** Browse the node file system. */
public class MyFilesView extends ViewPart implements IDoubleClickListener {
	public final static String ID = AsUiPlugin.PLUGIN_ID + ".myFilesView";

	private Repository repository;
	private Session session;
	private AppWorkbenchService appWorkbenchService;
	private FileSystemProvider nodeFileSystemProvider;
	private DocumentsService documentsService;

	private DelayedText filterTxt;
	private TableViewer searchResultsViewer;
	private Composite searchCmp;

	@Override
	public void createPartControl(Composite parent) {
		session = ConnectJcrUtils.login(repository);
		// MainLayout
		parent.setLayout(new GridLayout());
		addFilterPanel(parent);
		searchCmp = new Composite(parent, SWT.NO_FOCUS);
		searchCmp.setLayout(EclipseUiUtils.noSpaceGridLayout());
		searchResultsViewer = createListPart(searchCmp, new FsSingleColumnLabelProvider());
		GridData gd = EclipseUiUtils.fillWidth();
		gd.heightHint = 0;
		searchCmp.setLayoutData(gd);

		Composite bookmarkCmp = new Composite(parent, SWT.NO_FOCUS);
		bookmarkCmp.setLayoutData(EclipseUiUtils.fillAll());
		populateBookmarks(bookmarkCmp);
	}

	public void addFilterPanel(Composite parent) {
		// Use a delayed text: the query won't be done until the user stop
		// typing for 800ms
		int style = SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL;
		filterTxt = new DelayedText(parent, style, ConnectUiConstants.SEARCH_TEXT_DELAY);
		filterTxt.setLayoutData(EclipseUiUtils.fillWidth());

		final ServerPushSession pushSession = new ServerPushSession();
		filterTxt.addDelayedModifyListener(pushSession, new ModifyListener() {
			private static final long serialVersionUID = 5003010530960334977L;

			public void modifyText(ModifyEvent event) {
				filterTxt.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						int resultNb = refreshFilteredList();
						if (resultNb > 0)
							((GridData) searchCmp.getLayoutData()).heightHint = 120;
						else
							((GridData) searchCmp.getLayoutData()).heightHint = 0;
						parent.layout(true, true);
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
					Object first = searchResultsViewer.getElementAt(0);
					if (first != null) {
						searchResultsViewer.getTable().setFocus();
						searchResultsViewer.setSelection(new StructuredSelection(first), true);
					}
					e.doit = false;
				}
			}
		});
	}

	protected TableViewer createListPart(Composite parent, ILabelProvider labelProvider) {
		parent.setLayout(new GridLayout());

		Composite tableComposite = new Composite(parent, SWT.NONE);
		tableComposite.setLayoutData(EclipseUiUtils.fillAll());

		TableViewer v = new TableViewer(tableComposite);
		v.setLabelProvider(labelProvider);

		TableColumn singleColumn = new TableColumn(v.getTable(), SWT.V_SCROLL);
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tableColumnLayout.setColumnData(singleColumn, new ColumnWeightData(100));
		tableComposite.setLayout(tableColumnLayout);

		// Corresponding table & style
		Table table = v.getTable();
		table.setLinesVisible(false);
		table.setHeaderVisible(false);
		CmsUtils.markup(table);
		CmsUtils.setItemHeight(table, 26);

		v.setContentProvider(new BasicNodeListContentProvider());
		v.addDoubleClickListener(this);
		return v;
	}

	@Override
	public void dispose() {
		JcrUtils.logoutQuietly(session);
		super.dispose();
	}

	protected int refreshFilteredList() {
		try {
			String filter = filterTxt.getText();
			if (EclipseUiUtils.isEmpty(filter)) {
				searchResultsViewer.setInput(null);
				return 0;
			}

			// XPATH Query
			// TODO manage cleanly jcr: prefix
			// String xpathQueryStr = "//element(*, " + NodeType.NT_FILE + ")";
			String xpathQueryStr = "//element(*, nt:file)";
			String xpathFilter = XPathUtils.getFreeTextConstraint(filter);
			if (notEmpty(xpathFilter))
				xpathQueryStr += "[" + xpathFilter + "]";

			QueryManager queryManager = session.getWorkspace().getQueryManager();
			Query xpathQuery = queryManager.createQuery(xpathQueryStr, ConnectConstants.QUERY_XPATH);
			// xpathQuery.setLimit(TrackerUiConstants.SEARCH_DEFAULT_LIMIT);
			QueryResult result = xpathQuery.execute();
			NodeIterator nit = result.getNodes();
			searchResultsViewer.setInput(JcrUtils.nodeIteratorToList(nit));

			return (int) nit.getSize();
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to list files", e);
		}
	}

	private void populateBookmarks(Composite parent) {
		CmsUtils.clear(parent);
		parent.setLayout(new GridLayout());
		int bookmarkColWith = 200;

		FsTableViewer homeViewer = new FsTableViewer(parent, SWT.SINGLE | SWT.NO_SCROLL);
		Table table = homeViewer.configureDefaultSingleColumnTable(bookmarkColWith);
		GridData gd = EclipseUiUtils.fillWidth();
		gd.horizontalIndent = 10;
		table.setLayoutData(gd);
		homeViewer.addDoubleClickListener(this);
		homeViewer.setPathsInput(documentsService.getMyDocumentsPath(nodeFileSystemProvider, session));

		appendTitle(parent, "Shared files");
		FsTableViewer groupsViewer = new FsTableViewer(parent, SWT.SINGLE | SWT.NO_SCROLL);
		table = groupsViewer.configureDefaultSingleColumnTable(bookmarkColWith);
		gd = EclipseUiUtils.fillWidth();
		gd.horizontalIndent = 10;
		table.setLayoutData(gd);
		groupsViewer.addDoubleClickListener(this);
		groupsViewer.setPathsInput(documentsService.getMyGroupsFilesPath(nodeFileSystemProvider, session));

		appendTitle(parent, "My bookmarks");
		FsTableViewer bookmarksViewer = new FsTableViewer(parent, SWT.SINGLE | SWT.NO_SCROLL);
		table = bookmarksViewer.configureDefaultSingleColumnTable(bookmarkColWith);
		gd = EclipseUiUtils.fillWidth();
		gd.horizontalIndent = 10;
		table.setLayoutData(gd);
		bookmarksViewer.addDoubleClickListener(this);
		bookmarksViewer.setPathsInput(documentsService.getMyBookmarks(nodeFileSystemProvider, session));
	}

	private Label appendTitle(Composite parent, String value) {
		Label titleLbl = new Label(parent, SWT.NONE);
		titleLbl.setText(value);
		titleLbl.setFont(EclipseUiUtils.getBoldFont(parent));
		GridData gd = EclipseUiUtils.fillWidth();
		gd.horizontalIndent = 5;
		gd.verticalIndent = 5;
		titleLbl.setLayoutData(gd);
		return titleLbl;
	}

	@Override
	public void doubleClick(DoubleClickEvent event) {
		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		if (selection.isEmpty())
			return;
		else {

			// currNode = ConnectJcrUtils.getNodeFromElement(obj, selectorName);
			// if (currNode != null)
			// CommandUtils
			// .callCommand(peopleWorkbenchService
			// .getOpenEntityEditorCmdId(),
			// OpenEntityEditor.PARAM_JCR_ID, currNode
			// .getIdentifier());
			System.out.println("Double clicked");
			// TODO open corresponding editor
			// Path newSelected = (Path) selection.getFirstElement();
			// if (newSelected.equals(currDisplayedFolder) &&
			// newSelected.equals(initialPath))
			// return;
			// initialPath = newSelected;
			// setInput(newSelected);
		}
	}

	@Override
	public void setFocus() {
	}

	/* DEPENDENCY INJECTION */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setAppWorkbenchService(AppWorkbenchService appWorkbenchService) {
		this.appWorkbenchService = appWorkbenchService;
	}

	public void setNodeFileSystemProvider(FileSystemProvider nodeFileSystemProvider) {
		this.nodeFileSystemProvider = nodeFileSystemProvider;
	}

	public void setDocumentsService(DocumentsService documentsService) {
		this.documentsService = documentsService;
	}
}
