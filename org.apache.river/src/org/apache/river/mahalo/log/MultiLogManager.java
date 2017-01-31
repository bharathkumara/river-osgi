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

package org.apache.river.mahalo.log;

import org.apache.river.logging.Levels;
import org.apache.river.mahalo.TxnManager;
import org.apache.river.system.FileSystem;
import java.io.File;
import java.io.FilenameFilter;

import net.jini.admin.Administrable;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sun Microsystems, Inc.
 *
 */
public class MultiLogManager
        implements LogManager, FileModes, Administrable, MultiLogManagerAdmin {
    private static final String LOG_FILE = "Log.";

    /** Logger for persistence related messages */
    private static final Logger persistenceLogger = 
        Logger.getLogger(TxnManager.MAHALO + ".persistence");

    /** Logger for operations related messages */
    private static final Logger operationsLogger = 
        Logger.getLogger(TxnManager.MAHALO + ".operations");

    /** Logger for initialization related messages */
    private static final Logger initLogger = 
        Logger.getLogger(TxnManager.MAHALO + ".init");

    /** Client called during log recovery to process log objects */
    private final LogRecovery client;

    /** Map of log files keyed by their associated cookie */
    private final Map logByID = new HashMap();
    
    /** Lock object used for coordinating access to logByID */
    private final Object logByIDLock = new Object();
    
    /** Flag that is set to true upon destruction */
    private boolean destroyed = false;
    
    /** Persistence directory */
    private String directory = null;
    
    private final static FilenameFilter filter =
        new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (operationsLogger.isLoggable(Level.FINER)) {
                    operationsLogger.entering(FilenameFilter.class.getName(), 
		        "accept", new Object[] {dir, name});
		}
                final boolean isLog = name.startsWith(LOG_FILE);
                if (operationsLogger.isLoggable(Level.FINER)) {
                    operationsLogger.exiting(FilenameFilter.class.getName(), 
		        "accept", Boolean.valueOf(isLog));
		}
                return isLog;
            };
        };

    /**
     * Callback interface for log files to remove themselves from 
     * this manager 
     */ 
    public static interface LogRemovalManager {
        public void release(long cookie);
    }
    
    /**
     * Capability object passed to log files, which is called back upon
     * log removal. 
     */ 
    final LogRemovalManager logMgrRef = new LogRemovalManager() {
        public void release(long cookie) {
	    MultiLogManager.this.release(cookie);
	}
    };
    
    /**
     * Create a non-persistent <code>MultiLogManager</code>.
     */
    public MultiLogManager() {
        directory = null; // just for insurance
        client = null; 
    }
	    
    /**
     * Create a <code>MultiLogManager</code>.
     *
     * @param client who to inform during recovery.
     *
     * @param path where to store logging information.
     */
    public MultiLogManager(LogRecovery client, String path) {
	if (path == null)
	    throw new IllegalArgumentException("MultiLogManager: must use " +
						"non-null path");
	if (client == null)
	    throw new IllegalArgumentException("MultiLogManager: must use " +
						"non-null recovery client");
        this.client = client;
	directory = path; 

	if (!directory.endsWith(File.separator))
	    directory = directory.concat(File.separator);

	if (persistenceLogger.isLoggable(Level.FINEST)) {
            persistenceLogger.log(Level.FINEST,
	    "directory = {0}", directory);
	}

        File tmpfile = new File(directory);
 
        //If you attempt to access the Version file and
        //it does not exist, start with zero.
         try {
            if (!tmpfile.exists())
                if (!tmpfile.mkdirs())
 	            if (persistenceLogger.isLoggable(Level.SEVERE)) {
                        persistenceLogger.log(Level.SEVERE,
		            "Could not create {0}", tmpfile);
		    }
//TODO - ignore???		    
        } catch (SecurityException se) {
            if (persistenceLogger.isLoggable(Level.SEVERE)) {
                persistenceLogger.log(Level.SEVERE,
	        "Error accessing Version File", se);
	    }
//TODO ignore? throw (SecurityException)se.fillInStackTrace();
        }
     }

    // javadoc inherited from supertype
    public ClientLog logFor(long cookie) throws LogException {
        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.entering(MultiLogManager.class.getName(), 
	        "logFor", Long.valueOf(cookie));
	}
	ClientLog cl = null;
	Long key = Long.valueOf(cookie);
        Object prev = null;
	
        synchronized(logByIDLock) {
	    if (destroyed)
	        throw new LogException("Manger has been destroyed");
		
	    cl = (ClientLog)logByID.get(key); 
	    if (cl == null) {
	        cl = (directory==null)?
		    (ClientLog)new TransientLogFile(cookie, logMgrRef):
		    (ClientLog)new SimpleLogFile(
		        directory + LOG_FILE + cookie, 
		        cookie, logMgrRef);
	        if (persistenceLogger.isLoggable(Level.FINEST)) {
                    persistenceLogger.log(Level.FINEST,
		    "Created ClientLog: {0}", 
		    directory + LOG_FILE + cookie);
		}
 	        prev = logByID.put(key, cl);
	    }
            if (persistenceLogger.isLoggable(Level.FINEST)) {
                persistenceLogger.log(Level.FINEST,
                "Currently managing {0} logs.", 
                Integer.valueOf(logByID.size()));
            }

	} 
        if (prev != null)
	    throw new LogException("Previous mapping for cookie(" 
	        + cookie + ") -- internal table corrupt?");
	     
	if (persistenceLogger.isLoggable(Level.FINEST)) {
            persistenceLogger.log(Level.FINEST,
            "Using ClientLog {0} for cookie {1}",
	    new Object[] {cl, Long.valueOf(cookie)});
	}
	
        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.exiting(MultiLogManager.class.getName(), 
	        "logFor", cl);
	}
	return cl;
    }

    // javadoc inherited from supertype
    private void release(long cookie) {
        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.entering(MultiLogManager.class.getName(), 
	        "release", Long.valueOf(cookie));
	}
	Object prev = null;
        synchronized(logByIDLock) {
	    if (destroyed)
	        return;
            if (persistenceLogger.isLoggable(Level.FINEST)) {
                persistenceLogger.log(Level.FINEST,
                "Releasing ClientLog for cookie {0}",
                Long.valueOf(cookie));
            }
	    prev = logByID.remove(Long.valueOf(cookie));
            if (persistenceLogger.isLoggable(Level.FINEST)) {
                persistenceLogger.log(Level.FINEST,
                "Currently managing {0} logs.", 
                Integer.valueOf(logByID.size()));
            }
	}
        
	if (persistenceLogger.isLoggable(Level.FINEST)) {
	    if (prev == null) {
    	        persistenceLogger.log(Level.FINEST,
		"Note: ClientLog already removed");
            }
        }

        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.exiting(MultiLogManager.class.getName(), 
	        "release");
	}
    }
        
    /**
     *  Consumes the log file and re-constructs a system's
     *  state.
     */
    public void recover() throws LogException {
        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.entering(MultiLogManager.class.getName(), 
	        "recover");
	}
        // Short-circuit for non-persistent mode
	if (directory == null) return;
	
	Log log = null;
	File tmpfile = null;
	String [] filenames = null;

	/* Called by initialization thread only, 
	 * so don't need to check for destroyed.
	 */

	try {
	    tmpfile = new File(directory);
	    filenames = tmpfile.list(filter);

	    if (filenames == null || filenames.length == 0)
	      return;

            String logName;
	    for (int i = 0; i < filenames.length; i++ ) {
	        logName = directory +  filenames[i];
	        log = new SimpleLogFile(logName, logMgrRef);
		if (persistenceLogger.isLoggable(Level.FINEST)) {
                    persistenceLogger.log(Level.FINEST,
		    "Recovering log: {0}", logName);
		}
		try {
		    log.recover(client);
                
		    /* Called by initialization thread only, 
		     * so doesn't need to be synchronized here.
		     */
		    logByID.put(Long.valueOf(log.cookie()),log);
	        } catch (LogException le) {
	            if(persistenceLogger.isLoggable(Level.WARNING)) {
		        persistenceLogger.log(Level.WARNING,
			"Unable to recover log state", le);
		    }
		}
	    }
	} catch (SecurityException se) { // TODO - shouldn't this percolate back up?
	    if(persistenceLogger.isLoggable(Level.WARNING)) {
		persistenceLogger.log(Level.WARNING,
		"Unable to recover log state", se);
	    }
	}
        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.exiting(MultiLogManager.class.getName(), 
	        "recover");
	}
    }


    /**
     * Retrieves the administration interface for the
     * <code>MultiLogManager</code>
     *
     */
    public Object getAdmin() {
        // TBD - pass capability object instead?
        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.entering(MultiLogManager.class.getName(), 
	        "getAdmin");
	}
        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.exiting(MultiLogManager.class.getName(), 
	        "getAdmin", this);
	}
	return (MultiLogManagerAdmin)this;
    }


    /**
     * Clean up all <code>LogFile</code> objects on behalf of caller.
     *
     * @see org.apache.river.admin.DestroyAdmin
     * @see org.apache.river.system.FileSystem
     */
    public void destroy() {
        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.entering(MultiLogManager.class.getName(), 
	        "destroy");
	}

	// TBD - Set destroy flag? used by logFor()/release()
	// TBD - Loop over values enum?
	/**
	 * Loop over know logs and invalidate them.
	 */
        synchronized(logByIDLock) {
  	    if (destroyed) // return silently to avoids retries
	        return;
 	    if (logByID.size() > 0) {
	        /* Can't use iterator because slf.invalidate() calls back into
		 * this.release() in order to remove itself from logByID,
		 * which would cause a concurrent modification exception.
		 */
	        Object [] vals = logByID.values().toArray();
		Log slf = null;
	        for (int i=0; i<vals.length; i++) {
		    try {
		        slf = (Log)vals[i];
			if (slf != null) 
			    slf.invalidate();
			else {
  		            if (persistenceLogger.isLoggable(Level.FINEST)) {
                                persistenceLogger.log(Level.FINEST,
				"Observed a null log file entry at: {0}", i);
			    }
			}
                    } catch (LogException le) {
                        if(persistenceLogger.isLoggable(Levels.HANDLED)) {
		            persistenceLogger.log(Levels.HANDLED,
			        "Unable to recover log state", le);
			}
                    } catch (java.util.NoSuchElementException nsee) {
                        if(persistenceLogger.isLoggable(Levels.HANDLED)) {
		            persistenceLogger.log(Levels.HANDLED,
			        "Problem enumerating internal log state", nsee);
			}
     	            }
	        }
	        logByID.clear();
		destroyed = true;
	    }
	} 
        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.exiting(MultiLogManager.class.getName(), 
	        "destroy");
	}
    }
}
