package org.argeo.suite.workbench.fs;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.suite.SuiteException;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Provide a single column label provider for file and directory lists. Icon and
 * displayed text vary with the element node type
 */
public class FsSingleColumnLabelProvider extends LabelProvider implements PeopleNames {
	private static final long serialVersionUID = -8895136766988459632L;

	public FsSingleColumnLabelProvider() {
	}

	@Override
	public String getText(Object element) {
		try {
			Node entity = (Node) element;
			String result;
			if (entity.isNodeType(NodeType.NT_FILE))
				result = entity.getName();
			// result = ConnectJcrUtils.get(entity, Property.JCR_TITLE);
			else if (entity.isNodeType(NodeType.NT_FOLDER))
				result = entity.getName();
			// result = ConnectJcrUtils.get(entity, Property.JCR_TITLE);
			else
				result = "";
			return ConnectUiUtils.replaceAmpersand(result);
		} catch (RepositoryException re) {
			throw new SuiteException("Unable to get formatted value for node", re);
		}
	}

	/** Overwrite this method to provide project specific images */
	@Override
	public Image getImage(Object element) {
		try {
			Node entity = (Node) element;
			if (entity.isNodeType(NodeType.NT_FILE))
				return FsImages.ICON_FILE;
			else if (entity.isNodeType(NodeType.NT_FOLDER))
				return FsImages.ICON_FOLDER;
			return null;
		} catch (RepositoryException re) {
			throw new SuiteException("Cannot get icon for " + element, re);
		}
	}
}
