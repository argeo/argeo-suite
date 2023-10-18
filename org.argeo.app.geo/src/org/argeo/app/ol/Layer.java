package org.argeo.app.ol;

import java.util.Objects;

public class Layer extends AbstractOlObject {
	public final static String NAME_KEY = "name";

	// cached
	private String name;

	public Layer(Object... args) {
		super(args);
	}

	public void setOpacity(double opacity) {
		if (opacity < 0 || opacity > 1)
			throw new IllegalArgumentException("Opacity must be between 0 and 1");
//		if (isNew())
//			getNewOptions().put("opacity", opacity);
//		else
//			executeMethod(getMethodName(), opacity);
		doSetValue(getMethodName(), "opacity", opacity);
	}

	public void setSource(Source source) {
		Objects.requireNonNull(source);
		if (isNew())
			getNewOptions().put("source", source);
		else
			executeMethod(getMethodName(), source);
	}

	public Source getSource() {
		String reference = getReference() + ".getSource()";
		return new Source(getJsClient(), reference);
	}

	public void setMinResolution(double minResolution) {
		if (isNew())
			getNewOptions().put("minResolution", minResolution);
		else
			executeMethod(getMethodName(), minResolution);
	}

	public void setMaxResolution(double maxResolution) {
		if (isNew())
			getNewOptions().put("maxResolution", maxResolution);
		else
			executeMethod(getMethodName(), maxResolution);
	}

	public void setName(String name) {
		set(NAME_KEY, name);
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
