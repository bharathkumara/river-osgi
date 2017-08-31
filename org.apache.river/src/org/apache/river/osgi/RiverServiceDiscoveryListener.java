package org.apache.river.osgi;

import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceID;

public abstract class RiverServiceDiscoveryListener<T> {

	protected Class<T> type;

	public RiverServiceDiscoveryListener(Class<T> service) {
		type = service;
	}

	public abstract void serviceDiscovered(ServiceID serviceID, T service, Entry[] attrs);

	public abstract void serviceDiscarded(ServiceID serviceID, T service);

	public Class<T> getType() {
		return type;
	}
}
