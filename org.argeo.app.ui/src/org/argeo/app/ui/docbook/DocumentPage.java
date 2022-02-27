package org.argeo.app.ui.docbook;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.argeo.api.cms.CmsEditable;
import org.argeo.app.docbook.DbkType;
import org.argeo.cms.swt.CmsSwtUtils;
import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.cms.ui.util.CmsLink;
import org.argeo.cms.ui.viewers.JcrVersionCmsEditable;
import org.argeo.cms.ui.widgets.ScrolledPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Display the text of the context, and provide an editor if the user can edit.
 */
public class DocumentPage implements CmsUiProvider {
	public final static String WWW = "www";

	@Override
	public Control createUi(Composite parent, Node context) throws RepositoryException {

		ScrolledPage page = new ScrolledPage(parent, SWT.NONE);
		page.setLayout(CmsSwtUtils.noSpaceGridLayout());
		GridData textGd = CmsSwtUtils.fillAll();
		page.setLayoutData(textGd);

		if (context.isNodeType(DbkType.article.get())) {
			CmsEditable cmsEditable = new JcrVersionCmsEditable(context);
			if (cmsEditable.canEdit())
				new TextEditorHeader(cmsEditable, parent, SWT.NONE).setLayoutData(CmsSwtUtils.fillWidth());
			if (!cmsEditable.isEditing())
				cmsEditable.startEditing();
			new DocumentTextEditor(page, SWT.FLAT, context, cmsEditable);
		} else {
			parent.setBackgroundMode(SWT.INHERIT_NONE);
			if (context.getSession().hasPermission(context.getPath(), Session.ACTION_ADD_NODE)) {
//				new DocumentTextEditor(page, SWT.FLAT, indexNode, cmsEditable);
//				textGd.heightHint = 400;

				for (NodeIterator ni = context.getNodes(); ni.hasNext();) {
					Node textNode = ni.nextNode();
					if (textNode.isNodeType(NodeType.NT_FOLDER))
						new CmsLink(textNode.getName() + "/", textNode.getPath()).createUi(parent, textNode);
				}
				for (NodeIterator ni = context.getNodes(); ni.hasNext();) {
					Node textNode = ni.nextNode();
					if (textNode.isNodeType(DbkType.article.get()) && !textNode.getName().equals(WWW))
						new CmsLink(textNode.getName(), textNode.getPath()).createUi(parent, textNode);
				}
			}
		}
		return page;
	}
}
