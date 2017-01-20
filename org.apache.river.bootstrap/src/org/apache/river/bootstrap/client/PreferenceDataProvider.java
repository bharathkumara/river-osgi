package org.apache.river.bootstrap.client;

import java.io.Serializable;

import net.jini.core.lookup.ServiceItem;

public interface PreferenceDataProvider<T extends Serializable> {

	public T addingService(ServiceItem item);

	public T removingService(ServiceItem item, T storedData);
}
