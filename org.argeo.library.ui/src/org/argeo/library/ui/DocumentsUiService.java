package org.argeo.library.ui;

import static org.argeo.cms.ui.dialogs.CmsMessageDialog.openConfirm;
import static org.argeo.cms.ui.dialogs.CmsMessageDialog.openError;
import static org.argeo.cms.ui.dialogs.SingleValueDialog.ask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.cms.ui.dialogs.CmsFeedback;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

public class DocumentsUiService {
	private final static Log log = LogFactory.getLog(DocumentsUiService.class);

	// Default known actions
	public final static String ACTION_ID_CREATE_FOLDER = "createFolder";
	public final static String ACTION_ID_BOOKMARK_FOLDER = "bookmarkFolder";
	public final static String ACTION_ID_SHARE_FOLDER = "shareFolder";
	public final static String ACTION_ID_DOWNLOAD_FOLDER = "downloadFolder";
	public final static String ACTION_ID_RENAME = "rename";
	public final static String ACTION_ID_DELETE = "delete";
	public final static String ACTION_ID_UPLOAD_FILE = "uploadFiles";
	// public final static String ACTION_ID_OPEN = "open";
	public final static String ACTION_ID_DELETE_BOOKMARK = "deleteBookmark";
	public final static String ACTION_ID_RENAME_BOOKMARK = "renameBookmark";

	public String getLabel(String actionId) {
		switch (actionId) {
		case ACTION_ID_CREATE_FOLDER:
			return "Create Folder";
		case ACTION_ID_BOOKMARK_FOLDER:
			return "Bookmark Folder";
		case ACTION_ID_SHARE_FOLDER:
			return "Share Folder";
		case ACTION_ID_DOWNLOAD_FOLDER:
			return "Download as zip archive";
		case ACTION_ID_RENAME:
			return "Rename";
		case ACTION_ID_DELETE:
			return "Delete";
		case ACTION_ID_UPLOAD_FILE:
			return "Upload Files";
//		case ACTION_ID_OPEN:
//			return "Open";
		case ACTION_ID_DELETE_BOOKMARK:
			return "Delete bookmark";
		case ACTION_ID_RENAME_BOOKMARK:
			return "Rename bookmark";
		default:
			throw new IllegalArgumentException("Unknown action ID " + actionId);
		}
	}

	public void openFile(Path toOpenPath) {
		try {
			String name = toOpenPath.getFileName().toString();
			File tmpFile = File.createTempFile("tmp", name);
			tmpFile.deleteOnExit();
			try (OutputStream os = new FileOutputStream(tmpFile)) {
				Files.copy(toOpenPath, os);
			} catch (IOException e) {
				throw new IllegalStateException("Cannot open copy " + name + " to tmpFile.", e);
			}
			String uri = Paths.get(tmpFile.getAbsolutePath()).toUri().toString();
			Map<String, String> params = new HashMap<String, String>();
//			params.put(OpenFile.PARAM_FILE_NAME, name);
//			params.put(OpenFile.PARAM_FILE_URI, uri);
			// FIXME open file without a command
			// CommandUtils.callCommand(OpenFile.ID, params);
		} catch (IOException e1) {
			throw new IllegalStateException("Cannot create tmp copy of " + toOpenPath, e1);
		}
	}

	public boolean deleteItems(Shell shell, IStructuredSelection selection) {
		if (selection.isEmpty())
			return false;

		StringBuilder builder = new StringBuilder();
		@SuppressWarnings("unchecked")
		Iterator<Object> iterator = selection.iterator();
		List<Path> paths = new ArrayList<>();

		while (iterator.hasNext()) {
			Path path = (Path) iterator.next();
			builder.append(path.getFileName() + ", ");
			paths.add(path);
		}
		String msg = "You are about to delete following elements: " + builder.substring(0, builder.length() - 2)
				+ ". Are you sure?";
		if (openConfirm(msg)) {
			for (Path path : paths) {
				try {
					// recursively delete directory and its content
					Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
						@Override
						public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
							Files.delete(file);
							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
							Files.delete(dir);
							return FileVisitResult.CONTINUE;
						}
					});
				} catch (DirectoryNotEmptyException e) {
					String errMsg = path.getFileName() + " cannot be deleted: directory is not empty.";
					openError( errMsg);
					throw new IllegalArgumentException("Cannot delete path " + path, e);
				} catch (IOException e) {
					String errMsg = e.toString();
					openError(errMsg);
					throw new IllegalArgumentException("Cannot delete path " + path, e);
				}
			}
			return true;
		}
		return false;
	}

	public boolean renameItem(Shell shell, Path parentFolderPath, Path toRenamePath) {
		String msg = "Enter a new name:";
		String name = ask( msg, toRenamePath.getFileName().toString());
		// TODO enhance check of name validity
		if (EclipseUiUtils.notEmpty(name)) {
			try {
				Path child = parentFolderPath.resolve(name);
				if (Files.exists(child)) {
					String errMsg = "An object named " + name + " already exists at " + parentFolderPath.toString()
							+ ", please provide another name";
					openError( errMsg);
					throw new IllegalArgumentException(errMsg);
				} else {
					Files.move(toRenamePath, child);
					return true;
				}
			} catch (IOException e) {
				throw new IllegalStateException("Cannot rename " + name + " at " + parentFolderPath.toString(), e);
			}
		}
		return false;
	}

	public boolean createFolder(Shell shell, Path currFolderPath) {
		String msg = "Enter a name:";
		String name = ask( msg);
		// TODO enhance check of name validity
		if (EclipseUiUtils.notEmpty(name)) {
			name = name.trim();
			try {
				Path child = currFolderPath.resolve(name);
				if (Files.exists(child)) {
					String errMsg = "A folder named " + name + " already exists at " + currFolderPath.toString()
							+ ", cannot create";
					openError(errMsg);
					throw new IllegalArgumentException(errMsg);
				} else {
					Files.createDirectories(child);
					return true;
				}
			} catch (IOException e) {
				throw new IllegalStateException("Cannot create folder " + name + " at " + currFolderPath.toString(), e);
			}
		}
		return false;
	}

