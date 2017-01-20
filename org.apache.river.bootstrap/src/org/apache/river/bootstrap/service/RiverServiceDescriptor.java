package org.apache.river.bootstrap.service;

import net.jini.core.lookup.ServiceItem;
import net.jini.lookup.JoinManager;

public class RiverServiceDescriptor {

	private long bundleID;
	private String bsn;
	private String serviceName;
	private ServiceItem item;
	private JoinManager joinManager;

	public RiverServiceDescriptor(long id, String bsn, String service, ServiceItem item, JoinManager manager) {
		bundleID = id;
		this.bsn = bsn;
		serviceName = service;
		this.item = item;
		joinManager = manager;
	}

	public long getBundleID() {
		return bundleID;
	}

	public String getBsn() {
		return bsn;
	}

	public String getServiceName() {
		return serviceName;
	}

	public ServiceItem getItem() {
		return item;
	}

	public JoinManager getJoinManager() {
		return joinManager;
	}

}
