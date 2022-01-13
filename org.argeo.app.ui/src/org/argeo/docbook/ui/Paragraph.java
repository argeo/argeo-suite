package org.argeo.docbook.ui;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.swt.CmsSwtUtils;
import org.argeo.cms.ui.viewers.SectionPart;
import org.argeo.cms.ui.widgets.EditableText;
import org.argeo.cms.ui.widgets.TextStyles;
import org.argeo.docbook.DbkType;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/** An editable paragraph. */
public class Paragraph extends EditableText implements SectionPart {
	private static final long serialVersionUID = 3746457776229542887L;

	private final TextSection section;

	public Paragraph(TextSection section, int style, Node node) throws RepositoryException {
		super(section, style, node);
		this.section = section;
		CmsSwtUtils.style(this, DbkType.para.name());
	}

	public TextSection getSection() {
		return section;
	}

	@Override
	protected Label createLabel(Composite box, String style) {
		Label lbl = super.createLabel(box, style);
		CmsSwtUtils.disableMarkupValidation(lbl);
		return lbl;
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
