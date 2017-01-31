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
package org.apache.river.phoenix;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.ExportException;
import java.rmi.server.ObjID;
import java.rmi.server.UnicastRemoteObject;
import net.jini.export.Exporter;

/**
 * JRMP exporter to export an object using a well-known object identifier. This
 * exporter implementation is only designed to work with Java(TM) 2 Standard
 * Edition implementations from Sun Microsystems(TM), Inc.
 *
 * @author Sun Microsystems, Inc.
 *
 * @since 2.0
 * @deprecated
 */
@Deprecated
public class SunJrmpExporter implements Exporter {

    /**
     * The object identifier.
     */
    private final int id;
    /**
     * The port.
     */
    private final int port;
    /**
     * Weak reference to the impl.
     */
    private WeakReference ref;

    private static final Method exportMethod;
    private static final Constructor unicastServerRefConstructor;
    private static final Constructor liveRefConstructor;

    static {
        try {
            Class unicastServerRefClass = Class.forName("sun.rmi.server.UnicastServerRef");
            Class liveRefClass = Class.forName("sun.rmi.transport.LiveRef");
            Class[] paramTypes
                    = new Class[]{Remote.class, Object.class, boolean.class};
            exportMethod = unicastServerRefClass.getMethod("exportObject",
                    paramTypes);
            paramTypes = new Class[]{liveRefClass};
            unicastServerRefConstructor = unicastServerRefClass.getConstructor(paramTypes);
            unicastServerRefConstructor.setAccessible(true);
            paramTypes = new Class[]{ObjID.class, int.class};
            liveRefConstructor = liveRefClass.getConstructor(paramTypes);
            liveRefConstructor.setAccessible(true);
        } catch (NoSuchMethodException nsme) {
            throw (Error) new NoSuchMethodError().initCause(nsme);
        } catch (ClassNotFoundException ex) {
            throw (Error) new NoSuchMethodError().initCause(ex);
        }
    }

    /**
     * Creates a JRMP exporter that exports using the specified well-known
     * object identifier and the specified port.
     *
     * @param id the object identifier
     * @param port the port (if zero, an anonymous port will be chosen)
     */
    public SunJrmpExporter(int id, int port) {
        this.id = id;
        this.port = port;
    }

    /**
     * @throws java.rmi.server.ExportException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @throws IllegalStateException {@inheritDoc}
     */
    @Override
    public Remote export(Remote impl) throws ExportException {
        if (ref != null) {
            throw new IllegalStateException("already used for export");
        } else if (impl == null) {
            throw new NullPointerException();
        }
        ref = new WeakReference(impl);

        try {
            Object[] args = new Object[]{new ObjID(id), port};
            Object uref = getServerRef(liveRefConstructor.newInstance(args));
            args = new Object[]{impl, null, Boolean.TRUE};
            return (Remote) exportMethod.invoke(uref, args);
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if (t instanceof ExportException) {
                throw (ExportException) t;
            } else if (t instanceof RemoteException) {
                throw new ExportException("export failed", (Exception) t);
            } else if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else if (t instanceof Error) {
                throw (Error) t;
            } else {
                throw new AssertionError(t);
            }
        } catch (IllegalAccessException e) {
            throw (Error) new IllegalAccessError().initCause(e);
        } catch (InstantiationException ex) {
            throw new AssertionError(ex);
        } catch (IllegalArgumentException ex) {
            throw new AssertionError(ex);
        }
    }

    Object getServerRef(Object lref) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Object[] args = new Object[]{lref};
        return unicastServerRefConstructor.newInstance(args);
    }

    /**
     * @throws IllegalStateException {@inheritDoc}
     */
    @Override
    public boolean unexport(boolean force) {
        if (ref == null) {
            throw new IllegalStateException(
                    "no object exported through this exporter");
        }
        Remote impl = (Remote) ref.get();
        if (impl == null) {
            return true;
        }
        try {
            return UnicastRemoteObject.unexportObject(impl, force);
        } catch (NoSuchObjectException ex) {
            return true;
        }
    }
}
