package org.argeo.app.swt.space;

import java.net.URI;
import java.util.List;

import org.argeo.api.acr.Content;
import org.argeo.api.acr.NamespaceUtils;
import org.argeo.api.acr.spi.ProvidedContent;
import org.argeo.api.cms.ux.CmsView;
import org.argeo.app.api.EntityType;
import org.argeo.cms.swt.CmsSwtUtils;
import org.argeo.cms.swt.acr.SwtUiProvider;
import org.argeo.cms.swt.widgets.SwtTreeView;
import org.argeo.cms.ux.acr.ContentHierarchicalPart;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/** Entry area for managing the typologies. */
public class SpaceEntryArea implements SwtUiProvider {
	@Override
	public Control createUiPart(Composite parent, Content content) {
		CmsView cmsView = CmsSwtUtils.getCmsView(parent);

		parent.setLayout(new GridLayout());

		ContentHierarchicalPart contentPart = new ContentHierarchicalPart() {

			@Override
			public List<Content> getChildren(Content parent) {
				if (parent != null)
					return super.getChildren(parent);
				List<Content> res = ((ProvidedContent) content).getSession().search((bs) -> {
					bs.from(URI.create("/sys")).where((f) -> f.isContentClass(EntityType.space));
				}).filter((c) -> noSpaceParent((ProvidedContent) c)).toList();
				return res;
			}

		};
		contentPart.addColumn((c) -> NamespaceUtils.toPrefixedName(c.getName()));
//		contentPart.setInput(content);

		SwtTreeView<Content> view = new SwtTreeView<>(parent, 0, contentPart);
		view.setLayoutData(CmsSwtUtils.fillAll());

		contentPart.setInput(null);
		return view;

	}

	private static boolean noSpaceParent(ProvidedContent content) {
		if (content.isRoot() || !content.isParentAccessible())// end condition
			return true;
		ProvidedContent parent = (ProvidedContent) content.getParent();
		if (parent.hasContentClass(EntityType.space))
			return false;
		return noSpaceParent(parent);
	}
}
