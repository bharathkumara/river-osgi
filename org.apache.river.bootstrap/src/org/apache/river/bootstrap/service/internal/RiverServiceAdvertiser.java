package org.apache.river.bootstrap.service.internal;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;

import org.apache.river.bootstrap.common.BootstrapLogger;
import org.apache.river.bootstrap.common.Constants;
import org.apache.river.bootstrap.common.RiverService;
import org.apache.river.bootstrap.common.RiverUtils;
import org.apache.river.bootstrap.service.RiverServiceDescriptor;
import org.apache.river.bootstrap.service.ServiceIDManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;

import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;
import net.jini.discovery.LookupDiscoveryManager;
import net.jini.lease.LeaseRenewalManager;
import net.jini.lookup.JoinManager;
import net.jini.lookup.ServiceIDListener;
import net.jini.lookup.entry.Host;

public class RiverServiceAdvertiser extends ServiceTracker<ServiceItem, ServiceRegistration<RiverServiceDescriptor>>
		implements RiverService {

	private final static Logger logger = BootstrapLogger.getRiverServiceAdvertiserLogger();
	private LeaseRenewalManager leaseManager;
	private LookupDiscoveryManager discoveryManager;

	public RiverServiceAdvertiser(BundleContext context, LookupDiscoveryManager discoveryManager,
			LeaseRenewalManager leaseManager, ServiceIDManager idManager) {
		super(context, ServiceItem.class, null);
		this.discoveryManager = discoveryManager;
		this.leaseManager = leaseManager;
	}

	@Override
	public ServiceRegistration<RiverServiceDescriptor> addingService(ServiceReference<ServiceItem> reference) {
		ServiceItem serviceItem = context.getService(reference);
		logger.info("Started the process to adverte the river service {} ", serviceItem);
		Object service = serviceItem.service;
		ServiceID serviceID = serviceItem.serviceID;
		Entry[] attrSets = serviceItem.attributeSets;

		Entry[] entries = RiverUtils.getSemanticClassVersionForProvider(service);
		Entry[] defaultEntries = getDefaultEntries();
		Entry[] calculatedEntries = RiverUtils.merge(entries, defaultEntries);
		attrSets = RiverUtils.merge(calculatedEntries, attrSets);
		logger.info("Advertising the river service {} ", serviceItem);
		JoinManager joinManager = null;
		try {
			if (serviceID == null) {
				ServiceIDListener listener = (ServiceIDListener) reference
						.getProperty(Constants.INTERNAL_PROP_SERVICE_ID_LISTENER_KEY);
				joinManager = new JoinManager(service, attrSets, listener, discoveryManager, leaseManager);
			} else {
				joinManager = new JoinManager(service, attrSets, serviceID, discoveryManager, leaseManager);
			}
			logger.info("Completed the Advertising the River service {} in the network.", serviceItem);

			Bundle bundle = reference.getBundle();
			long id = bundle.getBundleId();
			String bsn = bundle.getSymbolicName();
			String serviceName = service.getClass().getName();
			logger.info("Registering the RiverServiceDescriptor service in osgi for the service item.", serviceItem);
			RiverServiceDescriptor description = new RiverServiceDescriptor(id, bsn, serviceName, serviceItem,
					joinManager);
			return context.registerService(RiverServiceDescriptor.class, description, null);

		} catch (IOException e) {
			logger.error("Failed to Advertise the River service in the network.", e);
			return null;
		}

	}

	private Entry[] getDefaultEntries() {
		logger.info("Get Default Entry objects for the river service");
		try {
			Host hostName = new Host(Inet4Address.getLocalHost().getHostName());
			return new Entry[] { hostName };
		} catch (UnknownHostException e) {
			logger.error("Error while getting the default entry objects for the river service", e);
		}
		return new Entry[] {};
	}

	@Override
	public void removedService(ServiceReference<ServiceItem> reference,
			ServiceRegistration<RiverServiceDescriptor> service) {
		ServiceReference<RiverServiceDescriptor> serviceReference = service.getReference();
		RiverServiceDescriptor riverServiceDescription = context.getService(serviceReference);
		logger.info("Started the process to unadverte the river service {} ", riverServiceDescription.getItem());
		JoinManager joinManager = riverServiceDescription.getJoinManager();
		joinManager.terminate();
		context.ungetService(reference);
		service.unregister();
		logger.info("Completed the processing to unadverte the river service {}");
	}

	@Override
	public void start() {
		logger.info("Started to listen for River services in the current JVM");
		open();
	}

	@Override
	public void stop() {
		logger.info("Stopped to listen for River services in the current JVM");
		close();
	}

}
