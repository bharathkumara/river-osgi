package org.apache.river;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	@Override
	public void start(BundleContext arg0) throws Exception {
		System.out.println("River Fragment is started");

	}

	@Override
	public void stop(BundleContext arg0) throws Exception {
		System.out.println("River Fragment is stopped");

	}

}
