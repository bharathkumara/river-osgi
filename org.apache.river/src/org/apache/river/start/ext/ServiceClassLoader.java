package org.apache.river.start.ext;

import java.net.URL;

import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;

import net.jini.loader.pref.PreferredClassLoader;

public class ServiceClassLoader extends PreferredClassLoader {

	public ServiceClassLoader(Bundle bundle, URL[] urls,String codebase) {
		super(urls, bundle.adapt(BundleWiring.class).getClassLoader(), codebase, false);

	}

}
