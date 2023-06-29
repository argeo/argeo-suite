package org.argeo.app.swt.ux;

import org.argeo.api.acr.Content;
import org.argeo.cms.Localized;
import org.argeo.cms.swt.acr.SwtUiProvider;
import org.eclipse.swt.widgets.Composite;

/** An UI layer for the main work area. */
public interface SwtAppLayer extends SwtUiProvider {
	static enum Property {
		title, icon, weights, startMaximized, singleTab, singleTabTitle, fixedEntryArea;
	}

	String getId();

	void view(SwtUiProvider uiProvider, Composite workArea, Content context);

	Content getCurrentContext(Composite workArea);

	default void open(SwtUiProvider uiProvider, Composite workArea, Content context) {
		view(uiProvider, workArea, context);
	}

	default Localized getTitle() {
		return null;
	}
}
