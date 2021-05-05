package org.argeo.library.ui;

import static org.argeo.library.ui.DocumentsUiService.ACTION_ID_BOOKMARK_FOLDER;
import static org.argeo.library.ui.DocumentsUiService.ACTION_ID_CREATE_FOLDER;
import static org.argeo.library.ui.DocumentsUiService.ACTION_ID_DELETE;
import static org.argeo.library.ui.DocumentsUiService.ACTION_ID_DOWNLOAD_FOLDER;
import static org.argeo.library.ui.DocumentsUiService.ACTION_ID_RENAME;
import static org.argeo.library.ui.DocumentsUiService.ACTION_ID_SHARE_FOLDER;
import static org.argeo.library.ui.DocumentsUiService.ACTION_ID_UPLOAD_FILE;

import java.nio.file.Files;
import java.nio.file.Path;

import org.argeo.suite.ui.widgets.AbstractConnectContextMenu;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;

/** Generic popup context menu to manage NIO Path in a Viewer. */
public class DocumentsContextMenu extends AbstractConnectContextMenu {
	// Local context
	private final DocumentsFolderComposite browser;
	private final DocumentsUiService uiService;
//	private final Repository repository;

	private final static String[] DEFAULT_ACTIONS = { ACTION_ID_CREATE_FOLDER, ACTION_ID_BOOKMARK_FOLDER,
			ACTION_ID_SHARE_FOLDER, ACTION_ID_DOWNLOAD_FOLDER, ACTION_ID_UPLOAD_FILE, ACTION_ID_RENAME,
			ACTION_ID_DELETE };

	private Path currFolderPath;

	public DocumentsContextMenu(DocumentsFolderComposite browser,
			DocumentsUiService documentsUiService) {
		super(browser.getDisplay(), DEFAULT_ACTIONS);
		this.browser = browser;
		this.uiService = documentsUiService;
//		this.repository = repository;

		createControl();
	}

	public void setCurrFolderPath(Path currFolderPath) {
		this.currFolderPath = currFolderPath;
	}

	protected boolean aboutToShow(Control source, Point location, IStructuredSelection selection) {
		boolean emptySel = true;
		boolean multiSel = false;
		boolean isFolder = true;
		if (selection != null && !selection.isEmpty()) {
			emptySel = false;
			multiSel = selection.size() > 1;
			if (!multiSel && selection.getFirstElement() instanceof Path) {
				isFolder = Files.isDirectory((Path) selection.getFirstElement());
			}
		}
		if (emptySel) {
			setVisible(true, ACTION_ID_CREATE_FOLDER, ACTION_ID_UPLOAD_FILE, ACTION_ID_BOOKMARK_FOLDER);
			setVisible(false, ACTION_ID_SHARE_FOLDER, ACTION_ID_DOWNLOAD_FOLDER, ACTION_ID_RENAME, ACTION_ID_DELETE
				);
		} else if (multiSel) {
			setVisible(true, ACTION_ID_CREATE_FOLDER, ACTION_ID_UPLOAD_FILE, ACTION_ID_DELETE,
					ACTION_ID_BOOKMARK_FOLDER);
			setVisible(false, ACTION_ID_SHARE_FOLDER, ACTION_ID_DOWNLOAD_FOLDER, ACTION_ID_RENAME);
		} else if (isFolder) {
			setVisible(true, ACTION_ID_CREATE_FOLDER, ACTION_ID_UPLOAD_FILE, ACTION_ID_RENAME, ACTION_ID_DELETE,
					ACTION_ID_BOOKMARK_FOLDER);
			setVisible(false, 
					// to be implemented
					ACTION_ID_SHARE_FOLDER, ACTION_ID_DOWNLOAD_FOLDER);
		} else {
			setVisible(true, ACTION_ID_CREATE_FOLDER, ACTION_ID_UPLOAD_FILE, ACTION_ID_RENAME,
					ACTION_ID_DELETE);
			setVisible(false, ACTION_ID_SHARE_FOLDER, ACTION_ID_DOWNLOAD_FOLDER, ACTION_ID_BOOKMARK_FOLDER);
		}
		return true;
	}

	public void show(Control source, Point location, IStructuredSelection selection, Path currFolderPath) {
		// TODO find a better way to retrieve the parent path (cannot be deduced
		// from table content because it will fail on an empty folder)
		this.currFolderPath = currFolderPath;
		super.show(source, location, selection);

	}

	@Override
	protected boolean performAction(String actionId) {
		switch (actionId) {
		case ACTION_ID_CREATE_FOLDER:
			createFolder();
			break;
		case ACTION_ID_BOOKMARK_FOLDER:
			bookmarkFolder();
			break;
		case ACTION_ID_RENAME:
			renameItem();
			break;
		case ACTION_ID_DELETE:
			deleteItems();
			break;
//		case ACTION_ID_OPEN:
//			openFile();
//			break;
		case ACTION_ID_UPLOAD_FILE:
			uploadFiles();
			break;
		default:
			throw new IllegalArgumentException("Unimplemented action " + actionId);
			// case ACTION_ID_SHARE_FOLDER:
			// return "Share Folder";
			// case ACTION_ID_DOWNLOAD_FOLDER:
			// return "Download as zip archive";
		}
		browser.setFocus();
		return false;
	}

	@Override
	protected String getLabel(String actionId) {
		return uiService.getLabel(actionId);
	}

	private void openFile() {
		IStructuredSelection selection = ((IStructuredSelection) browser.getViewer().getSelection());
		if (selection.isEmpty() || selection.size() > 1)
			// Should never happen
			return;
		Path toOpenPath = ((Path) selection.getFirstElement());
		uiService.openFile(toOpenPath);
	}

	private void deleteItems() {
		IStructuredSelection selection = ((IStructuredSelection) browser.getViewer().getSelection());
		if (selection.isEmpty())
			return;
		else if (uiService.deleteItems(getParentShell(), selection))
			browser.refresh();
	}

	private void renameItem() {
		IStructuredSelection selection = ((IStructuredSelection) browser.getViewer().getSelection());
		if (selection.isEmpty() || selection.size() > 1)
			// Should never happen
			return;
		Path toRenamePath = ((Path) selection.getFirstElement());
		if (uiService.renameItem(getParentShell(), currFolderPath, toRenamePath))
			browser.refresh();
	}

	private void createFolder() {
		if (uiService.createFolder(getParentShell(), currFolderPath))
			browser.refresh();
	}

	private void bookmarkFolder() {
		Path toBookmarkPath = null;
		IStructuredSelection selection = ((IStructuredSelection) browser.getViewer().getSelection());
		if (selection.isEmpty())
			toBookmarkPath = currFolderPath;
		else if (selection.size() > 1)
			toBookmarkPath = currFolderPath;
		else if (selection.size() == 1) {
			Path currSelected = ((Path) selection.getFirstElement());
			if (Files.isDirectory(currSelected))
				toBookmarkPath = currSelected;
			else
				return;
		}
		//uiService.bookmarkFolder(toBookmarkPath, repository, null);
	}

	private void uploadFiles() {
		if (uiService.uploadFiles(getParentShell(), currFolderPath))
			browser.refresh();
	}
}
