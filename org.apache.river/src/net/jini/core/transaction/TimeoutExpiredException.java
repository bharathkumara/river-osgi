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
package net.jini.core.transaction;


/**
 * Exception thrown when a transaction timeout has expired.
 *
 * @author Sun Microsystems, Inc.
 *
 * @since 1.0
 */
public class TimeoutExpiredException extends TransactionException {
    static final long serialVersionUID = 3918773760682958000L;

    /**
     * True if the transaction committed before the timeout.
     *
     * @serial
     */
    public boolean committed;

    /**
     * Constructs an instance with no detail message.
     *
     * @param committed whether the transaction committed before the timeout.
     */
    public TimeoutExpiredException(boolean committed) {
        this.committed = committed;
    }

    /**
     * Constructs an instance with a detail message.
     *
     * @param desc the detail message
     * @param committed whether the transaction committed before the timeout.
     */
    public TimeoutExpiredException(String desc, boolean committed) {
        super(desc);
        this.committed = committed;
    }
}
