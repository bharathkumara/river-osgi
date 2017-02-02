package org.apache.river.example.fileserver.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.rmi.RemoteException;
import java.rmi.server.ExportException;

import org.apache.river.admin.DestroyAdmin;
import org.apache.river.example.fileserver.proxy.FileServerProxy;
import org.apache.river.example.fileserver.proxy.InternalFileServer;
import org.apache.river.start.LifeCycle;

import com.healthmarketscience.rmiio.GZIPRemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;
import com.healthmarketscience.rmiio.RemoteInputStreamServer;

import net.jini.config.Configuration;
import net.jini.export.Exporter;
import net.jini.export.ProxyAccessor;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.tcp.TcpServerEndpoint;

public class InternalFileServerImpl implements InternalFileServer, ProxyAccessor, DestroyAdmin {

	private String folderLocation;
	private File folder;
	private FileServerProxy proxy;
	private Exporter exporter;

	public InternalFileServerImpl(Configuration configuration, LifeCycle lifeCycle) throws Exception {
		// configuration.getEntry("", name, type, defaultValue)
		this.folderLocation = System.getProperty("user.home");
		folder = new File(folderLocation);

	}

	@Override
	public void putFile(String fileName, RemoteInputStream inputStream) throws RemoteException {

		File f = new File(folderLocation, fileName);
		try {
			InputStream istream = RemoteInputStreamClient.wrap(inputStream);
			Files.copy(istream, f.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public RemoteInputStream getFile(String fileName) throws RemoteException {

		RemoteInputStreamServer istream = null;
		try {
			istream = new GZIPRemoteInputStream(new BufferedInputStream(new FileInputStream(fileName)));
			RemoteInputStream result = istream.export();
			istream = null;
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// we will only close the stream here if the server fails before
			// returning an exported stream
			if (istream != null)
				istream.close();
		}
		return null;
	}

	@Override
	public String[] getFiles() throws RemoteException {
		return folder.list();
	}

	@Override
	public Object getProxy() {
		if (proxy != null) {
			return proxy;
		}

		TcpServerEndpoint endpoint = TcpServerEndpoint.getInstance(0);
		exporter = new BasicJeriExporter(endpoint, new BasicILFactory());
		try {
			InternalFileServer export = (InternalFileServer) exporter.export(this);
			proxy = new FileServerProxy(export);
			return proxy;
		} catch (ExportException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void destroy() throws RemoteException {
		exporter.unexport(true);

	}

}
