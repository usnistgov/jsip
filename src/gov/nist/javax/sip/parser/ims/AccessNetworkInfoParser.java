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

package gov.nist.javax.sip.parser.ims;


import java.text.ParseException;

import gov.nist.javax.sip.header.ims.AccessNetworkInfo;
import gov.nist.javax.sip.header.ims.SIPHeaderNamesIms;
import gov.nist.core.Token;
import gov.nist.core.NameValue;
import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.parser.HeaderParser;
import gov.nist.javax.sip.parser.Lexer;
import gov.nist.javax.sip.parser.ParametersParser;
import gov.nist.javax.sip.parser.TokenTypes;


/**
 * 
 * @author Jose Miguel Freitas 
 */


public class AccessNetworkInfoParser
	extends HeaderParser
	implements TokenTypes
{
	
	public AccessNetworkInfoParser(String accessNetwork) {
				
		super(accessNetwork);
		
	}
	
	
	protected AccessNetworkInfoParser(Lexer lexer) {
		super(lexer);
		
	}

	

	
	
	/*
	 * baseado no VisitedNetworkID
	 */
	public SIPHeader parse() throws ParseException
	{	
		
		if (debug)
			dbg_enter("AccessNetworkInfoParser.parse");
		try {
			headerName(TokenTypes.P_ACCESS_NETWORK_INFO);
			AccessNetworkInfo accessNetworkInfo = new AccessNetworkInfo();
			
			accessNetworkInfo.setHeaderName(SIPHeaderNamesIms.P_ACCESS_NETWORK_INFO);
			
			this.lexer.SPorHT();
			
			NameValue nv = super.nameValue();
			accessNetworkInfo.setParameter(nv);
			this.lexer.SPorHT();
			while (lexer.lookAhead(0) == ',') {
				this.lexer.match(',');
				this.lexer.SPorHT();

				nv = super.nameValue();
				accessNetworkInfo.setParameter(nv);
				this.lexer.SPorHT();
			}
			this.lexer.SPorHT();
			//this.lexer.match('\n');

			return accessNetworkInfo;
		} finally {
			if (debug)
				dbg_leave("AccessNetworkInfoParser.parse");
		}
		
		
	
		
	}
	
	
	
	
}
