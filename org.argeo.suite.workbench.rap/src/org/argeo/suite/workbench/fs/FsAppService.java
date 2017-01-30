package org.argeo.suite.workbench.fs;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.spi.FileSystemProvider;
import java.security.PrivilegedActionException;

import javax.jcr.Session;

import org.argeo.cms.auth.CurrentUser;
import org.argeo.node.NodeUtils;
import org.argeo.suite.SuiteException;

public class FsAppService {
	private final static String NODE_PREFIX = "node://";

	private String getCurrentHomePath(Session session) {
		try {
			// Make in a do as if not from the workbench
			// Repository repo = session.getRepository();
			// session = CurrentUser.tryAs(() -> repo.login());
			String homepath = NodeUtils.getUserHome(session).getPath();
			return homepath;
		} catch (Exception e) {
			throw new SuiteException("Cannot retrieve Current User Home Path", e);
			// } finally {
			// JcrUtils.logoutQuietly(session);
		}
	}

	public Path[] getMyFilesPath(FileSystemProvider nodeFileSystemProvider, Session session) {
		// return Paths.get(System.getProperty("user.dir"));
		String currHomeUriStr = NODE_PREFIX + getCurrentHomePath(session);
		try {
			URI uri = new URI(currHomeUriStr);
			FileSystem fileSystem = nodeFileSystemProvider.getFileSystem(uri);
			if (fileSystem == null) {
				fileSystem = CurrentUser.tryAs(() -> nodeFileSystemProvider.newFileSystem(uri, null));
				// PrivilegedExceptionAction<FileSystem> pea = new
				// PrivilegedExceptionAction<FileSystem>() {
				// @Override
				// public FileSystem run() throws Exception {
				// return nodeFileSystemProvider.newFileSystem(uri, null);
				// }
				//
				// };
				// fileSystem = CurrentUser.tryAs(pea);
			}
			Path[] paths = { fileSystem.getPath(getCurrentHomePath(session)), fileSystem.getPath("/") };
			return paths;
		} catch (URISyntaxException | PrivilegedActionException e) {
			throw new RuntimeException("unable to initialise home file system for " + currHomeUriStr, e);
		}
	}

	public Path[] getMyGroupsFilesPath(FileSystemProvider nodeFileSystemProvider, Session session) {
		// TODO
		Path[] paths = { Paths.get(System.getProperty("user.dir")), Paths.get("/tmp") };
		return paths;
	}

	public Path[] getMyBookmarks(FileSystemProvider nodeFileSystemProvider, Session session) {
		// TODO
		Path[] paths = { Paths.get(System.getProperty("user.dir")), Paths.get("/tmp"), Paths.get("/opt") };
		return paths;
	}
}
