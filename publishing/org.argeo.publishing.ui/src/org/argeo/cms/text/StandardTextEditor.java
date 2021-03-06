package org.argeo.cms.text;

import static javax.jcr.Property.JCR_TITLE;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.argeo.cms.ui.CmsEditable;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.cms.ui.viewers.Section;
import org.eclipse.swt.widgets.Composite;

/** Text editor where sections and subsections can be managed by the user. */
@Deprecated
public class StandardTextEditor extends AbstractTextViewer {
	private static final long serialVersionUID = 6049661610883342325L;

	public StandardTextEditor(Composite parent, int style, Node textNode,
			CmsEditable cmsEditable) throws RepositoryException {
		super(new TextSection(parent, style, textNode), style, cmsEditable);
		refresh();
		getMainSection().setLayoutData(CmsUiUtils.fillWidth());
	}

	@Override
	protected void initModel(Node textNode) throws RepositoryException {
		if (isFlat())
			textNode.addNode(CMS_P).addMixin(CmsTypes.CMS_STYLED);
		else
			textNode.setProperty(JCR_TITLE, textNode.getName());
	}

	@Override
	protected Boolean isModelInitialized(Node textNode)
			throws RepositoryException {
		return textNode.hasProperty(Property.JCR_TITLE)
				|| textNode.hasNode(CMS_P)
				|| (!isFlat() && textNode.hasNode(CMS_H));
	}

	@Override
	public Section getMainSection() {
		// TODO Auto-generated method stub
		return super.getMainSection();
	}
}
