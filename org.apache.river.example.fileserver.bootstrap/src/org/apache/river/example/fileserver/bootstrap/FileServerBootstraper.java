package org.apache.river.example.fileserver.bootstrap;

import java.io.InputStream;
import java.net.URL;
import java.rmi.RemoteException;

import org.apache.river.admin.DestroyAdmin;
import org.apache.river.bootstrap.common.RiverCodeServer;
import org.apache.river.start.NonActivatableServiceDescriptor;
import org.apache.river.start.NonActivatableServiceDescriptor.Created;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import net.jini.config.DynamicConfiguration;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;
import net.jini.id.Uuid;
import net.jini.id.UuidFactory;

public class FileServerBootstraper {

	private static String implementation = "fileserver/org.apache.river.example.fileserver.impl.jar";
	private static String proxy = "fileserver/org.apache.river.example.fileserver.proxy.jar";
	private DestroyAdmin fileServer;
	private boolean serviceStarted;
	private ServiceRegistration<ServiceItem> serviceRegistration;

	public FileServerBootstraper(BundleContext context, RiverCodeServer codeServer) {

		try {
			URL implURL = context.getBundle().getEntry(implementation);

			URL proxyURL = context.getBundle().getEntry(proxy);
			InputStream stream = proxyURL.openStream();
			String codebase = codeServer.addJars("fileserver", stream, "org.apache.river.example.fileserver.proxy.jar");

			String url1 = implURL.toString();
			String url2 = proxyURL.toString();

			String importCodebase = url1 + " " + url2;

			String policyFile = System.getProperty("user.home") + "/.java.policy";
			ClassLoader oldCCL = Thread.currentThread().getContextClassLoader();
			DynamicConfiguration config = new DynamicConfiguration();
			try {
				Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
				NonActivatableServiceDescriptor desc = new NonActivatableServiceDescriptor(codebase, policyFile,
						importCodebase, "org.apache.river.example.fileserver.impl.InternalFileServerImpl", config, null,
						null);

				DynamicConfiguration con = new DynamicConfiguration();
				Created created = (Created) desc.create(con);

				fileServer = (DestroyAdmin) created.impl;

				Uuid uuid = UuidFactory.generate();
				ServiceID id = new ServiceID(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
				ServiceItem item = new ServiceItem(id, created.proxy, null);
				serviceRegistration = context.registerService(ServiceItem.class, item, null);

				System.out.println("File server is started");
				serviceStarted = true;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				Thread.currentThread().setContextClassLoader(oldCCL);
			}

		} catch (Exception e) {
			e.printStackTrace();
			serviceStarted = false;
		}

	}

	public void stop() {
		try {
			if (fileServer != null) {
				fileServer.destroy();
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		if(serviceRegistration != null){
			serviceRegistration.unregister();
		}

	}
}
