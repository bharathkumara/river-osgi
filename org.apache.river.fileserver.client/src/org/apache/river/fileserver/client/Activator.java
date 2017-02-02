package org.apache.river.fileserver.client;

import org.apache.river.example.fileserver.FileServer;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import net.jini.core.lookup.ServiceTemplate;

public class Activator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		ServiceTemplate template = new ServiceTemplate(null,new Class[]{FileServer.class},null);
		context.registerService(ServiceTemplate.class, template, null);	
		
	}

	@Override
	public void stop(BundleContext arg0) throws Exception {
		// TODO Auto-generated method stub

	}

}
