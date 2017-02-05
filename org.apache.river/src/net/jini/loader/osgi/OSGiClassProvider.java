package net.jini.loader.osgi;

import java.net.MalformedURLException;
import java.rmi.server.RMIClassLoaderSpi;

import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

public class OSGiClassProvider extends RMIClassLoaderSpi  {

	@Override
	public Class<?> loadClass(String codebase, String name, ClassLoader defaultLoader)
			throws MalformedURLException, ClassNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<?> loadProxyClass(String codebase, String[] interfaces, ClassLoader defaultLoader)
			throws MalformedURLException, ClassNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClassLoader getClassLoader(String codebase) throws MalformedURLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getClassAnnotation(Class<?> cl) {
		// TODO Auto-generated method stub
		return null;
	}

	
}
