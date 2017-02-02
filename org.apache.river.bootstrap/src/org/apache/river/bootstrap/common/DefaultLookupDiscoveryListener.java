package org.apache.river.bootstrap.common;

import org.osgi.framework.BundleContext;

import net.jini.discovery.DiscoveryEvent;
import net.jini.discovery.DiscoveryListener;

public class DefaultLookupDiscoveryListener implements DiscoveryListener {

	public DefaultLookupDiscoveryListener(BundleContext context) {

	}

	@Override
	public void discovered(DiscoveryEvent e) {

	}

	@Override
	public void discarded(DiscoveryEvent e) {
		// TODO Auto-generated method stub

	}

}
