package org.apache.river.bootstrap.internal;

import java.io.File;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.river.bootstrap.client.internal.ServiceTemplateManager;
import org.apache.river.bootstrap.codeserver.EmbeddedCodeServer;
import org.apache.river.bootstrap.common.Constants;
import org.apache.river.bootstrap.common.RegistrarDiscoveryListener;
import org.apache.river.bootstrap.common.RiverCodeServer;
import org.apache.river.bootstrap.service.ServiceIDManager;
import org.apache.river.bootstrap.service.internal.RiverServiceAdvertiser;
import org.apache.river.bootstrap.service.internal.RiverServiceExporter;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;

import net.jini.discovery.LookupDiscoveryManager;
import net.jini.lease.LeaseRenewalManager;
import net.jini.lookup.ServiceDiscoveryManager;

public class RiverBootstrapActivator implements BundleActivator {

	private BundleContext context;
	public static RiverBootstrapActivator singleton;
	private String codeServerLocation;
	private RiverCodeServer codeServer;
	private LookupDiscoveryManager discoveryManager;
	private LeaseRenewalManager leaseManager;
	private RegistrarDiscoveryListener discoveryListener;
	private ServiceDiscoveryManager serviceDiscoveryManager;
	private ServiceTemplateManager serviceTemplateManager;
	private RiverServiceAdvertiser riverServiceAdvertiser;
	private IEclipsePreferences preferenceService;
	private ServiceIDManager serviceIDManager;
	RiverServiceExporter remoteServiceTracker;

	@Override
	public void start(BundleContext context) throws Exception {
		singleton = this;
		this.context = context;
		try {
			initializeLogger();
			initializeRiver();
			String serverType = context.getProperty("org.apache.river.codeserver.type");
			if (serverType == null) {
				serverType = "embedded";
			}
			if (serverType.equalsIgnoreCase("jetty")) {
				initializeJettyBasedCodeServer();
			} else {
				initializeEmbeddedCodeServer();
			}

			initializeRiverProcesses();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initializeLogger() {

		if ((context.getProperty("devmode")) != null) {
			/*
			 * SLF4JBridgeHandler.removeHandlersForRootLogger();
			 * SLF4JBridgeHandler.install();
			 */
		}

	}

	private void initializeRiverProcesses() throws IOException {
		discoveryListener = new RegistrarDiscoveryListener(context);
		discoveryManager = new LookupDiscoveryManager(null, null, discoveryListener);
		context.registerService(LookupDiscoveryManager.class, discoveryManager, null);

		leaseManager = new LeaseRenewalManager();
		context.registerService(LeaseRenewalManager.class, leaseManager, null);

		serviceDiscoveryManager = new ServiceDiscoveryManager(discoveryManager, leaseManager);
		context.registerService(ServiceDiscoveryManager.class, serviceDiscoveryManager, null);

		// client tracking
		serviceTemplateManager = new ServiceTemplateManager(context, serviceDiscoveryManager);
		serviceTemplateManager.start();
		// service tracking

		preferenceService = ConfigurationScope.INSTANCE.getNode("");
		serviceIDManager = new ServiceIDManager(preferenceService);

		riverServiceAdvertiser = new RiverServiceAdvertiser(context, discoveryManager, leaseManager, serviceIDManager);
		riverServiceAdvertiser.start();

		try {
			Filter filter = null;
			filter = context.createFilter("(" + Constants.REMOTE_SERVICE_EXPORT_CONFIG_KEY + "="
					+ Constants.REMOTE_SERVICE_EXPORT_CONFIG_VALUE + ")");
			remoteServiceTracker = new RiverServiceExporter(context, filter, serviceIDManager);
			remoteServiceTracker.start();
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		}

	}

	private void initializeJettyBasedCodeServer() {
		/*
		 * httpServiceTracker = new HttpServiceTracker(context);
		 * httpServiceTracker.open();
		 */
	}

	private void initializeEmbeddedCodeServer() {
		String property = context.getProperty("org.apache.river.lookup.codeserver.location");
		if (property == null) {
			property = System.getProperty("user.home") + File.separatorChar + "org.apache.river.lookup.codeserver";
		}
		File f = new File(property);
		if (!f.exists()) {
			f.mkdirs();
		}
		codeServerLocation = f.getAbsolutePath();
		codeServer = new EmbeddedCodeServer();
		try {
			codeServer.start();
			Dictionary<String, Object> props = new Hashtable<>();
			props.put("org.apache.river.lookup.codeserver.port", codeServer.getPort());
			props.put("org.apache.river.lookup.codeserver.location", codeServerLocation);
			context.registerService(RiverCodeServer.class.getName(), codeServer, props);
		} catch (Exception e) {
			e.printStackTrace();
			codeServer = null;
		}
	}

	private void initializeRiver() {

		SecurityManager securityManager = System.getSecurityManager();
		if (securityManager == null) {
			System.setSecurityManager(new SecurityManager());
		}

		String policy = System.getProperty("java.security.policy");
		if (policy == null) {
			String property = System.getProperty("user.home");
			System.setProperty("java.security.policy", property + ".java.policy");
		}
		System.setProperty("java.rmi.server.useCodebaseOnly", "false");
		System.setProperty("java.rmi.server.ignoreStubClasses", "true");
		System.setProperty("net.jini.loader.ClassLoading.provider", "net.jini.loader.pref.PreferredClassProvider");

	}

	@Override
	public void stop(BundleContext arg0) throws Exception {
		singleton = null;
		stopCodeServer();
		stopRiverServices();

	}

	private void stopRiverServices() {

		context.registerService(LookupDiscoveryManager.class, discoveryManager, null);

		try {
			if (discoveryManager != null) {
				discoveryManager.terminate();
				discoveryManager = null;
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		try {
			if (leaseManager != null) {
				leaseManager.close();
				leaseManager = null;
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		try {
			if (serviceDiscoveryManager != null) {
				serviceDiscoveryManager.terminate();
				serviceDiscoveryManager = null;
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		try {
			if (serviceTemplateManager != null) {
				serviceTemplateManager.stop();
				serviceTemplateManager = null;
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		try {
			if (riverServiceAdvertiser != null) {
				riverServiceAdvertiser.stop();
				riverServiceAdvertiser = null;
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		if (remoteServiceTracker != null) {
			remoteServiceTracker.close();
			remoteServiceTracker = null;
		}

	}

	private void stopCodeServer() {

		if (codeServer != null) {
			try {
				codeServer.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
			codeServer = null;
		}
		

	}

}
