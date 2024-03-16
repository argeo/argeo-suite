package org.argeo.app.swt.terms;

import org.argeo.api.acr.Content;
import org.argeo.api.acr.NamespaceUtils;
import org.argeo.api.acr.spi.ProvidedContent;
import org.argeo.api.app.EntityType;
import org.argeo.api.app.TermsManager;
import org.argeo.api.cms.ux.CmsView;
import org.argeo.app.ux.SuiteUxEvent;
import org.argeo.cms.swt.CmsSwtUtils;
import org.argeo.cms.swt.acr.SwtUiProvider;
import org.argeo.cms.swt.widgets.SwtTreeView;
import org.argeo.cms.ux.acr.ContentHierarchicalPart;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/** Entry area for managing the typologies. */
public class TermsEntryArea implements SwtUiProvider {
	private TermsManager termsManager;

	@Override
	public Control createUiPart(Composite parent, Content content) {
//		parent.setLayout(new GridLayout());
//		Label lbl = new Label(parent, SWT.NONE);
//		lbl.setText("Typologies");
//
//		Set<Typology> typologies = termsManager.getTypologies();
//		for (Typology typology : typologies) {
//			new Label(parent, 0).setText(typology.getId());
//		}
//		
//		
//		return lbl;
		CmsView cmsView = CmsSwtUtils.getCmsView(parent);

		parent.setLayout(new GridLayout());
		Content rootContent = ((ProvidedContent) content).getSession().getRepository().get().get("/terms");

		ContentHierarchicalPart contentPart = new ContentHierarchicalPart() {

			@Override
			protected boolean isLeaf(Content content) {
				if (content.hasContentClass(EntityType.document.qName()))
					return true;
				return super.isLeaf(content);
			}

		};
		contentPart.addColumn((c) -> NamespaceUtils.toPrefixedName(c.getName()));
//		contentPart.setInput(rootContent);

		SwtTreeView<Content> view = new SwtTreeView<>(parent, 0, contentPart);
		view.setLayoutData(CmsSwtUtils.fillAll());

		contentPart.setInput(rootContent);
//		contentPart.onSelected((o) -> {
//			Content c = (Content) o;
////			log.debug(c.getPath());
//			cmsView.sendEvent(SuiteUxEvent.refreshPart.topic(), SuiteUxEvent.eventProperties(c));
//		});
		return view;

	}

	public void setTermsManager(TermsManager termsManager) {
		this.termsManager = termsManager;
	}

}
