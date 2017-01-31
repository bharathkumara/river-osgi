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
package org.apache.river.reggie;

import org.apache.river.proxy.ConstrainableProxyUtil;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import net.jini.core.constraint.MethodConstraints;
import net.jini.core.constraint.RemoteMethodControl;
import net.jini.core.lease.Lease;
import net.jini.core.lease.LeaseMap;
import net.jini.core.lookup.ServiceID;
import net.jini.id.Uuid;
import net.jini.security.proxytrust.ProxyTrustIterator;
import net.jini.security.proxytrust.SingletonProxyTrustIterator;
import org.apache.river.api.io.AtomicSerial;
import org.apache.river.api.io.AtomicSerial.GetArg;

/**
 * EventLease subclass that supports constraints.
 *
 * @author Sun Microsystems, Inc.
 *
 */
@AtomicSerial
final class ConstrainableEventLease
    extends EventLease implements RemoteMethodControl
{
    private static final long serialVersionUID = 2L;

    /** Mappings between Lease and Registrar methods */
    private static final Method[] methodMappings = {
	Util.getMethod(Lease.class, "cancel", new Class[0]),
	Util.getMethod(Registrar.class, "cancelEventLease",
		       new Class[]{ long.class, Uuid.class }),

	Util.getMethod(Lease.class, "renew", new Class[]{ long.class }),
	Util.getMethod(Registrar.class, "renewEventLease",
		       new Class[]{ long.class, Uuid.class, long.class })
    };

    /**
     * Verifies that the client constraints for this proxy are consistent with
     * those set on the underlying server ref.
     */
    public static void verifyConsistentConstraints(
	MethodConstraints constraints, Object proxy) throws InvalidObjectException {
	ConstrainableProxyUtil.verifyConsistentConstraints(
	    constraints, proxy, methodMappings);
    }


    /** Client constraints for this proxy, or null */
    private final MethodConstraints constraints;

    
    private static GetArg check(GetArg arg) throws IOException{
	MethodConstraints constraints = (MethodConstraints) arg.get("constraints", null);
	EventLease el = new EventLease(arg);
	verifyConsistentConstraints(constraints, el.server);
	return arg;
    }
    
    ConstrainableEventLease(GetArg arg) throws IOException{
	super(check(arg));
	constraints = (MethodConstraints) arg.get("constraints", null);
    }

    /**
     * Creates new ConstrainableEventLease with given server reference, event
     * and lease IDs, expiration time and client constraints.
     */
    ConstrainableEventLease(Registrar server,
			    ServiceID registrarID,
			    long eventID,
			    Uuid leaseID,
			    long expiration,
			    MethodConstraints constraints,
			    boolean setConstraints)
    {
	super( setConstraints ? (Registrar) ((RemoteMethodControl) server).setConstraints(
		  ConstrainableProxyUtil.translateConstraints(
		      constraints, methodMappings)) : server,
	      registrarID,
	      eventID,
	      leaseID,
	      expiration);
	this.constraints = constraints;
    }

    /**
     * Creates a constraint-aware lease map.
     */
    public LeaseMap<? extends Lease,Long> createLeaseMap(long duration) {
	return new ConstrainableRegistrarLeaseMap(this, duration);
    }

    /**
     * Two leases can be batched if they are both RegistrarLeases, share the
     * same server, and have compatible constraints.
     */
    public boolean canBatch(Lease lease) {
	if (!(super.canBatch(lease) && lease instanceof RemoteMethodControl)) {
	    return false;
	}
	return ConstrainableProxyUtil.equivalentConstraints(
	    ((RemoteMethodControl) lease).getConstraints(),
	    ConstrainableProxyUtil.translateConstraints(
		constraints, ConstrainableRegistrarLeaseMap.methodMappings),
	    ConstrainableRegistrarLeaseMap.methodMappings);
    }

    // javadoc inherited from RemoteMethodControl.setConstraints
    public RemoteMethodControl setConstraints(MethodConstraints constraints) {
	return new ConstrainableEventLease(
	    server, registrarID, eventID, leaseID, getExpiration(), constraints, true);
    }

    // javadoc inherited from RemoteMethodControl.getConstraints
    public MethodConstraints getConstraints() {
	return constraints;
    }

    /**
     * Returns iterator used by ProxyTrustVerifier to retrieve a trust verifier
     * for this object.
     */
    private ProxyTrustIterator getProxyTrustIterator() {
	return new SingletonProxyTrustIterator(server);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
	out.defaultWriteObject();
    }

    /**
     * Verifies that the client constraints for this proxy are consistent with
     * those set on the underlying server ref.
     */
    private void readObject(ObjectInputStream in)
	throws IOException, ClassNotFoundException
    {
	in.defaultReadObject();
	verifyConsistentConstraints(constraints, server);
    }
}