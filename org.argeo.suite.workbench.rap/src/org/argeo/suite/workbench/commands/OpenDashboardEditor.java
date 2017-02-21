package org.argeo.suite.workbench.commands;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.connect.ui.workbench.util.EntityEditorInput;
import org.argeo.jcr.JcrUtils;
import org.argeo.node.NodeUtils;
import org.argeo.suite.SuiteException;
import org.argeo.suite.workbench.AsUiPlugin;
import org.argeo.suite.workbench.parts.DefaultDashboardEditor;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

/** Workaround to enable opening of a default editor */
public class OpenDashboardEditor extends AbstractHandler {
	public final static String ID = AsUiPlugin.PLUGIN_ID + ".openDashboardEditor";

	private Repository repository;

	public Object execute(ExecutionEvent event) throws ExecutionException {
		Session session = null;
		try {
			// TODO check roles
			session = repository.login();
			IWorkbenchPage iwPage = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
			Node homeNode = NodeUtils.getUserHome(session);
			EntityEditorInput eei = new EntityEditorInput(homeNode.getIdentifier());
			IEditorPart iep = iwPage.findEditor(eei);
			if (iep == null) {
				iwPage.openEditor(eei, DefaultDashboardEditor.ID);
			} else
				iwPage.activate(iep);
		} catch (RepositoryException | PartInitException re) {
			throw new SuiteException("Unable to open dashboard", re);
		} finally {
			JcrUtils.logoutQuietly(session);
		}
		return null;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}
}
