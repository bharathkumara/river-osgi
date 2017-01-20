package org.apache.river.bootstrap.common;

import java.util.Arrays;
import java.util.Collection;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Component;

import net.jini.core.discovery.LookupLocator;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceRegistrar;

@Component(immediate = true, property = { "osgi.command.function=registrars", "osgi.command.scope=river" }, service = {
		Object.class })
public class RegistrarManager {

	public void registrars(String... args) {
		try {
			BundleContext bundleContext = FrameworkUtil.getBundle(getClass()).getBundleContext();
			Collection<ServiceReference<ServiceRegistrar>> references = bundleContext
					.getServiceReferences(ServiceRegistrar.class, null);

			int maxLength = 12;

			String[][] store = new String[references.size()][3];
			int i = 0;
			for (ServiceReference<ServiceRegistrar> serviceReference : references) {
				ServiceRegistrar serviceRegistrar = bundleContext.getService(serviceReference);
				String host = null;
				ServiceID serviceID = null;
				String[] groups = null;
				try {
					LookupLocator locator = serviceRegistrar.getLocator();
					host = locator.getHost();
					serviceID = serviceRegistrar.getServiceID();
					groups = serviceRegistrar.getGroups();
				} catch (Exception e) {
					continue;
				}
				if (maxLength < host.length())
					maxLength = host.length();

				store[i][0] = host;
				store[i][1] = serviceID.toString();
				store[i][2] = Arrays.deepToString(groups);
				bundleContext.ungetService(serviceReference);
				i++;
			}
			if (store == null || store.length == 0) {
				System.out.println("\tNo lookup service found");
				return;
			}
			System.out.println("\nDiscovered Lookup Services");
			maxLength += 5;

			System.out.println(
					String.format("  %-" + maxLength + "s | %-40s | %-10s", "Host Name", "ServiceID", "Groups"));
			for (int j = 0; j < store.length; j++) {
				if (store[j][0] != null)
					System.out.println(String.format("  %-" + maxLength + "s | %-40s | %-10s", store[j][0], store[j][1],
							store[j][2]));
			}
			System.out.println("\n");
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		}
	}

}
