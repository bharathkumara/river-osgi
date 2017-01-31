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
package org.apache.river.fiddler;

import org.apache.river.start.LifeCycle;

import net.jini.config.ConfigurationException;

import javax.security.auth.login.LoginException;

import java.io.IOException;


/**
 * Convenience class intended for use with the
 * {@link org.apache.river.start.ServiceStarter} framework to start
 * a <i>transient</i> (non-activatable, non-persistent) implementation
 * of Fiddler.
 *
 * @author Sun Microsystems, Inc.
 * @since 2.0
 */
class TransientFiddlerImpl extends FiddlerImpl {

    /**
     * Constructs a new instance of <code>FiddlerImpl</code> that is not
     * activatable, and which will not persist its state.
     *
     * @param configArgs <code>String</code> array whose elements are
     *                   the arguments to use when creating the server.
     * @param lifeCycle  instance of <code>LifeCycle</code> that, if 
     *                   non-<code>null</code>, will cause this object's
     *                   <code>unregister</code> method to be invoked during
     *                   shutdown to notify the service starter framework that
     *                   the reference to this service's implementation can be
     *                   'released' for garbage collection. A value of 
     *                   <code>null</code> for this argument is allowed.
     *
     * @throws IOException            this exception can occur when there is
     *                                a problem exporting the server that's
     *                                being created.
     * @throws ConfigurationException this exception can occur when an
     *                                problem occurs while retrieving an item
     *                                from the <code>Configuration</code>
     *                                generated from the contents of the
     *                                given <code>configArgs</code> parameter
     * @throws LoginException         this exception occurs when authentication
     *                                fails while performing a JAAS login for
     *                                this service
     */
    TransientFiddlerImpl(String[] configArgs, LifeCycle lifeCycle)
                                             throws IOException,
                                                    ConfigurationException,
                                                    LoginException
    {
        super(configArgs, lifeCycle, false);//false ==> not persistent
    }//end constructor

}//end class TransientFiddlerImpl

