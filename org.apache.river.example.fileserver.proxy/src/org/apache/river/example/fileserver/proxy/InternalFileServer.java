package org.apache.river.example.fileserver.proxy;

import java.rmi.Remote;
import java.rmi.RemoteException;

import com.healthmarketscience.rmiio.RemoteInputStream;

public interface InternalFileServer extends Remote {

	public void putFile(String fileName, RemoteInputStream inputStream) throws RemoteException;

	public RemoteInputStream getFile(String fileName) throws RemoteException;

	public String[] getFiles() throws RemoteException;

}
