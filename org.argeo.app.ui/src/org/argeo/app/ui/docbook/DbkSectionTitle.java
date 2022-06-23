package org.argeo.app.ui.docbook;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.swt.EditablePart;
import org.argeo.cms.ui.viewers.NodePart;
import org.argeo.cms.ui.widgets.EditableText;
import org.eclipse.swt.widgets.Composite;

/** The title of a section, based on an XML text node. */
public class DbkSectionTitle extends EditableText implements EditablePart, NodePart {
	private static final long serialVersionUID = -1787983154946583171L;

	private final TextSection section;

	public DbkSectionTitle(Composite parent, int swtStyle, Node titleNode) throws RepositoryException {
		super(parent, swtStyle, titleNode);
		section = (TextSection) TextSection.findSection(this);
	}

	public TextSection getSection() {
		return section;
	}

	@Override
	public Node getItem() throws RepositoryException {
		return getNode();
	}
}
