package org.apache.river.osgi;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.server.ExportException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;
import net.jini.export.Exporter;
import net.jini.id.Uuid;
import net.jini.id.UuidFactory;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.tcp.TcpServerEndpoint;
import net.jini.lookup.ServiceIDListener;

public class RiverServiceExporter extends ServiceTracker<Object, ServiceRegistration<ServiceItem>>
		implements RiverService {
	private final static Logger logger = Logger.getLogger("RiverService-Advertiser");
	private ServiceIDManager idManager;
	private Map<ServiceRegistration<ServiceItem>, ServiceItem> map = new IdentityHashMap<>();

	public RiverServiceExporter(BundleContext context, Filter filter) {
		super(context, filter, null);
	}

	@Override
	public ServiceRegistration<ServiceItem> addingService(ServiceReference<Object> reference) {

		Object service = context.getService(reference);
		logger.log(Level.FINE, "Checking the OSGI service for river service properties {} ", reference);
		if (!(service instanceof Remote)) {
			logger.log(Level.FINE, "OSGI service is not a remote object. Ignoring this service to export", reference);
			logger.log(Level.INFO, "OSGI service is not a river service {} ", reference);
			return null;
		}
		String property = (String) reference.getProperty(Constants.REMOTE_SERVICE_EXPORT_CONFIG_KEY);
		if (property == null) {
			logger.log(Level.FINE, "OSGI service is not a having the property {} . Ignoring this service to export",
					Constants.REMOTE_SERVICE_EXPORT_CONFIG_KEY);
			logger.log(Level.INFO, "OSGI service is not a river service {} ", reference);
			return null;
		}
		String config = (String) reference.getProperty(Constants.REMOTE_SERVICE_EXPORT_CONFIG_KEY);
		if (config == null || !config.equals(Constants.REMOTE_SERVICE_EXPORT_CONFIG_VALUE)) {
			return null;
		}
		Map<String, Object> serviceProperties = RiverUtils.getServiceProperties(reference);

		Dictionary<String, Object> serviceItemProperties = new Hashtable<>();

		Object exporterKey = reference.getProperty(Constants.REMOTE_SERVICE_JERI_EXPORTER_KEY);
		Exporter exporter = null;
		if (exporterKey != null && exporterKey instanceof String) {
			logger.log(Level.FINE,"River Exporter property is found. Checking for the requested exporter ",
					Constants.REMOTE_SERVICE_EXPORT_CONFIG_KEY);
			if (exporterKey.equals(Constants.REMOTE_SERVICE_JERI_NO_EXPORTER)) {
				logger.log(Level.FINE,"River Exporter property is {}. ", Constants.REMOTE_SERVICE_JERI_NO_EXPORTER);
				logger.log(Level.FINE,"No Exporter will be created. OSGI service object will be exported");
				serviceItemProperties.put(Constants.INTERNAL_PROP_SERVICE_EXPORTER, null);
			} else {
				logger.log(Level.FINE,"River Exporter property is {}. ", exporterKey);
				exporter = getExporter(serviceProperties);
				serviceItemProperties.put(Constants.INTERNAL_PROP_SERVICE_EXPORTER, exporter);
				logger.log(Level.FINE,"Exporter will be created. OSGI service object will be exported using exporter {}",
						exporter);
			}

		}

		String rootPath = getServicePreferenceNodePath(reference);
		ServiceID serviceID = idManager.getServiceID(rootPath);

		if (serviceID == null) {
			Uuid uuid = UuidFactory.generate();
			serviceID = new ServiceID(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
			serviceItemProperties.put(Constants.SERVICE_ID_KEY, serviceID);
		} else {
			serviceItemProperties.put(Constants.SERVICE_ID_KEY, serviceID);
		}
		try {
			Object proxy = null;
			if (exporter != null) {
				proxy = exporter.export((Remote) service);
			} else {
				if (service instanceof Serializable) {
					proxy = service;
				} else {
					return null;
				}
			}
			ServiceItem item = new ServiceItem(serviceID, proxy, null);
			logger.log(Level.FINEST,"OSGI service is exported as a river service {} ", item);
			ServiceRegistration<ServiceItem> serviceRegistration = context.registerService(ServiceItem.class, item,
					serviceItemProperties);
			logger.log(Level.FINEST,"Registering the river service in osgi registry {} ", serviceRegistration);
			synchronized (map) {
				map.put(serviceRegistration, item);
			}
			return serviceRegistration;
		} catch (ExportException e) {
			logger.log(Level.SEVERE,"Failed to export OSGI service as a river service {} ", e);
			return null;
		}
	}

	@Override
	public void removedService(ServiceReference<Object> reference, ServiceRegistration<ServiceItem> service) {
		logger.log(Level.FINEST,"Unregistering the river service registration {} ", service);
		Exporter exporter = (Exporter) service.getReference().getProperty(Constants.INTERNAL_PROP_SERVICE_EXPORTER);
		logger.log(Level.FINEST,"Unexport the river service {} using the exporter ", exporter);
		if (exporter != null) {
			exporter.unexport(true);
		}
		synchronized (map) {
			map.remove(service);
		}
		service.unregister();
		logger.log(Level.FINEST,"Completed the unregistration of river service from osgi registry", exporter);
		context.ungetService(reference);
	}

	private String getServicePreferenceNodePath(ServiceReference<Object> reference) {
		Bundle bundle = reference.getBundle();
		Object preferencePath = reference.getProperty("PreferenceTreePath");
		String serviceName = "";
		if (preferencePath == null || !(preferencePath instanceof String)) {
			serviceName = context.getService(reference).getClass().getName();
		} else {
			serviceName = preferencePath.toString();
		}
		String symbolicName = bundle.getSymbolicName();
		String rootPath = symbolicName + "/" + serviceName;
		return rootPath;
	}

	private Entry createServicePropertiesEntry(Map<String, Object> serviceProperties) {

		Set<String> keySet = serviceProperties.keySet();
		for (String key : keySet) {
			Object value = serviceProperties.get(key);
			if (value instanceof Serializable) {

			}
		}
		return null;

	}

	private Dictionary createServiceItemProperties(Map<String, Object> serviceProperties) {
		Dictionary<String, Object> properties = new Hashtable<>();
		Set<String> keySet = serviceProperties.keySet();
		for (String key : keySet) {

		}

		return properties;

	}

	private Exporter getExporter(Map<String, Object> serviceProperties) {

		Object object = serviceProperties.get(Constants.REMOTE_SERVICE_JERI_EXPORTER_PORT_KEY);
		int port = 0;
		if (object != null) {
			try {
				port = Integer.parseInt(object.toString());
			} catch (Exception exception) {
				// Ignore
			}
		}
		String config = (String) serviceProperties.get(Constants.REMOTE_SERVICE_JERI_EXPORTER_KEY);

		if (config.equals(Constants.REMOTE_SERVICE_JERI_EXPORTER_TCP)) {
			return createTCPEndpointExporter(port, false);
		} else if (config.equals(Constants.REMOTE_SERVICE_JERI_EXPORTER_HTTP)) {
			return createHttpEndpointExporter(port, false);
		} else if (config.equals(Constants.REMOTE_SERVICE_JERI_EXPORTER_SSL)) {
			return createHttpEndpointExporter(port, true);
		} else if (config.equals(Constants.REMOTE_SERVICE_JERI_EXPORTER_HTTPS)) {
			return createHttpEndpointExporter(port, true);
		} else {
			return createTCPEndpointExporter(port, false);
		}

	}

	private Exporter createHttpEndpointExporter(int port, boolean ssl) {
		return null;
	}

	private Exporter createTCPEndpointExporter(int port, boolean ssl) {
		return new BasicJeriExporter(TcpServerEndpoint.getInstance(port), new BasicILFactory());
	}

	@Override
	public void start() {
		open();
	}

	@Override
	public void stop() {
		close();
	}

	private static class IDListener implements ServiceIDListener {

		private String path;
		private ServiceIDManager manager;

		public IDListener(ServiceIDManager manager, String path) {
			this.manager = manager;
			this.path = path;
		}

		@Override
		public void serviceIDNotify(ServiceID serviceID) {
			manager.putServiceID(path, serviceID);
			manager = null;
			path = null;
		}

	}

}
