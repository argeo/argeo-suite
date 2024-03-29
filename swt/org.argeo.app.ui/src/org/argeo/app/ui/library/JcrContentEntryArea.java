package org.argeo.app.ui.library;

import java.util.SortedMap;
import java.util.TreeMap;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;

import org.argeo.api.acr.Content;
import org.argeo.api.app.EntityType;
import org.argeo.api.cms.CmsConstants;
import org.argeo.app.ui.SuiteUiUtils;
import org.argeo.app.ui.widgets.TreeOrSearchArea;
import org.argeo.app.ux.SuiteIcon;
import org.argeo.app.ux.SuiteUxEvent;
import org.argeo.cms.jcr.acr.JcrContentProvider;
import org.argeo.cms.swt.CmsSwtTheme;
import org.argeo.cms.swt.CmsSwtUtils;
import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.jcr.Jcr;
import org.argeo.jcr.JcrException;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class JcrContentEntryArea implements CmsUiProvider {
	private JcrContentProvider jcrContentProvider;

	@Override
	public Control createUiPart(Composite parent, Content context) {
		CmsSwtTheme theme = CmsSwtUtils.getCmsTheme(parent);

		parent.setLayout(new GridLayout());
		Ui ui = new Ui(parent, SWT.NONE);
		ui.setLayoutData(CmsSwtUtils.fillAll());

		TreeViewerColumn nameCol = new TreeViewerColumn(ui.getTreeViewer(), SWT.NONE);
		nameCol.getColumn().setWidth(400);
		nameCol.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				Node node = (Node) element;
				return Jcr.getTitle(node);
			}

			@Override
			public Image getImage(Object element) {
				Node node = (Node) element;
				Image icon;
				if (Jcr.isNodeType(node, NodeType.NT_FOLDER)) {
					icon = theme.getSmallIcon(SuiteIcon.folder);
				} else if (Jcr.isNodeType(node, NodeType.NT_FILE)) {
					// TODO check recognized document types
					icon = theme.getSmallIcon(SuiteIcon.document);
				} else if (Jcr.isNodeType(node, EntityType.document.get())) {
					icon = theme.getSmallIcon(SuiteIcon.document);
				} else {
					if (!isLeaf(node))
						icon = theme.getSmallIcon(SuiteIcon.folder);
					else
						icon = null;
				}
				return icon;
			}

		});

		ui.getTreeViewer().addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				Node user = (Node) ui.getTreeViewer().getStructuredSelection().getFirstElement();
				if (user != null) {
					CmsSwtUtils.getCmsView(parent).sendEvent(SuiteUxEvent.openNewPart.topic(),
							SuiteUiUtils.eventProperties(user));
				}

			}
		});
		ui.getTreeViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				Node user = (Node) ui.getTreeViewer().getStructuredSelection().getFirstElement();
				if (user != null) {
					CmsSwtUtils.getCmsView(parent).sendEvent(SuiteUxEvent.refreshPart.topic(),
							SuiteUiUtils.eventProperties(user));
				}
			}
		});

		ui.getTreeViewer().setContentProvider(new SpacesContentProvider());
		Session session = jcrContentProvider.getJcrSession(context, CmsConstants.SYS_WORKSPACE);
		ui.getTreeViewer().setInput(session);
		return ui;
	}

	protected boolean isLeaf(Node node) {
		return Jcr.isNodeType(node, EntityType.entity.get()) || Jcr.isNodeType(node, EntityType.document.get())
				|| Jcr.isNodeType(node, NodeType.NT_FILE);
	}

	public void setJcrContentProvider(JcrContentProvider jcrContentProvider) {
		this.jcrContentProvider = jcrContentProvider;
	}

	class Ui extends TreeOrSearchArea {

		public Ui(Composite parent, int style) {
			super(parent, style);
		}

	}

	class SpacesContentProvider implements ITreeContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			Session session = (Session) inputElement;
			try {
				Query query = session.getWorkspace().getQueryManager()
						.createQuery("SELECT * FROM [" + EntityType.space.get() + "]", Query.JCR_SQL2);
				NodeIterator spacesIt = query.execute().getNodes();
				SortedMap<String, Node> map = new TreeMap<>();
				while (spacesIt.hasNext()) {
					Node space = spacesIt.nextNode();
					String path = space.getPath();
					map.put(path, space);
				}
				return map.values().toArray();
			} catch (RepositoryException e) {
				throw new JcrException(e);
			}
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			Node parent = (Node) parentElement;
			if (isLeaf(parent))
				return null;
			return Jcr.getNodes(parent).toArray();
		}

		@Override
		public Object getParent(Object element) {
			Node node = (Node) element;
			return Jcr.getParent(node);
		}

		@Override
		public boolean hasChildren(Object element) {
			Node node = (Node) element;
			return !isLeaf(node);
		}

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

	}

}
