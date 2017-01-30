package org.argeo.suite.workbench;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/** The activator class controls the plug-in life cycle */
public class AsUiPlugin extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.argeo.suite.workbench.rap";

	// The shared instance
	private static AsUiPlugin plugin;

	private BundleContext bundleContext;

	/** Default constructor */
	public AsUiPlugin() {
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		this.bundleContext = context;
		plugin = this;
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static AsUiPlugin getDefault() {
		return plugin;
	}

	/** Creates the image */
	public static Image img(String path) {
		return getImageDescriptor(path).createImage();
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public URL imageUrl(String path) {
		return bundleContext.getBundle().getResource(path);
	}

	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {
	}

	public BundleContext getBundleContext() {
		return bundleContext;
	}
}
