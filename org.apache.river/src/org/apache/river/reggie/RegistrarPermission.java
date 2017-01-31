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

import net.jini.jeri.BasicInvocationDispatcher;
import net.jini.jeri.BasicJeriExporter;
import net.jini.security.AccessPermission;

/**
 * Permission that can be used to express the access control policy for an
 * instance of the Reggie lookup service exported with a {@link
 * BasicJeriExporter}.  This class can be specified to {@link
 * BasicInvocationDispatcher}, which will then perform permission checks for
 * incoming remote calls using <code>RegistrarPermission</code> instances.
 * <p>
 * An instance contains a name (also referred to as a "target name") but no
 * actions list; you either have the named permission or you don't.  The
 * convention is that the target name is the fully qualified name of the remote
 * method being invoked.  Wildcard matches are supported using the syntax
 * specified by {@link AccessPermission}.
 * <p>
 * The possible target names for use with a Reggie server are specified in the
 * package documentation for {@link org.apache.river.reggie}.
 *
 * @author Sun Microsystems, Inc.
 * @since 2.0
 */
public class RegistrarPermission extends AccessPermission {

    private static final long serialVersionUID = 2L;

    /**
     * Creates an instance with the specified target name.
     *
     * @param name the target name
     * @throws NullPointerException if the target name is <code>null</code>
     * @throws IllegalArgumentException if the target name does not match
     * the syntax specified in the comments at the beginning of the {@link
     * AccessPermission} class
     */
    public RegistrarPermission(String name) {
	super(name);
    }
}
