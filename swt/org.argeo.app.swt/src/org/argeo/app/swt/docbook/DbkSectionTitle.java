package org.argeo.app.swt.docbook;

import org.argeo.api.acr.Content;
import org.argeo.cms.swt.SwtEditablePart;
import org.argeo.cms.swt.widgets.EditableText;
import org.argeo.cms.ux.acr.ContentPart;
import org.eclipse.swt.widgets.Composite;

/** The title of a section, based on an XML text node. */
public class DbkSectionTitle extends EditableText implements SwtEditablePart, ContentPart {
	private static final long serialVersionUID = -1787983154946583171L;

	private final TextSection section;

	public DbkSectionTitle(Composite parent, int swtStyle, Content titleNode) {
		super(parent, swtStyle);
		section = (TextSection) TextSection.findSection(this);
		setData(titleNode);
	}

	public TextSection getSection() {
		return section;
	}

	@Override
	public Content getContent() {
		return (Content) getData();
	}

}
