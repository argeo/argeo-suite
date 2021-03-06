package org.argeo.publishing.ui;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.ui.CmsEditable;
import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.cms.ui.viewers.JcrVersionCmsEditable;
import org.argeo.cms.ui.widgets.ScrolledPage;
import org.argeo.docbook.DbkType;
import org.argeo.docbook.ui.AbstractDbkViewer;
import org.argeo.docbook.ui.DocumentTextEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class DocumentUiProvider implements CmsUiProvider {

	@Override
	public Control createUi(Composite parent, Node context) throws RepositoryException {
		CmsEditable cmsEditable = new JcrVersionCmsEditable(context);
		if (context.hasNode(DbkType.article.get())) {
			Node textNode = context.getNode(DbkType.article.get());
			// Title
			parent.setLayout(CmsUiUtils.noSpaceGridLayout());

			ScrolledPage page = new ScrolledPage(parent, SWT.NONE);
			page.setLayoutData(CmsUiUtils.fillAll());
			page.setLayout(CmsUiUtils.noSpaceGridLayout());

			AbstractDbkViewer dbkEditor = new DocumentTextEditor(page, SWT.NONE, textNode, cmsEditable);

		}
		return null;
	}

}
