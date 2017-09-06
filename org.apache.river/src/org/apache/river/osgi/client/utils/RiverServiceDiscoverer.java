package org.apache.river.osgi.client.utils;

import java.lang.reflect.Proxy;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.river.osgi.Constants;
import org.apache.river.osgi.services.RiverService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.wiring.BundleWiring;

import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.entry.AbstractEntry;
import net.jini.lookup.LookupCache;
import net.jini.lookup.ServiceDiscoveryEvent;
import net.jini.lookup.ServiceDiscoveryListener;
import net.jini.lookup.ServiceDiscoveryManager;
import net.jini.lookup.ServiceItemFilter;

class RiverServiceDiscoverer implements ServiceDiscoveryListener, RiverService {
	private final static Logger logger = Logger.getLogger(RiverServiceDiscoverer.class.getName());
	private ClassLoader bundleClassLoader;
	private ServiceTemplate template;
	private Bundle bundle;
	private String[] serviceTypes;
	private Map<ServiceID, ServiceRegistration<?>> serviceRegistrationMap;
	private ServiceDiscoveryManager discoveryManager;
	private LookupCache lookupCache;
	private Map<String, Object> properties;
	private ServiceDiscoveryListener serviceDiscoveryListener;
	private BundleContext context;

	private final static int SERVICE_ADD = 1;
	private final static int SERVICE_REMOVE = 0;

	public RiverServiceDiscoverer(BundleContext context, ServiceDiscoveryManager discoveryManager, Bundle bundle,
			ServiceTemplate template, Map<String, Object> properties) {
		this.context = context;
		this.discoveryManager = discoveryManager;
		this.bundle = bundle;
		this.template = template;
		this.properties = properties;
		this.bundleClassLoader = bundle.adapt(BundleWiring.class).getClassLoader();
		serviceTypes = convert(template.serviceTypes);
		serviceRegistrationMap = new ConcurrentHashMap<>();
		logger.log(Level.FINEST, "River Service discovery is constructed {}", this);
	}

	public void start() {
		logger.log(Level.FINEST, "Initiating the River discovery process for the template {} ", this);
		try {
			Object filterObject = properties.get(Constants.INTERNAL_PROP_SERVICE_ITEM_FILTER_KEY);
			if (filterObject != null && !(filterObject instanceof ServiceItemFilter)) {
				filterObject = null;
			}

			Object serviceListener = properties.get(Constants.INTERNAL_PROP_SERVICE_DISCOVERY_LISTENER_KEY);
			if (serviceListener == null || !(serviceListener instanceof ServiceDiscoveryListener)) {
				serviceDiscoveryListener = null;
			}
			lookupCache = discoveryManager.createLookupCache(template, (ServiceItemFilter) filterObject, this,
					bundleClassLoader);
		} catch (RemoteException e) {
			logger.log(Level.FINEST, "Failed to start the River discovery process for the service template {}", this);
			logger.log(Level.SEVERE, "River service discovery process is failed to start", e);
		}
	}

