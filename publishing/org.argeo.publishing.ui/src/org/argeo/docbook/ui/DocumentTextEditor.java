package org.argeo.docbook.ui;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.argeo.cms.text.TextSection;
import org.argeo.cms.ui.CmsEditable;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.jcr.Jcr;
import org.argeo.jcr.JcrxType;
import org.eclipse.swt.widgets.Composite;

/** Text editor where sections and subsections can be managed by the user. */
public class DocumentTextEditor extends AbstractDbkViewer {
	private static final long serialVersionUID = 6049661610883342325L;

	public DocumentTextEditor(Composite parent, int style, Node textNode, CmsEditable cmsEditable) {
		super(new TextSection(parent, style, textNode), style, cmsEditable);
		refresh();
		getMainSection().setLayoutData(CmsUiUtils.fillWidth());
	}

	@Override
	protected void initModel(Node textNode) throws RepositoryException {
		if (isFlat()) {
			textNode.addNode(DocBookNames.DBK_PARA, DocBookTypes.PARA).addNode(Jcr.JCR_XMLTEXT, JcrxType.JCRX_XMLTEXT)
					.setProperty(Jcr.JCR_XMLCHARACTERS, "");
		}
//		else
//			textNode.setProperty(DocBookNames.DBK_TITLE, textNode.getName());
	}

	@Override
	protected Boolean isModelInitialized(Node textNode) throws RepositoryException {
		return textNode.hasProperty(Property.JCR_TITLE) || textNode.hasNode(DocBookNames.DBK_PARA)
				|| (!isFlat() && textNode.hasNode(DocBookNames.DBK_SECTION));
	}

}
