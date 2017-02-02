package org.apache.river.example.fileserver.bootstrap;

import org.apache.river.bootstrap.common.RiverCodeServer;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class Activator implements BundleActivator {

	private FileServerBootstraper serviceBootstraper;
	private ServiceReference<RiverCodeServer> serviceReference;

	@Override
	public void start(BundleContext context) throws Exception {
		serviceReference = context.getServiceReference(RiverCodeServer.class);
		if (serviceReference != null) {
			RiverCodeServer codeServer = context.getService(serviceReference);
			serviceBootstraper = new FileServerBootstraper(context, codeServer);
		}

	}

	@Override
	public void stop(BundleContext context) throws Exception {
		if (serviceBootstraper != null) {
			serviceBootstraper.stop();
		}

		if (serviceReference != null) {
			context.ungetService(serviceReference);
		}
	}

}
