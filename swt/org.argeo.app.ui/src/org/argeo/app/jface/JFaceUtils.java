package org.argeo.app.jface;

import org.eclipse.jface.viewers.AbstractTableViewer;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.Viewer;

/** Utilities around JFace. */
public class JFaceUtils {
	/**
	 * TootlTip support is supported only for {@link AbstractTableViewer} in RAP
	 */
	public static void enableToolTipSupport(Viewer viewer) {
		if (viewer instanceof ColumnViewer)
			ColumnViewerToolTipSupport.enableFor((ColumnViewer) viewer);
	}

	/** singleton */
	private JFaceUtils() {

	}
}
