package org.argeo.cms.text;

import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.argeo.cms.ui.viewers.EditablePart;
import org.argeo.cms.ui.viewers.PropertyPart;
import org.argeo.cms.ui.widgets.EditableText;
import org.eclipse.swt.widgets.Composite;

/** The title of a section. */
@Deprecated
public class SectionTitle extends EditableText implements EditablePart,
		PropertyPart {
	private static final long serialVersionUID = -1787983154946583171L;

	private final TextSection section;

	public SectionTitle(Composite parent, int swtStyle, Property title)
			throws RepositoryException {
		super(parent, swtStyle, title);
		section = (TextSection) TextSection.findSection(this);
	}

	public TextSection getSection() {
		return section;
	}

	// @Override
	// public Property getProperty() throws RepositoryException {
	// return getSection().getNode().getProperty(Property.JCR_TITLE);
	// }

	@Override
	public Property getItem() throws RepositoryException {
		return getProperty();
	}

}
