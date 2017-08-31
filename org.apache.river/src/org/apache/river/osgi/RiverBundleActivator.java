package org.apache.river.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class RiverBundleActivator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		System.out.println("River Bundle is started");
		
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		System.out.println("River bundle is stopped");
		
	}

}
