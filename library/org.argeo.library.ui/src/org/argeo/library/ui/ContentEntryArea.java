package org.argeo.library.ui;

import java.util.SortedMap;
import java.util.TreeMap;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;

import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.entity.EntityType;
import org.argeo.jcr.Jcr;
import org.argeo.jcr.JcrException;
import org.argeo.suite.ui.widgets.TreeOrSearchArea;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class ContentEntryArea implements CmsUiProvider {

	@Override
	public Control createUi(Composite parent, Node context) throws RepositoryException {
		parent.setLayout(new GridLayout());
		Ui ui = new Ui(parent, SWT.NONE);
		ui.setLayoutData(CmsUiUtils.fillAll());

		ui.getTreeViewer().setContentProvider(new SpacesContentProvider());
		ui.getTreeViewer().setInput(context.getSession());
		return ui;
	}

	protected boolean isLeaf(Node node) {
		return Jcr.isNodeType(node, EntityType.entity.get()) || Jcr.isNodeType(node, NodeType.NT_FILE);
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
