/*
 * @author:     Brett Buckingham
 * @author:     Last modified by: $Author: deruelle_jean $
 * @version:    $Date: 2010-05-06 14:08:11 $ $Revision: 1.4 $
 *
 * This source code has been contributed to the public domain.
 */

package gov.nist.javax.sip.stack;


/**
 * A subclass of TimerTask which runs TimerTask code within a try/catch block to
 * avoid killing the SIPTransactionStack timer thread. Note: subclasses MUST not
 * override run(); instead they should override runTask().
 *
 * @author Brett Buckingham
 *
 */
public abstract class SIPStackTimerTask {
	// the underlying timer task that was scheduled in the Stack SIP timer
	Object timerTask = null; 
    // Implements code to be run when the SIPStackTimerTask is executed.
    public abstract void runTask();
    
    public void cleanUpBeforeCancel() {
    	
    }
    
	public void setSipTimerTask(Object timer) {
		timerTask = timer;
	}

	public Object getSipTimerTask() {
		return timerTask;
	}
}
