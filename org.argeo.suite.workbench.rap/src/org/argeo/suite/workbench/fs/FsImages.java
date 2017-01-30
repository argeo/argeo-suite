package org.argeo.suite.workbench.fs;

import org.argeo.suite.workbench.AsUiPlugin;
import org.eclipse.swt.graphics.Image;

/** Shared icons for the file system RAP workbench */
public class FsImages {
	final private static String BASE_PATH = "/theme/argeo-classic/icons/fs/";
	// Various types
	public final static Image ICON_FOLDER = AsUiPlugin.getImageDescriptor(BASE_PATH + "folder.gif").createImage();
	public final static Image ICON_FILE = AsUiPlugin.getImageDescriptor(BASE_PATH + "file.gif").createImage();
}
