/*
 * Conditions Of Use
 *
 * This software was developed by employees of the National Institute of
 * Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 15 Untied States Code Section 105, works of NIST
 * employees are not subject to copyright protection in the United States
 * and are considered to be in the public domain.  As a result, a formal
 * license is not needed to use the software.
 *
 * This software is provided by NIST as a service and is expressly
 * provided "AS IS."  NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
 * OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
 * AND DATA ACCURACY.  NIST does not warrant or make any representations
 * regarding the use of the software or the results thereof, including but
 * not limited to the correctness, accuracy, reliability or usefulness of
 * the software.
 *
 * Permission to use this software is contingent upon your acceptance
 * of the terms of this agreement
 *
 * .
 *
 */
package gov.nist.javax.sip.stack;

import gov.nist.core.CommonLogger;
import gov.nist.core.LogLevels;
import gov.nist.core.StackLogger;

import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

public class BlockingQueueDispatchAuditor extends TimerTask {
  private Timer timer = null;
  private static int timerThreadCount = 0;
  private static StackLogger logger = CommonLogger.getLogger(BlockingQueueDispatchAuditor.class);
    private long totalReject = 0;
    private boolean started = false;
    private Queue<? extends Runnable> queue;
    private int timeout = 8000;
    public BlockingQueueDispatchAuditor(Queue<? extends Runnable> queue) {
    	this.queue = queue;
    }

    public void start(int interval) {
    	if(started) stop();
    	started = true;
    	timer = new Timer("BlockingQueueDispatchAuditor-Timer-" + timerThreadCount++, true);
    	timer.scheduleAtFixedRate(this, interval, interval);
    }

    public int getTimeout() {
    	return timeout;
    }

    public void setTimeout(int timeout) {
    	this.timeout = timeout;
    }

    public void stop() {
    	try {
    		timer.cancel();
    		timer = null;
    	} catch (Exception e) {
    		//not important
    	} finally {
    		started = false;
    	}
    }

    public void run() {
    	try {
    		int removed = 0;
    		synchronized(this.queue) {
    			QueuedMessageDispatchBase runnable =(QueuedMessageDispatchBase) this.queue.peek();
    			while(runnable != null) {
    				QueuedMessageDispatchBase d = (QueuedMessageDispatchBase) runnable;
    				if(System.currentTimeMillis() - d.getReceptionTime() > timeout) {
    					queue.poll();
    					runnable = (QueuedMessageDispatchBase) this.queue.peek();
    					removed ++;
    				} else {
    					runnable = null;
    				}
    			}
    		}
    		if(removed>0) {
    			totalReject+=removed;
    			if(logger != null && logger.isLoggingEnabled(LogLevels.TRACE_WARN))
    				logger.logWarning("Removed stuck messages=" + removed +
    						" total rejected=" + totalReject + " stil in queue=" + this.queue.size());
    		}

    	} catch (Exception e) {
    		if(logger != null && logger.isLoggingEnabled(LogLevels.TRACE_WARN)) {
				logger.logWarning("Problem reaping old requests. This is not a fatal error." + e);
			}
		}
	}
}