package org.apache.river.example.fileserver.proxy;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.rmi.RemoteException;

import org.apache.river.example.fileserver.FileServer;

import com.healthmarketscience.rmiio.GZIPRemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;
import com.healthmarketscience.rmiio.RemoteInputStreamServer;

public class FileServerProxy implements FileServer, Serializable {

	private InternalFileServer server;

	public FileServerProxy(InternalFileServer server) {
		this.server = server;
	}

	@Override
	public void put(File file) throws RemoteException {
		RemoteInputStreamServer istream = null;
		try {
			istream = new GZIPRemoteInputStream(new BufferedInputStream(new FileInputStream(file)));
			server.putFile(file.getName(), istream.export());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (istream != null)
				istream.close();
		}

	}

	@Override
	public String[] listFiles() throws RemoteException {
		return server.getFiles();
	}

	@Override
	public void get(String fileName, String localFolder) throws RemoteException {
		RemoteInputStream remoteInputStream = server.getFile(fileName);
		try {
			InputStream istream = RemoteInputStreamClient.wrap(remoteInputStream);
			File f = new File(localFolder, fileName);
			Files.copy(istream, f.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
