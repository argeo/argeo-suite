
package org.argeo.suite.e4;

import org.eclipse.e4.core.di.annotations.Evaluate;

public class ActiveFeature {
	
	
	public ActiveFeature() {
		super();
	}

	@Evaluate
	public boolean evaluate() {
		return false;
	}
}
