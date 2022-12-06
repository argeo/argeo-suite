package org.argeo.app.swt.docbook;

import org.argeo.api.acr.Content;
import org.argeo.api.acr.spi.ProvidedContent;
import org.argeo.app.docbook.DbkType;
import org.argeo.cms.swt.CmsSwtUtils;
import org.argeo.cms.swt.acr.SwtSectionPart;
import org.argeo.cms.swt.widgets.EditableText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/** An editable paragraph. */
public class Paragraph extends EditableText implements SwtSectionPart {
	private static final long serialVersionUID = 3746457776229542887L;

	private final TextSection section;

	public Paragraph(TextSection section, int style, Content node) {
		super(section, style);
		this.section = section;
		setData(node);
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
		return ((ProvidedContent) getContent()).getSessionLocalId();
	}

	@Override
	public Content getContent() {
		return (Content) getData();
	}

	@Override
	public String toString() {
		return "Paragraph #" + getPartId();
	}
}
