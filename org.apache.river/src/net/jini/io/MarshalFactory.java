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

package net.jini.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

/**
 * Allows for alternative serialization implementations of MarshalledInstance.
 * 
 * @author peter
 */
public interface MarshalFactory {
    
    /**
     * 
     * @param objIn InputStream to write serialized objects to.
     * @param locIn InputStream to write codebase annotations to, optional.
     * @param defaultLoader
     * @param verifyCodebaseIntegrity
     * @param verifierLoader
     * @param context
     * @return 
     * @throws java.io.IOException 
     */
    MarshalInstanceInput createMarshalInput(InputStream objIn, 
	    InputStream locIn,
	    ClassLoader defaultLoader,
	    boolean verifyCodebaseIntegrity,
	    ClassLoader verifierLoader,
	    Collection context) throws IOException;
    
    /**
     * 
     * @param objOut
     * @param locOut
     * @param context
     * @return 
     * @throws java.io.IOException 
     */
    MarshalInstanceOutput createMarshalOutput(OutputStream objOut, OutputStream locOut, Collection context) throws IOException;
    
}