	public void stop() {
		logger.log(Level.FINEST, "Stopping the River discovery process for the template {} ", this);

		try {
			if (lookupCache != null) {
				lookupCache.terminate();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		Set<ServiceID> keySet = serviceRegistrationMap.keySet();
		for (ServiceID serviceID : keySet) {
			try {
				ServiceRegistration<?> registration = serviceRegistrationMap.get(serviceID);
				registration.unregister();
			} catch (Exception e) {
				logger.log(Level.FINE, "Failed to stop the River discovery process for the service template {}", this);
				logger.log(Level.FINEST, "River service discovery process is failed to stop", e);
			}
		}
	}

	@Override
	public void serviceAdded(ServiceDiscoveryEvent event) {
		logger.log(Level.FINEST, "River service is found for the template {} ", this);
		logger.log(Level.FINEST, "Started processing the discovered river service. ServiceTemplate =  {} ", this);
		Thread currentThread = Thread.currentThread();
		ClassLoader oldCL = currentThread.getContextClassLoader();
		currentThread.setContextClassLoader(bundleClassLoader);
		try {
			ServiceItem serviceItem = event.getPostEventServiceItem();
			if (serviceDiscoveryListener != null) {
				serviceDiscoveryListener.serviceAdded(event);
			} else {
				try {
					registerNetworkService(event);
				} catch (Exception e) {
					logger.log(Level.FINEST, "Failed to register the River service {} into OSGI registry", serviceItem);
					logger.log(Level.FINEST, "Failed to register the river service into OSGI registry", e);
				}
			}
			logger.log(Level.FINEST, "River service is found Service item = {} ", serviceItem);
			ServiceID serviceID = serviceItem.serviceID;
			Object service = serviceItem.service;
			Entry[] attributeSets = serviceItem.attributeSets;
			logger.log(Level.FINEST, "Notify RiverServiceDiscoveryListener for  serviceItem = {} ", serviceItem);
			notifyListeners(serviceID, service, attributeSets, SERVICE_ADD);
		} catch (Exception e) {
			logger.log(Level.FINEST, "Failed to process the new River service for the service template {} ", this);
			logger.log(Level.FINEST, "Failed to process the new River service", e);
			return;
		} finally {
			currentThread.setContextClassLoader(oldCL);
		}

		logger.log(Level.FINEST, "Completed the processing of the discovered river service. ServiceTemplate =  {} ",
				this);

	}

	private void notifyListeners(ServiceID id, Object service, Entry[] attrs, int eventType)
			throws InvalidSyntaxException {
		if (context == null) {
			return;
		}

		Collection<ServiceReference<RiverServiceDiscoveryListener>> references = context
				.getServiceReferences(RiverServiceDiscoveryListener.class, null);
		for (ServiceReference<RiverServiceDiscoveryListener> serviceReference : references) {
			RiverServiceDiscoveryListener listener = context.getService(serviceReference);
			Class type = listener.getType();
			boolean isProxyClass = Proxy.isProxyClass(service.getClass());
			boolean notify = false;
			if (isProxyClass) {
				Class<?>[] interfaces = service.getClass().getInterfaces();
				for (int i = 0; i < interfaces.length; i++) {
					if (interfaces[i].getName().equals(type.getName())) {
						notify = true;
						break;
					}
				}
			} else if (service.getClass().isAssignableFrom(type)) {
				notify = true;
			}
			if (notify) {
				if (eventType == SERVICE_ADD)
					listener.serviceDiscovered(id, service, attrs);
				else
					listener.serviceDiscarded(id, service);
			}
			context.ungetService(serviceReference);
		}
	}

	private String[] convert(Class<?>[] clazzes) {
		String[] ifaces = new String[clazzes.length];
		for (int i = 0; i < clazzes.length; i++) {
			ifaces[i] = clazzes[i].getName();

		}
		return ifaces;
	}

	@Override
	public void serviceRemoved(ServiceDiscoveryEvent event) {
		logger.log(Level.FINEST, "Started processing the discarded river service. ServiceTemplate =  {} ", this);
		logger.log(Level.FINEST, "River service is removed from the network. Template {} ", this);
		Thread currentThread = Thread.currentThread();
		ClassLoader oldCL = currentThread.getContextClassLoader();
		currentThread.setContextClassLoader(bundleClassLoader);
		try {

			if (serviceDiscoveryListener != null) {
				serviceDiscoveryListener.serviceRemoved(event);

			} else {
				unregisterNetworkService(event);
			}

			ServiceItem serviceItem = event.getPreEventServiceItem();
			notifyListeners(serviceItem.serviceID, serviceItem.service, serviceItem.attributeSets, SERVICE_REMOVE);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Failed to process the discarded river service for the service template {} ",
					this);
			logger.log(Level.SEVERE, "Failed to process the discard the river service", e);
		} finally {
			currentThread.setContextClassLoader(oldCL);
		}

	}

	@Override
	public void serviceChanged(ServiceDiscoveryEvent event) {
		// TODO Auto-generated method stub

	}

	private void registerNetworkService(ServiceDiscoveryEvent event) {
		ServiceItem serviceItem = event.getPostEventServiceItem();
		logger.log(Level.FINEST, "Trying to register the new river service to OSGI registry ServiceItem = {} ",
				serviceItem);
		ServiceID serviceID = serviceItem.serviceID;
		Object service = serviceItem.service;
		Dictionary<String, Object> properties = new Hashtable<>();
		properties.put(Constants.SERVICE_ID_KEY, serviceID);
		Entry[] attributeSets = serviceItem.attributeSets;
		for (int i = 0; i < attributeSets.length; i++) {
			try {
				properties.put(attributeSets[i].getClass().getName(), AbstractEntry.toString(attributeSets[i]));
			} catch (Exception e) {
				continue;
			}
		}
		ServiceRegistration<?> registration = bundle.getBundleContext().registerService(serviceTypes, service,
				properties);
		serviceRegistrationMap.put(serviceID, registration);
		logger.log(Level.FINEST,
				"Completed the sevice registration for the new river service Service Registration= {} ", registration);

	}

	private void unregisterNetworkService(ServiceDiscoveryEvent event) {
		ServiceItem serviceItem = event.getPreEventServiceItem();
		ServiceID serviceID = serviceItem.serviceID;
		logger.log(Level.FINEST,
				"Trying to unregister the discarded river service from OSGI registry, ServiceItem = {} ", serviceItem);
		if (serviceRegistrationMap.containsKey(serviceID)) {
			ServiceRegistration<?> registration = serviceRegistrationMap.get(serviceID);
			registration.unregister();
			serviceRegistrationMap.remove(serviceID);
		}

		logger.log(Level.FINEST,
				"Completed the task to unregister the discarded river service from OSGI registry, ServiceItem = {} ",
				serviceItem);

	}

	@Override
	public String toString() {
		return "ServiceDiscoveryHandler [template=" + template + ", bundle=" + bundle + ", properties=" + properties
				+ "]";
	}

}
