package org.argeo.app.ol;

public class VectorSource extends Source {

	public VectorSource(Object... args) {
		super(args);
	}

	public VectorSource(String url, FeatureFormat format) {
		setUrl(url);
		setFormat(format);
	}

	public void setUrl(String url) {
		doSetValue(getMethodName(), "url", url);
	}

	public void setFormat(FeatureFormat format) {
		doSetValue(null, "format", format);
	}
}
