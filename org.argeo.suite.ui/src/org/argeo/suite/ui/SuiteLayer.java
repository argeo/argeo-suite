package org.argeo.suite.ui;

import javax.jcr.Node;

import org.argeo.cms.ui.CmsUiProvider;
import org.eclipse.swt.widgets.Composite;

/** An UI layer for the main work area. */
public interface SuiteLayer extends CmsUiProvider {
	void view(Composite workArea, Node context);

	default void open(Composite workArea, Node context) {
		view(workArea, context);
	}
}
