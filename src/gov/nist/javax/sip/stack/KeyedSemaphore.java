package gov.nist.javax.sip.stack;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class KeyedSemaphore {
	ConcurrentHashMap<String, Semaphore> map = new ConcurrentHashMap<String, Semaphore>();
	static StackLogger logger = CommonLogger.getLogger(KeyedSemaphore.class);
	
    public void leaveIOCriticalSection(String key) {
        Semaphore creationSemaphore = map.get(key);
        if (creationSemaphore != null) {
            creationSemaphore.release();
        }
    }
    
    public void remove(String key) {
    	if ( map.get(key) != null ) {
        	map.get(key).release();
        	map.remove(key);
        }
    }
    
    public void enterIOCriticalSection(String key) throws IOException {
        // http://dmy999.com/article/34/correct-use-of-concurrenthashmap
        Semaphore creationSemaphore = map.get(key);
        if(creationSemaphore == null) {
            Semaphore newCreationSemaphore = new Semaphore(1, true);
            creationSemaphore = map.putIfAbsent(key, newCreationSemaphore);
            if(creationSemaphore == null) {
                creationSemaphore = newCreationSemaphore;       
                if (logger.isLoggingEnabled(StackLogger.TRACE_DEBUG)) {
                    logger.logDebug("new Semaphore added for key " + key);
                }
            }
        }
        
        try {
            boolean retval = creationSemaphore.tryAcquire(10, TimeUnit.SECONDS);
            if (!retval) {
                throw new IOException("Could not acquire IO Semaphore'" + key
                        + "' after 10 seconds -- giving up ");
            }
        } catch (InterruptedException e) {
            throw new IOException("exception in acquiring sem");
        }
    }
}
