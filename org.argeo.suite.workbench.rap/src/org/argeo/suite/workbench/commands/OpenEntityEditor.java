package org.argeo.suite.workbench.commands;


public class OpenEntityEditor{}

//import javax.jcr.Node;
//import javax.jcr.Repository;
//import javax.jcr.RepositoryException;
//import javax.jcr.Session;
//
//import org.argeo.connect.ui.workbench.AppWorkbenchService;
//import org.argeo.connect.ui.workbench.NodeEditorInput;
//import org.argeo.jcr.JcrUtils;
//import org.argeo.suite.SuiteException;
//import org.argeo.suite.workbench.AsUiPlugin;
//import org.eclipse.core.commands.AbstractHandler;
//import org.eclipse.core.commands.ExecutionEvent;
//import org.eclipse.core.commands.ExecutionException;
//import org.eclipse.ui.IWorkbenchPage;
//import org.eclipse.ui.IWorkbenchWindow;
//import org.eclipse.ui.PartInitException;
//import org.eclipse.ui.handlers.HandlerUtil;
//
///** Workaround to enable opening of a default editor */
//public class OpenEntityEditor extends AbstractHandler {
//	public final static String ID = AsUiPlugin.PLUGIN_ID + ".openEntityEditor";
//
//	public final static String PARAM_JCR_ID = "param.jcrId";
//	// public final static String PARAM_OPEN_FOR_EDIT = "param.openForEdit";
//	// public final static String PARAM_CTAB_ID = "param.cTabId";
//
//	private Repository repository;
//	private AppWorkbenchService appWorkbenchService;
//
//	public Object execute(ExecutionEvent event) throws ExecutionException {
//		NodeEditorInput eei = null;
//		Node entity = null;
//		Session session = null;
//		String jcrId = event.getParameter(PARAM_JCR_ID);
//		try {
//			session = repository.login();
//			if (jcrId != null) {
//				entity = session.getNodeByIdentifier(jcrId);
//				eei = new NodeEditorInput(jcrId);
//			} else
//				return null;
//
//			String editorId = appWorkbenchService.getEntityEditorId(entity);
//			if (editorId != null) {
//				IWorkbenchWindow iww = HandlerUtil.getActiveWorkbenchWindow(event);
//				IWorkbenchPage iwp = iww.getActivePage();
//				iwp.openEditor(eei, editorId);
//			}
//		} catch (PartInitException pie) {
//			throw new SuiteException("Unexpected PartInitException while opening entity editor", pie);
//		} catch (RepositoryException e) {
//			throw new SuiteException("unexpected JCR error while opening editor", e);
//		} finally {
//			JcrUtils.logoutQuietly(session);
//		}
//		return null;
//	}
//
//	public void setRepository(Repository repository) {
//		this.repository = repository;
//	}
//
//	public void setAppWorkbenchService(AppWorkbenchService appWorkbenchService) {
//		this.appWorkbenchService = appWorkbenchService;
//	}
//}
