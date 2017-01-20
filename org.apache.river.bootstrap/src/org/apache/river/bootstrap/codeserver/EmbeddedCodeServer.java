package org.apache.river.bootstrap.codeserver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.river.bootstrap.common.RiverCodeServer;
import org.apache.river.tool.ClassServer;

public class EmbeddedCodeServer implements RiverCodeServer {

	private String directory;
	private ClassServer classServer;
	private String baseLocation;

	public EmbeddedCodeServer() {
		String property = System.getProperty("org.apache.river.lookup.codeserver.location");
		if (property == null) {
			property = System.getProperty("user.home") + File.separatorChar + "org.apache.river.lookup.codeserver";
		}
		File f = new File(property);
		if (!f.exists()) {
			f.mkdirs();
		}
		directory = f.getAbsolutePath();
		try {
			classServer = new ClassServer(0, directory, true, true, true);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public String addJars(String id, InputStream stream, String targetFileName) throws IOException {


		File f = new File(directory, id);
		if (!f.exists()) {
			f.mkdirs();
		}
		Path copy = Paths.get(f.getAbsolutePath(), targetFileName);
		Files.copy(stream, copy, StandardCopyOption.REPLACE_EXISTING);
		return baseLocation + id + "/" + targetFileName;

	
	}

	@Override
	public void start() throws Exception {
		classServer.start();
		constructBaseLocation();
	}

	@Override
	public void stop() throws Exception {
		classServer.terminate();
	}

	@Override
	public int getPort() {
		return classServer.getPort();
	}

	@Override
	public String getDirectory() {
		return directory;
	}

	@Override
	public String getBaseLocation() {
		return baseLocation;
	}
	private void constructBaseLocation() {

		InetAddress localHost;
		try {
			localHost = InetAddress.getLocalHost();
			String hostName = localHost.getHostName();
			if (hostName == null || hostName.trim().equals("")) {
				hostName = localHost.getHostAddress();
			}
			StringBuilder builder = new StringBuilder();
			builder.append("http://");
			builder.append(hostName);
			builder.append(":");
			builder.append(classServer.getPort());
			builder.append("/");
			baseLocation = builder.toString();

		} catch (UnknownHostException e) {
			e.printStackTrace();
			baseLocation = null;
		}

	}

}
