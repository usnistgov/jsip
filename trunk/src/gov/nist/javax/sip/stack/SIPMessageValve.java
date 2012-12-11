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

import gov.nist.javax.sip.message.SIPRequest;

import javax.sip.SipStack;
import javax.sip.message.Response;

/**
 * This interface has callbacks that are notified for every SIP message arriving at the container.
 * The callbacks occurs before any significant long-lived resources are allocated for this call, thus
 * it gives a chance to the application to pre-process the message and filter based on some
 * application-specific algorithm. Creating and sending a stateless response is also allowed.
 * 
 * It is useful for congestion control or header re-writing.
 * 
 * @author Vladimir Ralev <vralev@redhat.com>
 *
 */
public interface SIPMessageValve {
	/**
	 * The callback method that is called for every request before any transaction/dialog mapping
	 * or allocation occur.
	 * 
	 * @param request
	 * @param messageChannel
	 * @return
	 */
	public boolean processRequest(SIPRequest request, MessageChannel messageChannel);
	
	/**
	 * The callback method that is called for every response before any transaction/dialog mapping
	 * or allocation occur.
	 * 
	 * @param response
	 * @param messageChannel
	 * @return
	 */
	public boolean processResponse(Response response, MessageChannel messageChannel);
	
	/**
	 * This method is called when the valve is initialized. You can perform any initialization here.
	 * 
	 * @param stack
	 */
	public void init(SipStack stack);
	
	/**
	 * This method is called when the valve is about to be destroyed. You can perform any cleanup here.
	 */
	public void destroy();
}
