package org.apache.river.osgi;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.lookup.ServiceDiscoveryManager;

public class ServiceTemplateManager extends ServiceTracker<ServiceTemplate, RiverServiceDiscoverer>
		implements RiverService {

	ServiceDiscoveryManager manager;
	private ServiceDiscoveryManager discoveryManager;

	public ServiceTemplateManager(BundleContext context, ServiceDiscoveryManager discoveryManager) {
		super(context, ServiceTemplate.class, null);
		this.discoveryManager = discoveryManager;
	}

	@Override
	public RiverServiceDiscoverer addingService(ServiceReference<ServiceTemplate> reference) {
		ServiceTemplate template = context.getService(reference);
		Entry[] attributeSetTemplates = template.attributeSetTemplates;
		Entry[] classVersions = RiverUtils.getSemanticClassVersion(template);
		Entry[] mergedEntries = RiverUtils.merge(classVersions, attributeSetTemplates);
		template.attributeSetTemplates = mergedEntries;
		Map<String, Object> properties = new HashMap<>();
		String[] propertyKeys = reference.getPropertyKeys();
		for (int i = 0; i < propertyKeys.length; i++) {
			Object value = reference.getProperty(propertyKeys[i]);
			properties.put(propertyKeys[i], value);
		}
		Bundle bundle = reference.getBundle();
		RiverServiceDiscoverer handler = new RiverServiceDiscoverer(context, discoveryManager, bundle, template,
				properties);
		handler.start();
		return handler;
	}

	@Override
	public void removedService(ServiceReference<ServiceTemplate> reference, RiverServiceDiscoverer service) {
		service.stop();

	}

	@Override
	public void start() {
		open();
	}

	@Override
	public void stop() {
		SortedMap<ServiceReference<ServiceTemplate>, RiverServiceDiscoverer> tracked2 = getTracked();
		Set<ServiceReference<ServiceTemplate>> keySet = tracked2.keySet();
		for (ServiceReference<ServiceTemplate> serviceReference : keySet) {
			try {
				ServiceTemplate service = context.getService(serviceReference);
				RiverServiceDiscoverer serviceDiscoveryHandler = tracked2.get(service);
				serviceDiscoveryHandler.stop();
			} catch (Exception e) {
				continue;
			}
		}
		close();
		discoveryManager = null;
	}

}
