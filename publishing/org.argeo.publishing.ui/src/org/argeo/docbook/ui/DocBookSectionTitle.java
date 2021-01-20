package org.argeo.docbook.ui;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.text.TextSection;
import org.argeo.cms.ui.viewers.EditablePart;
import org.argeo.cms.ui.viewers.NodePart;
import org.argeo.cms.ui.widgets.EditableText;
import org.eclipse.swt.widgets.Composite;

/** The title of a section, based on an XML text node. */
public class DocBookSectionTitle extends EditableText implements EditablePart, NodePart {
	private static final long serialVersionUID = -1787983154946583171L;

	private final TextSection section;

	public DocBookSectionTitle(Composite parent, int swtStyle, Node titleNode) throws RepositoryException {
		super(parent, swtStyle, titleNode);
		section = (TextSection) TextSection.findSection(this);
	}

//	@Override
	public TextSection getSection() {
		return section;
	}

	@Override
	public Node getItem() throws RepositoryException {
		return getNode();
	}

//	@Override
//	public String getPartId() {
//		return getNodeId();
//	}

//	@Override
//	protected void setControlLayoutData(Control control) {
//		super.setControlLayoutData(control);
//		control.setLayoutData(new GridData(SWT.LEAD, SWT.CENTER, true, false));
//	}
//
//	@Override
//	protected void setContainerLayoutData(Composite composite) {
//		super.setContainerLayoutData(composite);
//		composite.setLayoutData(new GridData(SWT.LEAD, SWT.BOTTOM, true, false));
//	}

}