//	public void bookmarkFolder(Path toBookmarkPath, Repository repository, DocumentsService documentsService) {
//		String msg = "Provide a name:";
//		String name = SingleQuestion.ask("Create bookmark", msg, toBookmarkPath.getFileName().toString());
//		if (EclipseUiUtils.notEmpty(name))
//			documentsService.createFolderBookmark(toBookmarkPath, name, repository);
//	}

	public boolean uploadFiles(Shell shell, Path currFolderPath) {
//		shell = Display.getCurrent().getActiveShell();// ignore argument
		try {
			FileDialog dialog = new FileDialog(shell, SWT.MULTI);
			dialog.setText("Choose one or more files to upload");

			if (EclipseUiUtils.notEmpty(dialog.open())) {
				String[] names = dialog.getFileNames();
				// Workaround small differences between RAP and RCP
				// 1. returned names are absolute path on RAP and
				// relative in RCP
				// 2. in RCP we must use getFilterPath that does not
				// exists on RAP
				Method filterMethod = null;
				Path parPath = null;
				try {
					filterMethod = dialog.getClass().getDeclaredMethod("getFilterPath");
					String filterPath = (String) filterMethod.invoke(dialog);
					parPath = Paths.get(filterPath);
				} catch (NoSuchMethodException nsme) { // RAP
				}
				if (names.length == 0)
					return false;
				else {
					loop: for (String name : names) {
						Path tmpPath = Paths.get(name);
						if (parPath != null)
							tmpPath = parPath.resolve(tmpPath);
						if (Files.exists(tmpPath)) {
							URI uri = tmpPath.toUri();
							String uriStr = uri.toString();

							if (Files.isDirectory(tmpPath)) {
								openError(
										"Upload of directories in the system is not yet implemented");
								continue loop;
							}
							Path targetPath = currFolderPath.resolve(tmpPath.getFileName().toString());
							try (InputStream in = new FileInputStream(tmpPath.toFile())) {
								Files.copy(in, targetPath);
								Files.delete(tmpPath);
							}
							if (log.isDebugEnabled())
								log.debug("copied uploaded file " + uriStr + " to " + targetPath.toString());
						} else {
							String msg = "Cannot copy tmp file from " + tmpPath.toString();
							if (parPath != null)
								msg += "\nPlease remember that file upload fails when choosing files from the \"Recently Used\" bookmarks on some OS";
							openError( msg);
							continue loop;
						}
					}
					return true;
				}
			}
		} catch (Exception e) {
			CmsFeedback.show("Cannot import files to " + currFolderPath,e);
		}
		return false;
	}

//	public boolean deleteBookmark(Shell shell, IStructuredSelection selection, Node bookmarkParent) {
//		if (selection.isEmpty())
//			return false;
//
//		StringBuilder builder = new StringBuilder();
//		@SuppressWarnings("unchecked")
//		Iterator<Object> iterator = selection.iterator();
//		List<Node> nodes = new ArrayList<>();
//
//		while (iterator.hasNext()) {
//			Node node = (Node) iterator.next();
//			builder.append(Jcr.get(node, Property.JCR_TITLE) + ", ");
//			nodes.add(node);
//		}
//		String msg = "You are about to delete following bookmark: " + builder.substring(0, builder.length() - 2)
//				+ ". Are you sure?";
//		if (MessageDialog.openConfirm(shell, "Confirm deletion", msg)) {
//			Session session = Jcr.session(bookmarkParent);
//			try {
//				if (session.hasPendingChanges())
//					throw new DocumentsException("Cannot remove bookmarks, session is not clean");
//				for (Node path : nodes)
//					path.remove();
//				bookmarkParent.getSession().save();
//				return true;
//			} catch (RepositoryException e) {
//				JcrUtils.discardQuietly(session);
//				throw new DocumentsException("Cannot delete bookmarks " + builder.toString(), e);
//			}
//		}
//		return false;
//	}

//	public boolean renameBookmark(IStructuredSelection selection) {
//		if (selection.isEmpty() || selection.size() > 1)
//			return false;
//		Node toRename = (Node) selection.getFirstElement();
//		String msg = "Please provide a new name.";
//		String name = SingleQuestion.ask("Rename bookmark", msg, ConnectJcrUtils.get(toRename, Property.JCR_TITLE));
//		if (EclipseUiUtils.notEmpty(name)
//				&& ConnectJcrUtils.setJcrProperty(toRename, Property.JCR_TITLE, PropertyType.STRING, name)) {
//			ConnectJcrUtils.saveIfNecessary(toRename);
//			return true;
//		}
//		return false;
//	}
}
