package org.argeo.app.ui.library;

import org.argeo.api.acr.Content;
import org.argeo.api.acr.NamespaceUtils;
import org.argeo.api.acr.spi.ProvidedContent;
import org.argeo.api.cms.CmsLog;
import org.argeo.api.cms.ux.CmsView;
import org.argeo.app.api.EntityType;
import org.argeo.app.ui.SuiteUxEvent;
import org.argeo.cms.swt.CmsSwtUtils;
import org.argeo.cms.swt.acr.SwtUiProvider;
import org.argeo.cms.swt.widgets.SwtTreeView;
import org.argeo.cms.ux.acr.ContentHierarchicalPart;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class ContentEntryArea implements SwtUiProvider {
	private final static CmsLog log = CmsLog.getLog(ContentEntryArea.class);

	@Override
	public Control createUiPart(Composite parent, Content context) {
		CmsView cmsView = CmsSwtUtils.getCmsView(parent);

		parent.setLayout(new GridLayout());

		new Label(parent, 0).setText(context.toString());

		Content rootContent = ((ProvidedContent) context).getSession().getRepository().get().get("/sys");

		ContentHierarchicalPart contentPart = new ContentHierarchicalPart() {

			@Override
			protected boolean isLeaf(Content content) {
				if (content.hasContentClass(EntityType.document.qName()))
					return true;
				return super.isLeaf(content);
			}

		};
		contentPart.addColumn((c) -> NamespaceUtils.toPrefixedName(c.getName()));
		contentPart.setInput(rootContent);

		SwtTreeView<Content> view = new SwtTreeView<>(parent, 0, contentPart);
		view.setLayoutData(CmsSwtUtils.fillAll());

		contentPart.setInput(rootContent);
		contentPart.onSelected((o) -> {
			Content c = (Content) o;
			log.debug(c.getPath());
			cmsView.sendEvent(SuiteUxEvent.refreshPart.topic(), SuiteUxEvent.eventProperties(c));
		});
		return view;
	}

}
