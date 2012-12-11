/*
 * @author:     Brett Buckingham
 * @author:     Last modified by: $Author: deruelle_jean $
 * @version:    $Date: 2010-05-06 14:08:11 $ $Revision: 1.4 $
 *
 * This source code has been contributed to the public domain.
 */
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
