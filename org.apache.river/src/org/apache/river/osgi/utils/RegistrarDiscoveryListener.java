package org.apache.river.osgi.utils;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.discovery.DiscoveryChangeListener;
import net.jini.discovery.DiscoveryEvent;
import net.jini.discovery.DiscoveryListener;

public class RegistrarDiscoveryListener implements DiscoveryChangeListener {

	private static final Logger logger = Logger.getLogger(RegistrarDiscoveryListener.class.getName());
	private BundleContext context;
	private static int DISCOVERED = 1;
	private static int DISCARDED = 0;
	private static int CHANGED = 2;

	private Map<ServiceID, ServiceRegistration<ServiceRegistrar>> registrarsMap = new ConcurrentHashMap<>();

	public RegistrarDiscoveryListener(BundleContext context) {
		this.context = context;
	}

	@Override
	public void discovered(DiscoveryEvent e) {

		if (context == null) {
			logger.log(Level.FINER, "Bundle Context is null. Lookup Discovery Listener is not removed properly");
			return;
		}
		logger.log(Level.FINE, "Lookup registrar discovered.");
		ServiceRegistrar[] registrars = e.getRegistrars();
		for (int i = 0; i < registrars.length; i++) {
			logger.log(Level.FINE, "Registrar found {}", registrars[i]);
			ServiceRegistration<ServiceRegistrar> serviceRegistration = context.registerService(ServiceRegistrar.class,
					registrars[i], null);
			registrarsMap.put(registrars[i].getServiceID(), serviceRegistration);
		}

		notifyListeners(e, DISCOVERED);
	}

	private void notifyListeners(DiscoveryEvent e, int eventType) {
		try {
			logger.log(Level.FINEST, "Started notifying the discovery listeners.");
			Collection<ServiceReference<DiscoveryListener>> references = context
					.getServiceReferences(DiscoveryListener.class, null);
			for (ServiceReference<DiscoveryListener> serviceReference : references) {
				try {

					DiscoveryListener listener = context.getService(serviceReference);
					if (eventType == DISCOVERED) {
						logger.log(Level.FINEST, "Notify Discovery Listener about new Registrar {} ", serviceReference);
						listener.discovered(e);
					} else if (eventType == DISCARDED) {
						logger.log(Level.FINEST, "Notify Discovery Listener about discarded Registrar {} ",
								serviceReference);
						listener.discarded(e);
					} else {
						if (listener instanceof DiscoveryChangeListener) {
							logger.log(Level.FINEST, "Notify Discovery Listener about changed Registrar {} ",
									serviceReference);
							((DiscoveryChangeListener) listener).changed(e);
						}
					}
				} catch (Exception e1) {
					logger.log(Level.SEVERE, "Error while notifying the Discovery Listener", e1);
				}
			}

			logger.log(Level.FINEST, "Completed notifying the discovery listeners.");
		} catch (InvalidSyntaxException e1) {
			logger.log(Level.WARNING, "Error while notifying the Discovery Listener", e1);
		}

	}

	@Override
	public void discarded(DiscoveryEvent e) {
		if (context == null) {
			logger.log(Level.WARNING, "Bundle Context is null. Lookup Discovery Listener is not removed properly");
			return;
		}
		ServiceRegistrar[] registrars = e.getRegistrars();
		for (int i = 0; i < registrars.length; i++) {
			logger.log(Level.FINEST, "Registrar discarded {}", registrars[i]);
			ServiceID serviceID = registrars[i].getServiceID();
			ServiceRegistration<ServiceRegistrar> remove = registrarsMap.remove(serviceID);
			if (remove != null) {
				remove.unregister();
			}
		}
		notifyListeners(e, DISCARDED);

	}

	@Override
	public void changed(DiscoveryEvent e) {

		if (context == null) {
			logger.log(Level.FINEST,"Bundle Context is null. Lookup Discovery Listener is not removed properly");
			return;
		}
		ServiceRegistrar[] registrars = e.getRegistrars();
		for (int i = 0; i < registrars.length; i++) {
			logger.log(Level.FINEST,"Registrar changed {}", registrars[i]);
			ServiceID serviceID = registrars[i].getServiceID();
			ServiceRegistration<ServiceRegistrar> remove = registrarsMap.remove(serviceID);
			if (remove != null) {
				remove.unregister();
			}
			ServiceRegistration<ServiceRegistrar> serviceRegistration = context.registerService(ServiceRegistrar.class,
					registrars[i], null);
			registrarsMap.put(registrars[i].getServiceID(), serviceRegistration);
		}
		notifyListeners(e, CHANGED);

	}

}
