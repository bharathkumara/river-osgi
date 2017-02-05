package net.jini.loader.osgi;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;

public class OSGiBundleTracker extends BundleTracker<Object> {

	public OSGiBundleTracker(BundleContext context, int stateMask, BundleTrackerCustomizer<Object> customizer) {
		super(context, stateMask, customizer);
	}

	@Override
	public Object addingBundle(Bundle bundle, BundleEvent event) {
		// TODO Auto-generated method stub
		return super.addingBundle(bundle, event);
	}

	@Override
	public void modifiedBundle(Bundle bundle, BundleEvent event, Object object) {
		// TODO Auto-generated method stub
		super.modifiedBundle(bundle, event, object);
	}

	@Override
	public void removedBundle(Bundle bundle, BundleEvent event, Object object) {
		// TODO Auto-generated method stub
		super.removedBundle(bundle, event, object);
	}

	
	
}
