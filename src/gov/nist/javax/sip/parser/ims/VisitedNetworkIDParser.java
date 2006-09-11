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

import gov.nist.javax.sip.header.ims.VisitedNetworkID;
import gov.nist.javax.sip.header.ims.VisitedNetworkIDList;
import gov.nist.core.Token;
import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.parser.Lexer;
import gov.nist.javax.sip.parser.ParametersParser;
import gov.nist.javax.sip.parser.TokenTypes;

/**
 * @author ALEXANDRE MIGUEL SILVA SANTOS 
 */

public class VisitedNetworkIDParser extends ParametersParser implements TokenTypes {
	
	/**
	 * Constructor
	 * @param recordRoute message to parse to set
	 */
	public VisitedNetworkIDParser(String networkID) {
		super(networkID);
		
	}

	protected VisitedNetworkIDParser(Lexer lexer) {
		super(lexer);
		
	}

	
	
	
	public SIPHeader parse() throws ParseException {
		
		VisitedNetworkIDList visitedNetworkIDList = new VisitedNetworkIDList();

		if (debug)
			dbg_enter("VisitedNetworkIDParser.parse");

		try {
			this.lexer.match(TokenTypes.P_VISITED_NETWORK_ID);
			this.lexer.SPorHT();
			this.lexer.match(':');
			this.lexer.SPorHT();
			
			while (true) {
				
				VisitedNetworkID visitedNetworkID = new VisitedNetworkID();
				
				if (this.lexer.lookAhead(0) == '\"')
					parseQuotedString(visitedNetworkID);
				else
					parseToken(visitedNetworkID);
					
				visitedNetworkIDList.add(visitedNetworkID);
				
				this.lexer.SPorHT();
				if (lexer.lookAhead(0) == ',') {
					this.lexer.match(',');
					this.lexer.SPorHT();
				} else if (lexer.lookAhead(0) == '\n')
					break;
				else
					throw createParseException("unexpected char");
			}
			return visitedNetworkIDList;
		} finally {
			if (debug)
				dbg_leave("VisitedNetworkIDParser.parse");
		}

	}
	
	protected void parseQuotedString(VisitedNetworkID visitedNetworkID) throws ParseException {
		
		if (debug)
			dbg_enter("parseQuotedString");
		
		try {
			
			StringBuffer retval = new StringBuffer();
			
			if (this.lexer.lookAhead(0) != '\"')
				throw createParseException("unexpected char");
			this.lexer.consume(1);
			
			while (true) {
				char next = this.lexer.getNextChar();
				if (next == '\"') {
					// Got to the terminating quote.
					break;
				} else if (next == '\0') {
					throw new ParseException("unexpected EOL", 1);
				} else if (next == '\\') {
					retval.append(next);
					next = this.lexer.getNextChar();
					retval.append(next);
				} else {
					retval.append(next);
				}
			}
			
			visitedNetworkID.setVisitedNetworkID(retval.toString());
			super.parse(visitedNetworkID);
			
					
				
		}finally {
			if (debug)
				dbg_leave("parseQuotedString.parse");
		}
		
	}
		
	protected void parseToken(VisitedNetworkID visitedNetworkID) throws ParseException {
				
		
		Token token = lexer.getNextToken();
		String value = token.getTokenValue();
		visitedNetworkID.setVisitedNetworkID(value);
		super.parse(visitedNetworkID);
							
	}


}
