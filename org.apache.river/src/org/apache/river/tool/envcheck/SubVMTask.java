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
package org.apache.river.tool.envcheck;

/** 
 * A subtask to be run in a separate VM. Classes implementing this
 * interface must provide a public no-arg constructor. 
 */
public interface SubVMTask {

    /**
     * Run the subtask. Return an object representing the
     * result of the task. If an unexpected exception is thrown
     * in the body of the method, it should be caught and used
     * as the returned object.
     *
     * @param args
     * @return the task status object, which may be <code>null</code>
     */
    public Object run(String[] args);
}
