package org.apache.river.osgi.client.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceID;

public class DefaultRiverServiceDiscoveryListener<T> extends RiverServiceDiscoveryListener<T> {

	private final static Logger logger = Logger.getLogger(DefaultRiverServiceDiscoveryListener.class.getName());
	private Map<ServiceID, T> serviceMap;
	private Map<ServiceID, Entry[]> attributesMap;

	public DefaultRiverServiceDiscoveryListener(Class<T> service) {
		super(service);
		serviceMap = new ConcurrentHashMap<>();
		attributesMap = new ConcurrentHashMap<>();
	}

	@Override
	public void serviceDiscovered(ServiceID serviceID, T service, Entry[] attrs) {
		logger.log(Level.FINEST, "River service is discovered. ServiceID={} , Service ={}, Attributes ={}",
				new Object[] { serviceID, service, Arrays.deepToString(attrs) });
		serviceMap.put(serviceID, service);
		attributesMap.put(serviceID, attrs);
	}

	@Override
	public void serviceDiscarded(ServiceID serviceID, T service) {
		logger.log(Level.FINEST,"River service is discarded. ServiceID={} , Service ={}", new Object[]{serviceID, service});
		serviceMap.remove(serviceID);
		attributesMap.remove(serviceID);
	}

	public T getService(ServiceID serviceID) {
		return serviceMap.get(serviceID);
	}

	public Entry[] getAttributes(ServiceID serviceID) {
		return attributesMap.get(serviceID);
	}

	public Map<ServiceID, T> getServiceMap() {
		return Collections.unmodifiableMap(serviceMap);
	}

	public Map<ServiceID, Entry[]> getAttributesMap() {
		return Collections.unmodifiableMap(attributesMap);
	}

	public void clear() {
		serviceMap.clear();
		attributesMap.clear();
	}

}
