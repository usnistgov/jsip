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

import java.util.Properties;

import gov.nist.javax.sip.SipStackImpl;
import gov.nist.javax.sip.stack.CallAnalyzer.MetricAnalysisConfiguration;
import gov.nist.javax.sip.stack.CallAnalyzer.MetricReference;

import javax.sip.SipStack;
import javax.sip.message.Message;

/**
 * This sample interceptor keeps track of requests stuck in JAIN SIP threads and prints a thread dump
 * when such event occurs periodically.
 * @author Vladimir Ralev
 *
 */
public class CallAnalysisInterceptor implements SIPEventInterceptor {

	private CallAnalyzer callAnalyzer;
	private static final MetricReference interceptorCheckpoint = new MetricReference("ick");
	
	public void afterMessage(Message message) {
		callAnalyzer.leave(interceptorCheckpoint);
		
	}

	public void beforeMessage(Message message) {
		callAnalyzer.enter(interceptorCheckpoint);
	}

	public void destroy() {
		callAnalyzer.stop();
		callAnalyzer = null;
		
	}
	public void init(SipStack stack) {
		callAnalyzer = new CallAnalyzer(((SipStackImpl) stack));
		Properties props = ((SipStackImpl) stack).getConfigurationProperties();
		Long checkingInterval = Long.parseLong(
				props.getProperty(CallAnalysisInterceptor.class.getName() + ".checkingInterval", "1000"));
		Long minStuckTime = Long.parseLong(
				props.getProperty(CallAnalysisInterceptor.class.getName() + ".minStuckTIme", "4000"));
		Long minTimeBetweenDumps = Long.parseLong(
				props.getProperty(CallAnalysisInterceptor.class.getName() + ".minTimeBetweenDumps", "2000"));
		MetricAnalysisConfiguration config = new MetricAnalysisConfiguration(
				checkingInterval, minTimeBetweenDumps, minStuckTime);
		callAnalyzer.configure(interceptorCheckpoint, config);
	}
	

}
