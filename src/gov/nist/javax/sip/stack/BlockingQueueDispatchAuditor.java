package gov.nist.javax.sip.stack;

import gov.nist.core.LogLevels;
import gov.nist.core.StackLogger;

import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

public class BlockingQueueDispatchAuditor extends TimerTask {
	private Timer timer = new Timer();
	private StackLogger logger;
	private long totalReject = 0;     
	private boolean started = false;
	private Queue queue;
	private int timeout = 8000;
	
    public BlockingQueueDispatchAuditor(Queue queue) {
    	this.queue = queue;
    }
    
    public void setLogger(StackLogger logger) {
    	this.logger = logger;
    }
    
    public int getTimeout() {
    	return timeout;
    }
    
    public void setTimeout(int timeout) {
    	this.timeout = timeout;
    }
    
    public void start(int interval) {
    	if(started) stop();
    	started = true;
    	timer = new Timer();
    	timer.scheduleAtFixedRate(this, interval, interval);
    }

    public void stop() {
    	try {
    		try {
    			if(timer != null) timer.cancel();
    		} catch (Exception e) {
    			
    		}
    		timer = null;
    		queue = null;
    	} catch (Exception e) {
    		//not important
    	} finally {
    		started = false;
    	}
    }

	public void run() {
		try {
			int removed = 0;
			synchronized(this.queue) { // We can afford to lock here because it will either run very quickly or the server is congested anyway
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