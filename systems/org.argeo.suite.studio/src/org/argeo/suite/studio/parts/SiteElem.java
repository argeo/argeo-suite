package org.argeo.suite.studio.parts;

import org.argeo.eclipse.ui.TreeParent;

/** Base class for site map elements. */
abstract class SiteElem extends TreeParent {

	private final String path;

	public SiteElem(String path) {
		super(extractLastSegment(path));
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	static String extractLastSegment(String path) {
		if (path == null || path.equals(""))
			throw new IllegalArgumentException("Path should not be null or empty.");
		if (path.equals("/"))
			return path;
		String[] segments = path.split("/");
		for (int i = segments.length - 1; i >= 0; i--) {
			if (!segments[i].equals("") && !segments[i].equals("*"))
				return segments[i];
		}
		return "/";
	}

	@Override
	public String toString() {
		return path;
	}

}
