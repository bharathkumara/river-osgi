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

import org.apache.river.discovery.Discovery;
import org.apache.river.discovery.DiscoveryConstraints;
import org.apache.river.discovery.UnicastResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import net.jini.core.constraint.InvocationConstraints;

/**
 * Utility class used by implementations which want to perform unicast
 * discovery on possibly multiple IP addresses for a given host name.
 * This class supports unicast discovery constraints as specified in
 * DiscoveryConstraints.
 */
public abstract class MultiIPDiscovery {
    // Default value for unicast socket timeout
    public final static int DEFAULT_TIMEOUT = 60 * 1000;

    public UnicastResponse getResponse(String host,
				       int port,
				       InvocationConstraints constraints)
	throws IOException, ClassNotFoundException
    {
	InetAddress addrs[] = null;
	try {
	    addrs = InetAddress.getAllByName(host);
	} catch (UnknownHostException uhe) {
	    // Name resolution failed.
	    // We'll just try to use the host name later anyway.
	}
	
	DiscoveryConstraints dc = DiscoveryConstraints.process(constraints);
	int pv = dc.chooseProtocolVersion();
	Discovery disco;
	switch (pv) {
	    case Discovery.PROTOCOL_VERSION_1:
		disco = Discovery.getProtocol1();
		break;
	    case Discovery.PROTOCOL_VERSION_2:
		disco = Discovery.getProtocol2(null);
		break;
	    default:
		throw new AssertionError(pv);
	}

	long deadline = dc.getConnectionDeadline(Long.MAX_VALUE);
	long connectionTimeout = getTimeout(deadline);

	if (addrs == null) {
	    return getSingleResponse(host, connectionTimeout, port, dc, disco);
	}
	
	IOException ioEx = null;
	SecurityException secEx = null;
	ClassNotFoundException cnfEx = null;
	for (int i = 0; i < addrs.length; i++) {
	    try {
		return getSingleResponse(addrs[i].getHostAddress(),
				     connectionTimeout, port, dc, disco);
	    } catch (ClassNotFoundException ex) {
		cnfEx = ex;
		singleResponseException(ex, addrs[i], port);
	    } catch (IOException ex) {
		ioEx = ex;
		singleResponseException(ex, addrs[i], port);
	    } catch (SecurityException ex) {
		secEx = ex;
		singleResponseException(ex, addrs[i], port);
	    }
	    
	    try {
		connectionTimeout = getTimeout(deadline);
	    } catch (SocketTimeoutException ex) {
		if (ioEx == null) {
		    ioEx = ex;
		}
		// Out of time.
		break;
	    }
	}
	if (cnfEx != null) {
	    throw cnfEx;
	}
	if (ioEx != null) {
	    throw ioEx;
	}
	assert (secEx != null);
	throw secEx;
    }
    
    private long getTimeout(long deadline) throws SocketTimeoutException {
	long now = System.currentTimeMillis();
	if (now >= deadline) {
	    throw new SocketTimeoutException("timeout expired before"
					     + " connection attempted");
	}
	return deadline - now;
    }
    
    private UnicastResponse getSingleResponse(String host,
					      long connectionTimeout,
					      int port,
					      DiscoveryConstraints dc,
					      Discovery disco)
	throws IOException, ClassNotFoundException
    {
	Socket s = new Socket();
        boolean discoveryAttempted = false;
        try {
            if (connectionTimeout > Integer.MAX_VALUE) {
                s.connect(new InetSocketAddress(host, port));
            } else {
                s.connect(new InetSocketAddress(host, port), 
                          (int) connectionTimeout);
            }
            try {
                s.setTcpNoDelay(true);
            } catch (SocketException e) {
                // ignore possible failures and proceed anyway
            }
            try {
                s.setKeepAlive(true);
            } catch (SocketException e) {
                // ignore possible failures and proceed anyway
            }
            s.setSoTimeout(dc.getUnicastSocketTimeout(
                            getDefaultUnicastSocketTimeout()));
            discoveryAttempted = true;
            return performDiscovery(disco, dc, s);
        } finally {
            try {
                s.close();
            } catch (IOException e) {
                if (discoveryAttempted) {
                    socketCloseException(e);
                }
            }
        }
    }
    
    /*
     * Subclasses may override this method to supply their own default
     * timeout. This class implements this method to return a value of
     * DEFAULT_TIMEOUT.
     */
    protected int getDefaultUnicastSocketTimeout() {
	return DEFAULT_TIMEOUT;
    }
    
    /*
     * Called when doing unicast discovery on a single IP results in a
     * ClassNotFoundException, IOException or SecurityException. The subclass
     * may perform any action it pleases, like logging. This class implements
     * this method to by default do nothing.
     */
    protected void singleResponseException(Exception ex,
					   InetAddress addr,
					   int port)
    { // do nothing
    }
    
    /*
     * Called when close of a socket on which discovery has been attempted
     * fails. This class implements this method to do nothing by default.
     */
    protected void socketCloseException(IOException ex) {}
    
    /*
     * Called to actually perform the discovery operation. All other protected
     * methods have default implementations, this one must be implemented.
     */
    protected abstract UnicastResponse performDiscovery(Discovery disco,
							DiscoveryConstraints dc,
							Socket s)
	throws IOException, ClassNotFoundException;
	    
}
