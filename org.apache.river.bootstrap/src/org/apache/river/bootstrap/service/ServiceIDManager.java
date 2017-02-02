package org.apache.river.bootstrap.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import net.jini.core.lookup.ServiceID;

public class ServiceIDManager {

	private Preferences riverServiceNode;
	public static final String SERVICE_ID = "ServiceID";
	public static final String RIVER_SERVICE_NODE = "RiverServices";

	public ServiceIDManager(Preferences preferenceService) {
		riverServiceNode = preferenceService.node(RIVER_SERVICE_NODE);
	}

	public ServiceID getServiceID(String serviceNode) {
		try {
			boolean nodeExists = riverServiceNode.nodeExists(serviceNode);
			if (!nodeExists) {
				return null;
			}
			Preferences node = riverServiceNode.node(serviceNode);
			byte[] byteArray = node.getByteArray(SERVICE_ID, null);
			if (byteArray != null) {
				return toServiceID(byteArray);
			}
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
		return null;

	}

	public void putServiceID(String serviceNode, ServiceID uuid) {
		try {
			boolean nodeExists = riverServiceNode.nodeExists(serviceNode);
			if (!nodeExists) {
				Preferences node = riverServiceNode.node(serviceNode);
				node.flush();
			}
			Preferences node = riverServiceNode.node(serviceNode);
			byte[] byteArray = toByteArray(uuid);
			node.putByteArray(SERVICE_ID, byteArray);
			node.flush();

		} catch (BackingStoreException e) {
			e.printStackTrace();
		}

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

	private ServiceID toServiceID(byte[] byteArray) {
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new ByteArrayInputStream(byteArray));
			ServiceID serviceID = (ServiceID) ois.readObject();
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

}
