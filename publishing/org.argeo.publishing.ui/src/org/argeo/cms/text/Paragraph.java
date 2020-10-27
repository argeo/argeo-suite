package org.argeo.cms.text;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.cms.ui.viewers.Section;
import org.argeo.cms.ui.viewers.SectionPart;
import org.argeo.cms.ui.widgets.EditableText;
import org.argeo.cms.ui.widgets.TextStyles;

public class Paragraph extends EditableText implements SectionPart {
	private static final long serialVersionUID = 3746457776229542887L;

	private final TextSection section;

	public Paragraph(TextSection section, int style, Node node)
			throws RepositoryException {
		super(section, style, node);
		this.section = section;
		CmsUiUtils.style(this, TextStyles.TEXT_PARAGRAPH);
	}

	public Section getSection() {
		return section;
	}

	@Override
	public String getPartId() {
		return getNodeId();
	}

	@Override
	public Node getItem() throws RepositoryException {
		return getNode();
	}

	@Override
	public String toString() {
		return "Paragraph #" + getPartId();
	}
}
