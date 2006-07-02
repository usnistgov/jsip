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
import gov.nist.javax.sip.parser.Lexer;
import gov.nist.javax.sip.parser.ParametersParser;


/**
 * 
 * @author Jose Miguel Freitas 23875
 */


public class AccessNetworkInfoParser
	extends ParametersParser 
	implements TokenTypesIms 
{
	
	public AccessNetworkInfoParser(String accessNetwork) {
				
		super(accessNetwork);
		this.selectLexerIms();
				
	}
	
	
	protected AccessNetworkInfoParser(Lexer lexer) {
		super(lexer);
		this.selectLexerIms();
	}

	public void selectLexerIms() {
	
		LexerIms lexerims = new LexerIms();
	
	}

	
	
	/*
	 * baseado no VisitedNetworkID
	 */
	public SIPHeader parse() throws ParseException
	{	
		
		if (debug)
			dbg_enter("AccessNetworkInfoParser.parse");
		try {
			headerName(TokenTypesIms.P_ACCESS_NETWORK_INFO);
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
		
		
		/*
		AccessNetworkInfoList accessNetworkInfoList = new AccessNetworkInfoList();

		if (debug)
			dbg_enter("AccessNetworkInfoParser.parse");

		try {
			this.lexer.match(TokenTypesIms.P_ACCESS_NETWORK_INFO);
			this.lexer.SPorHT();
			this.lexer.match(':');
			this.lexer.SPorHT();
			
			while (true) {
				
				AccessNetworkInfo accessNetworkInfo = new AccessNetworkInfo();
				
				if (this.lexer.lookAhead(0) == '\"')
					parseQuotedString(accessNetworkInfo);
				else
					parseToken(accessNetworkInfo);
					
				
				accessNetworkInfoList.add(accessNetworkInfo);
				
				
				this.lexer.SPorHT();
				if (lexer.lookAhead(0) == ';')
				{
					this.lexer.match(';');
					this.lexer.SPorHT();
				} 
				else if (lexer.lookAhead(0) == '\n')
					break;
				else
					throw createParseException("unexpected char");
			}
			return accessNetworkInfoList;
			
		} finally {
			if (debug)
				dbg_leave("AccessNetworkInfoParser.parse");
		}
		*/
		
	}
	
	protected void parseQuotedString(AccessNetworkInfo accessNetworkInfo) throws ParseException
	{
		
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
			
			accessNetworkInfo.setAccessType(retval.toString());
			super.parse(accessNetworkInfo);
			
					
				
		}finally {
			if (debug)
				dbg_leave("parseQuotedString.parse");
		}
		
	}
		
	protected void parseToken(AccessNetworkInfo accessNetworkInfo) throws ParseException 
	{	
		Token token = lexer.getNextToken();
		String value = token.getTokenValue();
		accessNetworkInfo.setAccessType(value);
		super.parse(accessNetworkInfo);
							
	}
	
	
	
	
	/*
	 * 
	public SIPHeader parse() throws ParseException
	{

		if (debug)
			dbg_enter("AccessNetworkInfoParser.parse");
		try {
			headerName(TokenTypesIms.P_ACCESS_NETWORK_INFO);
			AccessNetworkInfo accessNetworkInfo = new AccessNetworkInfo();
			
			accessNetworkInfo.setHeaderName(
					SIPHeaderNamesIms.P_ACCESS_NETWORK_INFO);

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

	*/
	
	
}
