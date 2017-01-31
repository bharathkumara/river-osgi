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
package net.jini.lease;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.rmi.MarshalledObject;
import net.jini.core.event.RemoteEvent;
import net.jini.core.lease.Lease;
import org.apache.river.api.io.AtomicSerial;
import org.apache.river.api.io.AtomicSerial.GetArg;

/**
 * Event generated by a lease renewal set when its lease is about to
 * expire.
 *
 * @author Sun Microsystems, Inc.
 * @see LeaseRenewalSet 
 */
@AtomicSerial
public class ExpirationWarningEvent extends RemoteEvent {
    private static final long serialVersionUID = -2020487536756927350L;

    private static GetArg check(GetArg arg) throws IOException {
	RemoteEvent sup = new RemoteEvent(arg);
	if (sup.getID() != LeaseRenewalSet.EXPIRATION_WARNING_EVENT_ID)
	    throw new InvalidObjectException("Illegal object state");
	return arg;
    }
    
    public ExpirationWarningEvent(GetArg arg) throws IOException {
	super(check(arg));
    }

    /**
     * Simple constructor. Note event id is fixed to
     * <code>LeaseRenewalSet.EXPIRATION_WARNING_EVENT_ID</code>.
     *
     * @param source the <code>LeaseRenewalSet</code> that generated the
     *	      event
     * @param seqNum the sequence number of this event
     * @param handback the <code>MarshalledObject</code> passed in as
     *	      part of the event registration
     */
    public ExpirationWarningEvent(LeaseRenewalSet  source, 
                                  long             seqNum, 
                                  MarshalledObject handback)
    {
	super(source, LeaseRenewalSet.EXPIRATION_WARNING_EVENT_ID, seqNum,
	      handback);
    }

    /**
     * Convenience method to retrieve the <code>Lease</code> associated
     * with the source of this event. This is the <code>Lease</code>
     * which is about to expire.
     * <p>
     * The <code>Lease</code> object returned will be equivalent (in the
     * sense of <code>equals</code>) to other <code>Lease</code> objects
     * associated with the set, but may not be the same object. One
     * notable consequence of having two different objects is that the
     * <code>getExpiration</code> method of the <code>Lease</code>
     * object returned by this method may return a different time than
     * the <code>getExpiration</code> methods of other
     * <code>Lease</code> objects granted on the same set.
     * <p>
     * The expiration time associated with the <code>Lease</code> object
     * returned by this method will reflect the expiration the lease had
     * when the event occurred. Renewal calls may have changed the
     * expiration time of the underlying lease between the time when the
     * event was generated and when it was delivered.
     *
     * @return the lease associated with the source of this event
     */
    public Lease getRenewalSetLease() {
	return ((LeaseRenewalSet) getSource()).getRenewalSetLease();
    }
}
