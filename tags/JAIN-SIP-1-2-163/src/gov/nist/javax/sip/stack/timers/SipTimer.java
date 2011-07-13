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
package gov.nist.javax.sip.stack.timers;

import gov.nist.javax.sip.SipStackImpl;
import gov.nist.javax.sip.stack.SIPStackTimerTask;

import java.util.Properties;

/**
 * Interface to implement to plug a new Timer implementation. currently the  ones provided with the stack are based on java.util.Timer
 * or java.util.concurrent.ScheduledThreadPoolExecutor
 * 
 * @author jean.deruelle@gmail.com
 *
 */
public interface SipTimer {

	/**
	 * Schedule a new SIPStackTimerTask after the specified delay
	 * @param task the task to schedule
	 * @param delay the delay in milliseconds to schedule the task
	 * @return true if the task was correctly scheduled, false otherwise
	 */
	boolean schedule(SIPStackTimerTask task, long delay);
	
	/**
	 * Schedule a new SIPStackTimerTask after the specified delay
	 * @param task the task to schedule
	 * @param delay the delay in milliseconds to schedule the task
	 * @param period the period to run the task after it has been first scheduled 
	 * @return true if the task was correctly scheduled, false otherwise
	 */
	boolean scheduleWithFixedDelay(SIPStackTimerTask task, long delay, long period);
	
	/**
	 * Stop the Timer (called when the stack is stop or reinitialized)
	 */
	void stop();
	
	/**
	 * cancel a previously scheduled task
	 * @param task task to cancel
	 * @return true if the task was cancelled, false otherwise
	 */
	boolean cancel(SIPStackTimerTask task);
	
	/**
	 * Start the SIP Timer, called when the stack is created. The stack configuration is passed
	 * so that different implementations can use specific config properties to configure themselves
	 * @param sipStack TODO
	 * @param configurationProperties the stack properties
	 */
	void start(SipStackImpl sipStack, Properties configurationProperties);
	
	/**
	 * Check if the timer is started or stopped
	 * @return true is the timer is started false otherwise
	 */
	boolean isStarted();
}
