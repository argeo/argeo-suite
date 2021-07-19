package org.argeo.publishing.ui;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.argeo.cms.ui.CmsEditable;
import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.cms.ui.CmsView;
import org.argeo.cms.ui.util.CmsLink;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.cms.ui.viewers.JcrVersionCmsEditable;
import org.argeo.cms.ui.widgets.ScrolledPage;
import org.argeo.docbook.DbkType;
import org.argeo.docbook.ui.AbstractDbkViewer;
import org.argeo.docbook.ui.DocumentTextEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class DocumentUiProvider implements CmsUiProvider {

	@Override
	public Control createUi(Composite parent, Node context) throws RepositoryException {
		CmsView cmsView = CmsView.getCmsView(parent);
		CmsEditable cmsEditable = new JcrVersionCmsEditable(context);
		if (context.hasNode(DbkType.article.get())) {
			Node textNode = context.getNode(DbkType.article.get());
			// Title
			parent.setLayout(CmsUiUtils.noSpaceGridLayout());

			CmsLink toHtml = new CmsLink("To HTML", "/html/dbk" + context.getPath());
			toHtml.createUiPart(parent, context);

			ScrolledPage page = new ScrolledPage(parent, SWT.NONE);
			page.setLayoutData(CmsUiUtils.fillAll());
			page.setLayout(CmsUiUtils.noSpaceGridLayout());

			cmsView.runAs(() -> {
				AbstractDbkViewer dbkEditor = new DocumentTextEditor(page, SWT.NONE, textNode, cmsEditable);
				dbkEditor.refresh();
			});
			return page;

		} else if (context.isNodeType(NodeType.NT_FILE)) {
			String fileName = context.getName();
			if (fileName.endsWith(".pdf")) {
				Browser browser = new Browser(parent, SWT.NONE);
				String dataPath = CmsUiUtils.getDataPath(context);
				browser.setUrl(dataPath);
				browser.setLayoutData(CmsUiUtils.fillAll());
				return browser;
			}
		}
		return null;
	}

}
