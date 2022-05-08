package org.argeo.app.ui.publish;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.argeo.api.cms.CmsEditable;
import org.argeo.api.cms.CmsView;
import org.argeo.app.docbook.DbkType;
import org.argeo.app.ui.docbook.AbstractDbkViewer;
import org.argeo.app.ui.docbook.DocumentTextEditor;
import org.argeo.cms.swt.CmsSwtUtils;
import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.cms.ui.util.CmsLink;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.cms.ui.viewers.JcrVersionCmsEditable;
import org.argeo.cms.ui.widgets.ScrolledPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class DocumentUiProvider implements CmsUiProvider {

	@Override
	public Control createUi(Composite parent, Node context) throws RepositoryException {
		CmsView cmsView = CmsSwtUtils.getCmsView(parent);
		CmsEditable cmsEditable = new JcrVersionCmsEditable(context);
		if (context.hasNode(DbkType.article.get())) {
			Node textNode = context.getNode(DbkType.article.get());
			// Title
			parent.setLayout(CmsSwtUtils.noSpaceGridLayout());

			Composite links = new Composite(parent, SWT.NONE);
			FillLayout linksLayout = new FillLayout();
			linksLayout.spacing = 2;
			links.setLayout(linksLayout);
			CmsLink toHtml = new CmsLink("to HTML", "/html/dbk" + context.getPath() + "/index.html");
			toHtml.createUiPart(links, context);
			CmsLink toPdf = new CmsLink("to PDF", "/html/dbk" + context.getPath() + "/index.pdf");
			toPdf.createUiPart(links, context);

			ScrolledPage page = new ScrolledPage(parent, SWT.NONE);
			page.setLayoutData(CmsSwtUtils.fillAll());
			page.setLayout(CmsSwtUtils.noSpaceGridLayout());

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
				browser.setLayoutData(CmsSwtUtils.fillAll());
				return browser;
			}
		}
		return null;
	}

}
