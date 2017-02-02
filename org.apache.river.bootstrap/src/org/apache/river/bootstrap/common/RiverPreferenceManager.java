package org.apache.river.bootstrap.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.Bundle;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import net.jini.core.lookup.ServiceItem;

public class RiverPreferenceManager implements RiverService {

	private String riverPreferenceRoot = "org.apache.river";
	private String servicesPreferenceNode = riverPreferenceRoot + "/local/services/";
	private String consumersPreferenceNode = riverPreferenceRoot + "/network/services/";
	private Preferences preferenceScope;

	public RiverPreferenceManager(Preferences preferences) {
		this.preferenceScope = preferences;
	}

	public void saveLocalServiceConfiguration(Bundle bundle, String serviceID, Map<String, Object> config) {
		String symbolicName = bundle.getSymbolicName();
		saveLocalServiceConfiguration(symbolicName, serviceID, config);
	}

	private Preferences getPreferenceNode(String localServicePrefNode) {

		try {
			boolean nodeExists = preferenceScope.nodeExists(localServicePrefNode);
			if (!nodeExists) {
				Preferences riverServiceNode = preferenceScope.node(localServicePrefNode);
				riverServiceNode.flush();
				return riverServiceNode;
			}
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
		return null;

	}

	public void saveLocalServiceConfiguration(String symbolicName, String serviceID, Map<String, Object> config) {
		String localServicePrefNode = servicesPreferenceNode + symbolicName + "/" + serviceID;
		saveLocalServiceConfiguration(symbolicName, serviceID, config);
		Preferences localServicePref = getPreferenceNode(localServicePrefNode);
		Set<String> keySet = config.keySet();
		for (String key : keySet) {
			Object object = config.get(key);
			if (object instanceof Serializable) {
				byte[] byteArray = toByteArray(object);
				localServicePref.putByteArray(key, byteArray);
			}
		}

	}

	public void saveNetworkServiceConfig(String symbolicName, String serviceID, ServiceItem item, Object data) {

		String localServicePrefNode = symbolicName + "/" + serviceID;
//		saveLocalServiceConfiguration(symbolicName, serviceID, config);
		Preferences localServicePref = getPreferenceNode(localServicePrefNode);
	}

	public Map<String, Object> getLocalServiceConfiguration(Bundle bundle, String serviceID) {
		String symbolicName = bundle.getSymbolicName();
		return getLocalServiceConfiguration(symbolicName, serviceID);
	}

	public Map<String, Object> getLocalServiceConfiguration(String bundleName, String serviceID) {
		String localServicePrefNode = bundleName + "/" + serviceID;
		Preferences localServicePref = getPreferenceNode(localServicePrefNode);
		return null;
	}

	public <T> T getLocalServiceConfig(String bundleName, String serviceID, String key, Class<T> clazz) {
		String localServicePrefNode = bundleName + "/" + serviceID;
		Preferences localServicePref = getPreferenceNode(localServicePrefNode);
		if (localServicePrefNode != null) {
			byte[] byteArray = localServicePref.getByteArray(key, null);
			if (byteArray != null) {
				T value = toObject(byteArray, clazz);
				return value;
			}
		}

		return null;
	}

	private byte[] toByteArray(Object serviceID) {
		ObjectOutputStream oos = null;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bos);
			oos.writeObject(serviceID);
			oos.flush();
			byte[] byteArray = bos.toByteArray();
			return byteArray;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private <T> T toObject(byte[] byteArray, Class<T> clazz) {
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new ByteArrayInputStream(byteArray));
			T serviceID = (T) ois.readObject();
			return serviceID;
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (ois != null) {
				try {
					ois.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	@Override
	public void start() throws Exception {
		initialize();

	}

	private void initialize() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stop() throws Exception {
		// TODO Auto-generated method stub

	}

}
