package org.apache.river.service.lookup;

import java.io.InputStream;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.river.admin.DestroyAdmin;
import org.apache.river.osgi.services.RiverCodeServer;
import org.apache.river.start.NonActivatableServiceDescriptor;
import org.apache.river.start.NonActivatableServiceDescriptor.Created;
import org.apache.river.start.ServiceStarter;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import net.jini.config.DynamicConfiguration;
import net.jini.export.Exporter;
import net.jini.jrmp.JrmpExporter;

@Component(immediate = true, property = { "osgi.command.function=lookup", "osgi.command.scope=river" })
public class LookupServiceImpl implements LookupService {

	private RiverCodeServer codeServer;
	private static final Logger logger = Logger.getLogger(ServiceStarter.class.getName());
	private String dlFile = "reggie/org.apache.river.lookup.proxy.jar";
	private DestroyAdmin lookupService;
	private BundleContext context;
	private boolean serviceStarted = false;

	@Reference(cardinality = ReferenceCardinality.MANDATORY, unbind = "unsetCodeServer")
	public void setCodeServer(RiverCodeServer codeServer) {
		this.codeServer = codeServer;

	}

	public void unsetCodeServer(RiverCodeServer codeServer) {
		this.codeServer = null;
	}

	public void lookup(String[] args) {

		String command;
		if (args.length == 0) {
			command = "status";
		} else {
			command = args[0];
		}
		if (command.equalsIgnoreCase("start")) {
			start();

		} else if (command.equalsIgnoreCase("stop")) {
			stop();
		} else if (command.equals("status")) {
			if (serviceStarted) {
				System.out.println("\tLookup service is running here.");
			} else {
				System.out.println("\tLookup service is not running here");
			}

		}

	}

	@Activate
	public void activate(ComponentContext context, BundleContext bc, Map<String, ?> properties) {
		this.context = bc;

	}

	@Override
	public void start() {

		try {
			URL entry = context.getBundle().getEntry(dlFile);
			InputStream stream = entry.openStream();
			String importCodebase = "reggie/reggie.jar";
			String codebase = codeServer.addJars("reggie", stream, "org.apache.river.lookup.proxy.jar");
			String config = "reggie/jrmp-reggie.config";
			URL url = context.getBundle().getEntry(config);
			String configLocation = url.toExternalForm();
			URL classpath = context.getBundle().getEntry(importCodebase);

			String policyFile = System.getProperty("user.home") + "/.java.policy";
			ClassLoader oldCCL = Thread.currentThread().getContextClassLoader();
			try {
				Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
				NonActivatableServiceDescriptor desc = new NonActivatableServiceDescriptor(codebase, policyFile,
						classpath.toString(), "org.apache.river.reggie.TransientRegistrarImpl",
						new String[] { configLocation });

				DynamicConfiguration con = new DynamicConfiguration();
				con.setEntry("org.apache.river.reggie", "serverExporter", Exporter.class, new JrmpExporter());
				con.setEntry("org.apache.river.reggie", "initialMemberGroups", String[].class,
						new String[] { "nonsecure.hello.example.jini.sun.com" });
				Created created = (Created) desc.create(con);
				lookupService = (DestroyAdmin) created.impl;
				System.out.println("Lookup service is started");
				serviceStarted = true;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				Thread.currentThread().setContextClassLoader(oldCCL);
			}

		} catch (Exception e) {
			e.printStackTrace();
			serviceStarted = false;
		}

	}

	@Override
	public void stop() {

		try {
			if (lookupService != null) {
				lookupService.destroy();
				System.out.println("Lookup service is stopped");
				serviceStarted = false;

			}
		} catch (RemoteException e) {
			System.out.println("Failed to stop the lookup service.");
			serviceStarted = false;
			e.printStackTrace();
		}

	}

	@Deactivate
	public void deactivate() {
		stop();
		this.context = null;

	}

}
