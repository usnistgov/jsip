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

import gov.nist.core.GenericObject;
import gov.nist.javax.sip.address.TelephoneNumber;
import gov.nist.javax.sip.parser.Lexer;
import gov.nist.javax.sip.parser.TokenTypes;
import gov.nist.javax.sip.parser.URLParser;
import gov.nist.javax.sip.header.ims.PreferredIdentity;

/**
 * @author ALEXANDRE MIGUEL SILVA SANTOS 
 */

public class PreferredIdentityParser extends AddressHeaderParser implements TokenTypes {
	
	public PreferredIdentityParser(String preferredIdentity) {
		super(preferredIdentity);
		
	}

	
	protected PreferredIdentityParser(Lexer lexer) {
		super(lexer);
		
	}

	
	
	public GenericObject biparse() throws ParseException {
		
		if (debug)
			dbg_enter("PreferredIdentityParser.parse");
		
		try {
			
				
				GenericObject xpto = null;
				boolean isTEL = false;
		
				this.lexer.match(TokenTypes.P_PREFERRED_IDENTITY);
				this.lexer.SPorHT();
				this.lexer.match(':');
				this.lexer.SPorHT();
				
				String aaa = this.lexer.charAsString(3);
				
								
				if(aaa.equalsIgnoreCase("tel")) {
					
					this.lexer.match(TokenTypes.TEL);
					this.lexer.SPorHT();
					
					String bbb = this.lexer.charAsString(1);
					
					if(bbb.equalsIgnoreCase(":")) {
						
						this.lexer.match(':');
						this.lexer.SPorHT();
						isTEL = true;
						
					}
						
				}
				
				else
					isTEL = false;
													
				if(isTEL) {
					
										
					xpto = this.parseTEL();
					
					if (lexer.lookAhead(0) == '\n') {
						
						return xpto;
					}

					else
						throw createParseException("unexpected char");
				
				}
					
				else {
					
										
					xpto = this.parsexpto();
					
					if (lexer.lookAhead(0) == '\n') {
						
						return xpto;
					}

					else
						throw createParseException("unexpected char");
					
				}
		}
				
		finally {
			if (debug)
				dbg_leave("PreferredIdentityParser.parse");
			}
				
						
	}

						
	
	protected GenericObject parsexpto() throws ParseException {
			
		PreferredIdentity preferredIdentity = new PreferredIdentity();
		
		
						
		super.parse(preferredIdentity);
		this.lexer.SPorHT();
	
		if (lexer.lookAhead(0) == '\n') {
				
			return preferredIdentity;
		}

		else
			throw createParseException("unexpected char");							
				
	}
	
	
	protected GenericObject parseTEL() throws ParseException {
		
		
		
		TelephoneNumber telephoneNumber = new TelephoneNumber();
		
		URLParser p = new URLParser(this.lexer.byteStringNoComma());
						
		telephoneNumber = p.parseTelephoneNumber();
		
		return telephoneNumber;
			
		
	}
	
	
	

}
