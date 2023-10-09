package org.argeo.app.ol;

public class View extends AbstractOlObject {
	public View(Object... args) {
		super(args);
	}

	public void setCenter(int[] coord) {
		if (isNew())
			getNewOptions().put("center", coord);
		else
			executeMethod(getMethodName(), new Object[] { coord });
	}

	public void setZoom(int zoom) {
		if (isNew())
			getNewOptions().put("zoom", zoom);
		else
			executeMethod(getMethodName(), zoom);
	}

//	public void fit(double[] extent) {
//		executeMethod(getMethodName(), extent);
//	}
//	public void setProjection(String projection) {
//		doSetValue(getMethodName(), "projection", projection);
//	}
}
