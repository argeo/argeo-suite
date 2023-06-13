package org.argeo.app.ui.docbook;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.api.cms.ux.CmsEditable;
import org.argeo.app.docbook.DbkType;
import org.argeo.app.jcr.docbook.DbkJcrUtils;
import org.argeo.cms.swt.CmsSwtUtils;
import org.eclipse.swt.widgets.Composite;

/** Text editor where sections and subsections can be managed by the user. */
public class DocumentTextEditor extends AbstractDbkViewer {
	private static final long serialVersionUID = 6049661610883342325L;

	public DocumentTextEditor(Composite parent, int style, Node textNode, CmsEditable cmsEditable) {
		super(new TextSection(parent, style, textNode), style, cmsEditable);
//		refresh();
		getMainSection().setLayoutData(CmsSwtUtils.fillWidth());
	}

	@Override
	protected void initModel(Node textNode) throws RepositoryException {
		if (isFlat()) {
			DbkJcrUtils.addParagraph(textNode, "");
		}
//		else
//			textNode.setProperty(DocBookNames.DBK_TITLE, textNode.getName());
	}

	@Override
	protected Boolean isModelInitialized(Node textNode) throws RepositoryException {
		return textNode.hasNode(DbkType.title.get()) || textNode.hasNode(DbkType.para.get())
				|| (!isFlat() && textNode.hasNode(DbkType.section.get()));
	}

}
