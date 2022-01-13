package org.argeo.app.ui.docbook;

import javax.jcr.Node;

import org.argeo.api.cms.CmsEditable;
import org.argeo.cms.ui.viewers.Section;
import org.eclipse.swt.widgets.Composite;

/**
 * Manages hardcoded sections as an arbitrary hierarchy under the main section,
 * which contains no text and no title.
 */
public class CustomDbkEditor extends AbstractDbkViewer {
	private static final long serialVersionUID = 656302500183820802L;

	public CustomDbkEditor(Composite parent, int style, Node textNode, CmsEditable cmsEditable) {
		this(new Section(parent, style, textNode), style, cmsEditable);
	}

	public CustomDbkEditor(Section parent, int style, CmsEditable cmsEditable) {
		super(parent, style, cmsEditable);
	}
}
