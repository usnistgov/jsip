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
/*******************************************
 * PRODUCT OF PT INOVAO - EST DEPARTMENT *
 *******************************************/

package gov.nist.javax.sip.parser.ims;

import java.text.ParseException;

import gov.nist.javax.sip.header.ims.AuthorizationIms;

import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.parser.ChallengeParser;
import gov.nist.javax.sip.parser.Lexer;
import gov.nist.javax.sip.parser.TokenTypes;

/**
 * @author ALEXANDRE MIGUEL SILVA SANTOS - Nú 10045401
 */

public class AuthorizationImsParser extends ChallengeParser {
	
	/**
	 * Constructor
	 * @param authorizationIms AuthorizationIms message to parse
	 */
	public AuthorizationImsParser(String authorizationIms) {
		super(authorizationIms);
	}

	/**
	 * Cosntructor
	 * @param lexer Lexer to set
	 */
	protected AuthorizationImsParser(Lexer lexer) {
		super(lexer);
	}

	/**
	 * parse the String message 
	 * @return SIPHeader (Authorization object)
	 * @throws SIPParseException if the message does not respect the spec.
	 */
	public SIPHeader parse() throws ParseException {
		dbg_enter("parse");
		try {
			headerName(TokenTypes.AUTHORIZATION);
			AuthorizationIms auth = new AuthorizationIms();
			super.parse(auth);
			return auth;
		} finally {
			dbg_leave("parse");
		}
	}
	
	

}
