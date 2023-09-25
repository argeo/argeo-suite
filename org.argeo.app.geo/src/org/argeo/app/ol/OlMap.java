package org.argeo.app.ol;

public class OlMap extends AbstractOlObject {

	public OlMap(Object... args) {
		super(args);
	}

	public void addLayer(Layer layer) {
		executeMethod(getMethodName(), layer);
	}

	public View getView() {
		return new View(getJsClient(), getReference() + ".getView()");
	}

	@Override
	public String getJsClassName() {
		return getJsPackage() + ".Map";
	}

}
