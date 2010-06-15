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
 * of the terms of this agreement.
 *
 */
package gov.nist.javax.sip.parser;

import java.text.ParseException;

import gov.nist.javax.sip.message.SIPMessage;


/**
 * Interface defining the contract for the stack to interact with the message parser to parse a byte array containing the SIP Message
 * into a SIPMessage object
 * 
 * @author jean.deruelle@gmail.com
 *
 */
public interface MessageParser {

	/**
	 * parse a byte array containing the SIP Message into a SIPMessage object
	 * @param msgBytes the SIP Message received from the network
	 * @param readBody If the content body should be read or not
	 * @param exhandler Callback if an exception occurs during the parsing to notify back the stack 
	 * @return a SIPMessage object that the stack can interact with
	 * @throws ParseException if a parseexception occurs
	 */
	SIPMessage parseSIPMessage(byte[] msgBytes, boolean readBody, boolean strict, ParseExceptionListener exhandler) throws ParseException;

}
