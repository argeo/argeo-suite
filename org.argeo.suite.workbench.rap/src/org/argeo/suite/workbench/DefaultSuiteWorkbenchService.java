package org.argeo.suite.workbench;

import java.util.List;

import javax.jcr.Node;

import org.argeo.connect.workbench.AppWorkbenchService;
import org.argeo.connect.workbench.SystemWorkbenchService;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.suite.workbench.parts.DefaultDashboardEditor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Image;

/** Centralise workbench services from the various base apps */
public class DefaultSuiteWorkbenchService implements SystemWorkbenchService {

	// Injected known AppWorkbenchServices: order is important, first found
	// result will be returned by the various methods.
	private List<AppWorkbenchService> knownAppWbServices;
	private String defaultEditorId = DefaultDashboardEditor.ID;

	@Override
	public String getDefaultEditorId() {
		return defaultEditorId;
	}

	@Override
	public String getEntityEditorId(Node entity) {
		String result = null;
		for (AppWorkbenchService appWbService : knownAppWbServices) {
			result = appWbService.getEntityEditorId(entity);
			if (EclipseUiUtils.notEmpty(result))
				return result;
		}
		return null;
	}

	@Override
	public String getSearchEntityEditorId(String nodeType) {
		String result = null;
		for (AppWorkbenchService appWbService : knownAppWbServices) {
			result = appWbService.getSearchEntityEditorId(nodeType);
			if (EclipseUiUtils.notEmpty(result))
				return result;
		}
		return null;
	}

	@Override
	public Image getIconForType(Node entity) {
		Image result = null;
		for (AppWorkbenchService appWbService : knownAppWbServices) {
			result = appWbService.getIconForType(entity);
			if (result != null)
				return result;
		}
		return null;
	}

	@Override
	public Wizard getCreationWizard(Node node) {
		Wizard result = null;
		for (AppWorkbenchService appWbService : knownAppWbServices) {
			result = appWbService.getCreationWizard(node);
			if (result != null)
				return result;
		}
		return null;
	}

	/* DEPENDENCY INJECTION */
	public void setKnownAppWbServices(List<AppWorkbenchService> knownAppWbServices) {
		this.knownAppWbServices = knownAppWbServices;
	}

	public void setDefaultEditorId(String defaultEditorId) {
		this.defaultEditorId = defaultEditorId;
	}
}