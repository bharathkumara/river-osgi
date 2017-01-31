/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.river.discovery.internal;

import net.jini.jeri.ssl.HttpsEndpoint;

/**
 * Provides a rendezvous point for the net.jini.jeri.https transport provider to
 * register an EndpointInternals instance used by provider classes for the
 * net.jini.discovery.https unicast discovery format.
 */
public class HttpsEndpointInternalsAccess {

    private static EndpointInternals endpointInternals = null;
    private static final Object lock = new Object();

    static {
	HttpsEndpoint.getInstance("triggerStaticInitializer", 1);
    }

    private HttpsEndpointInternalsAccess() {
    }

    /**
     * Registers EndpointInternals instance to use for back-door operations on
     * SslEndpoint and SslServerEndpoints.  This method should be called only
     * once, from within the static initializer of the SslEndpoint class.  If a
     * security manager is installed, this method checks that the calling
     * context has EndpointInternalsPermission.  Throws IllegalStateException
     * if EndpointInternals instance has already been set.
     */
    public static void set(EndpointInternals endpointInternals) {
	if (endpointInternals == null) {
	    throw new NullPointerException();
	}
	SecurityManager sm = System.getSecurityManager();
	if (sm != null) {
	    sm.checkPermission(new EndpointInternalsPermission(
				    EndpointInternalsPermission.SET));
	}
	synchronized (lock) {
	    if (HttpsEndpointInternalsAccess.endpointInternals != null) {
		throw new IllegalStateException(
		    "endpointInternals already set");
	    }
	    HttpsEndpointInternalsAccess.endpointInternals = endpointInternals;
	}
    }

    /**
     * Returns registered EndpointInternals instance.  Throws
     * IllegalStateException if EndpointInternals instance has not been set.
     */
    public static EndpointInternals get() {
	SecurityManager sm = System.getSecurityManager();
	if (sm != null) {
	    sm.checkPermission(new EndpointInternalsPermission(
				    EndpointInternalsPermission.GET));
	}
	synchronized (lock) {
	    if (endpointInternals == null) {
		throw new IllegalStateException("endpointInternals not set");
	    }
	    return endpointInternals;
	}
    }
}
