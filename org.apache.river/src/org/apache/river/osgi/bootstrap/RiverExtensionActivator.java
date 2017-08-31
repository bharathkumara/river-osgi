package org.apache.river.osgi.bootstrap;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class RiverExtensionActivator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		System.out.println("River Fragment is started");

	}

	@Override
	public void stop(BundleContext context) throws Exception {
		System.out.println("River Fragment is stopped");

	}

}
