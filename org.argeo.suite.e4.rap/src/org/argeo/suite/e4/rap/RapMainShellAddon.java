package org.argeo.suite.e4.rap;

import javax.annotation.PostConstruct;

import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.SWT;

public class RapMainShellAddon {
	private final static String STYLE_OVERRIDE = "styleOverride";
	private final static String SHELL_MAXIMIZED = "shellMaximized";

	@PostConstruct
	void init(EModelService modelService, MApplication application) {
		MWindow window = (MWindow) modelService.find("org.argeo.suite.e4.trimmedwindow.main", application);
		String currentStyle = window.getPersistedState().get(STYLE_OVERRIDE);
		int style = 8;
		if (currentStyle != null) {
			style = Integer.parseInt(currentStyle);
		}
		style = style | SWT.NO_TRIM;
		window.getPersistedState().put(STYLE_OVERRIDE, Integer.toString(style));
		window.getTags().add(SHELL_MAXIMIZED);
	}
}
