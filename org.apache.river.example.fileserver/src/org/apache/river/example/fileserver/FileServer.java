package org.apache.river.example.fileserver;

import java.io.File;
import java.rmi.RemoteException;

public interface FileServer {

	public void put(File file) throws RemoteException;

	public void get(String fileName, String localFolder) throws RemoteException;

	public String[] listFiles() throws RemoteException;

}
