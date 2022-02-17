package org.argeo.app.ui;

import javax.jcr.Node;

import org.argeo.cms.Localized;
import org.argeo.cms.ui.CmsUiProvider;
import org.eclipse.swt.widgets.Composite;

/** An UI layer for the main work area. */
public interface SuiteLayer extends CmsUiProvider {
	static enum Property {
		title, icon, weights, startMaximized, singleTab, fixedEntryArea;
	}

	void view(CmsUiProvider uiProvider, Composite workArea, Node context);
	
	Node getCurrentContext(Composite workArea);

	default void open(CmsUiProvider uiProvider, Composite workArea, Node context) {
		view(uiProvider, workArea, context);
	}

	default Localized getTitle() {
		return null;
	}
}