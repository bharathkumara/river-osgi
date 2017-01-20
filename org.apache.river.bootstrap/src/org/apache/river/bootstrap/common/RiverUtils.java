package org.apache.river.bootstrap.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRequirement;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWiring;

import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.lookup.entry.ClassVersion;

public class RiverUtils {

	private static int SEMANTIC_VERSION_CONSUMER = 1;
	private static int SEMANTIC_VERSION_PRVIDER = 2;

	public static Map<String, Version> extractClassVersions(Class<?>[] classes) {
		Map<String, Version> classVersionMap = getClassVersionMap(classes);
		return classVersionMap;
	}

	public static Entry getSemanticClassVersion(String className, Version version, int type) {
		if (className == null || version == null) {
			return null;
		}
		if (type == SEMANTIC_VERSION_CONSUMER) {
			return new ClassVersion(className, version.getMajor(), null, null, null);
		} else {
			return new ClassVersion(className, version.getMajor(), version.getMinor(), version.getMicro(),
					version.getQualifier());
		}

	}

	public static Entry[] getSemanticClassVersion(ServiceTemplate template) {
		Class<?>[] classes = template.serviceTypes;
		List<Entry> entries = new ArrayList<>(classes.length);
		Map<String, Version> classVersionMap = getClassVersionMap(classes);
		Set<String> keySet = classVersionMap.keySet();
		for (String clazz : keySet) {
			Entry version = getSemanticClassVersion(clazz, classVersionMap.get(clazz), SEMANTIC_VERSION_CONSUMER);
			if(version != null) {
				entries.add(version);
			}
		}
		return entries.toArray(new Entry[entries.size()]);

	}

	public static Entry[] getSemanticClassVersionForProvider(Object service) {
		Class<?>[] interfaces = service.getClass().getInterfaces();
		List<Entry> entries = new ArrayList<>(interfaces.length);
		Map<String, Version> classVersionMap = getClassVersionMap(interfaces);
		Set<String> keySet = classVersionMap.keySet();
		for (String clazz : keySet) {
			Entry version = getSemanticClassVersion(clazz, classVersionMap.get(clazz), SEMANTIC_VERSION_PRVIDER);
			if (version != null) {
				entries.add(version);
			}
		}
		return entries.toArray(new Entry[entries.size()]);

	}

	public static Map<String, Version> getClassVersionMap(Class<?>[] classes) {
		Map<String, Version> versionMap = new HashMap<>();
		if (classes == null) {
			return versionMap;
		}
		for (Class<?> clazz : classes) {
			Version version = getPackageVersion(clazz);
			versionMap.put(clazz.getName(), version);
		}
		return versionMap;
	}

	public static Version getPackageVersion(Class<?> clazz) {
		Package packageName = clazz.getPackage();
		Bundle bundle = FrameworkUtil.getBundle(clazz);
		if(bundle == null){
			Bundle bundle2 = FrameworkUtil.getBundle(RiverUtils.class);
			bundle = bundle2.getBundleContext().getBundle(0);
		}
		BundleWiring bundleWiring = bundle.adapt(BundleWiring.class);
		List<BundleCapability> capabilities = bundleWiring.getCapabilities(BundleRevision.PACKAGE_NAMESPACE);
		for (BundleCapability bundleCapability : capabilities) {
			if (bundleCapability.getAttributes().get(BundleRevision.PACKAGE_NAMESPACE).equals(packageName.getName())) {
				Object object = bundleCapability.getAttributes().get("version");
				Version v = new Version(object.toString());
				return v;

			}
		}

		List<BundleRequirement> requirements = bundleWiring.getRequirements(BundleRevision.PACKAGE_NAMESPACE);
		for (BundleRequirement requrement : requirements) {
			if (requrement.getAttributes().get(BundleRevision.PACKAGE_NAMESPACE).equals(packageName.getName())) {
				Object object = requrement.getAttributes().get("version");
				Version v = new Version(object.toString());
				return v;

			}
		}

		// must be java interface or api
		return new Version(0,0,0);
	}

	public static Entry[] merge(Entry[] entries1, Entry[] entries2) {

		if (entries1 != null) {
			if (entries2 != null) {
				int len1 = entries1.length;
				int len2 = entries2.length;
				Entry[] merged = new Entry[len1 + len2];
				System.arraycopy(entries1, 0, merged, 0, len1);
				System.arraycopy(entries2, 0, merged, len1, len2);
				return merged;
			}
			return entries1;
		} else if (entries2 != null) {
			return entries2;
		}
		return null;

	}

	public static Map<String, Object> getServiceProperties(ServiceReference<?> reference) {
		Map<String, Object> serviceProperties = new HashMap<>();
		String[] propertyKeys = reference.getPropertyKeys();
		for (int i = 0; i < propertyKeys.length; i++) {
			String property = propertyKeys[i];
			Object value = reference.getProperty(property);
			serviceProperties.put(propertyKeys[i], value);
		}
		return serviceProperties;

	}
	

}
