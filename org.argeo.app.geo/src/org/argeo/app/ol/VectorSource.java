package org.argeo.app.ol;

import org.argeo.app.ux.js.JsReference;

public class VectorSource extends Source {

	public VectorSource(Object... args) {
		super(args);
	}

	public VectorSource(String url, FeatureFormat format) {
		this(url, format, false);
	}

	public VectorSource(String url, FeatureFormat format, boolean bboxStrategy) {
		setFormat(format);
		if (bboxStrategy) {
			setUrl(url);
			getNewOptions().put("strategy", new JsReference(getJsPackage() + ".bbox"));
		} else {
			setUrl(url);
		}
	}

	public void setUrl(String url) {
		doSetValue(getMethodName(), "url", url);
	}

	public void setFormat(FeatureFormat format) {
		doSetValue(null, "format", format);
	}
}
