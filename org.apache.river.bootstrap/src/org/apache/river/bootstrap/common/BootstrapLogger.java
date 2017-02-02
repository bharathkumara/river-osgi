package org.apache.river.bootstrap.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BootstrapLogger {

	public final static Logger LOGGER = LoggerFactory.getLogger("River-Bootstrap");
	public final static Logger riverServiceExporterLogger = LoggerFactory.getLogger("River-Exporter");
	public final static Logger riverServiceAdvertiserLogger = LoggerFactory.getLogger("River-Advertiser");
	public final static Logger riverServiceDiscovererLogger = LoggerFactory.getLogger("River-Discoverer");
	public final static Logger riverLookupDiscoveryLogger = LoggerFactory.getLogger("River-Lookup");

	public static Logger getLogger() {
		return LOGGER;
	}

	public static Logger getRiverServiceExporterLogger() {
		return riverServiceExporterLogger;
	}

	public static Logger getRiverServiceAdvertiserLogger() {
		return riverServiceAdvertiserLogger;
	}

	public static Logger getRiverServiceDiscovererLogger() {
		return riverServiceDiscovererLogger;
	}

	public static Logger getRiverLookupDiscoveryLogger() {
		return riverLookupDiscoveryLogger;
	}
}
