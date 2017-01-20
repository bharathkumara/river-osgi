package org.apache.river.bootstrap.common;

public interface Constants {

	public static final String REMOTE_SERVICE_EXPORT_INTERFACES_KEY = "service.exported.interfaces";

	public static final String REMOTE_SERVICE_EXPORT_INTERFACES_WILECARD_VALUE = "*";

	public static final String REMOTE_SERVICE_EXPORT_CONFIG_KEY = "service.exported.configs";

	public static final String REMOTE_SERVICE_EXPORT_CONFIG_VALUE = "org.apache.river.jeri";

	public static final String REMOTE_SERVICE_JERI_EXPORTER_KEY = "org.apache.river.exporter";

	public static final String REMOTE_SERVICE_JERI_EXPORTER_TCP = "org.apache.river.jeri.tcp";

	public static final String REMOTE_SERVICE_JERI_EXPORTER_SSL = "org.apache.river.jeri.ssl";

	public static final Object REMOTE_SERVICE_JERI_EXPORTER_HTTP = "org.apache.river.jeri.http";

	public static final Object REMOTE_SERVICE_JERI_EXPORTER_HTTPS = "org.apache.river.jeri.https";
	
	public static final String REMOTE_SERVICE_JERI_NO_EXPORTER = "org.apache.river.noexporter";

	public static final String REMOTE_SERVICE_JERI_EXPORTER_PORT_KEY = "org.apache.river.jeri.tcp.port";

	public static final String INTERNAL_PROP_SERVICE_ID_LISTENER_KEY = ".ServiceIDListener";

	public static final String INTERNAL_PROP_SERVICE_DISCOVERY_LISTENER_KEY = ".ServiceDiscoveryListener";

	public static final String INTERNAL_PROP_SERVICE_ITEM_FILTER_KEY = ".ServiceItemFilter";

	public static final String INTERNAL_PROP_SERVICE_EXPORTER = ".Exporter";

	public static final String SERVICE_ID_KEY = "ServiceID";
	
}
