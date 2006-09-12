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

import javax.sip.InvalidArgumentException;

import gov.nist.javax.sip.header.ims.MediaAuthorizationList;
import gov.nist.javax.sip.header.ims.MediaAuthorization;
import gov.nist.javax.sip.header.ims.SIPHeaderNamesIms;
import gov.nist.core.Token;
import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.parser.HeaderParser;
import gov.nist.javax.sip.parser.Lexer;
import gov.nist.javax.sip.parser.TokenTypes;


/**
 * 
 * @author Jose Miguel Freitas 
 */

public class MediaAuthorizationParser 
	extends HeaderParser
	implements TokenTypes
{

	public MediaAuthorizationParser(String mediaAuthorization)
	{
		super(mediaAuthorization);
		
	}

	public MediaAuthorizationParser(Lexer lexer)
	{
		super(lexer);
		
	}
		
	
	 
	 
	 
	public SIPHeader parse() throws ParseException
	{
		MediaAuthorizationList mediaAuthorizationList = new MediaAuthorizationList();
		
		if (debug)
			dbg_enter("MediaAuthorizationParser.parse");
		
		
		try
		{
			headerName(TokenTypes.P_MEDIA_AUTHORIZATION);
			
			while (lexer.lookAhead(0) != '\n')
			{
				MediaAuthorization mediaAuthorization = new MediaAuthorization();
				
				mediaAuthorization.setHeaderName(SIPHeaderNamesIms.P_MEDIA_AUTHORIZATION);
				
				this.lexer.match(TokenTypes.ID);
				this.lexer.SPorHT();
				Token token = lexer.getNextToken();
				try
				{
					mediaAuthorization.setMediaAuthorizationToken(token.getTokenValue());
				}
				catch (InvalidArgumentException e)
				{
					throw createParseException(e.getMessage());
				}
				this.lexer.SPorHT();
				
				mediaAuthorizationList.add(mediaAuthorization);
				
				
				while (lexer.lookAhead(0) == ',')
				{
					this.lexer.match(',');
					
					mediaAuthorization = new MediaAuthorization();
					
					this.lexer.match(TokenTypes.ID);
					this.lexer.SPorHT();
					token = lexer.getNextToken();
					try
					{
						mediaAuthorization.setMediaAuthorizationToken(token.getTokenValue());
					}
					catch (InvalidArgumentException e)
					{
						throw createParseException(e.getMessage());
					}
					this.lexer.SPorHT();

					mediaAuthorizationList.add(mediaAuthorization);
				}

				
			}
		}
		finally 
		{
			if (debug)
				dbg_leave("MediaAuthorizationParser.parse");
		}
		
		
		return mediaAuthorizationList;
     
	}
	 


	
	/** 
	 * teste
	 * 
	public static void main(String args[]) throws ParseException 
	{
	    String pHeader[] = {
	    	"P-Media-Authorization: 0123456789 \n",
	        "P-Media-Authorization: 0123456789, ABCDEF\n"
	        };
	    
	    for (int i = 0; i < r.length; i++ )
	    {
	        MediaAuthenticationParser mParser = 
	        	new MediaAuthenticationParser(pHeader[i]);
	        	
	        MediaAuthenticationList mList= (MediaAuthenticationList) mParser.parse();
	        System.out.println("encoded = " + mList.encode());
	    }
	}
	 */
	
	
	
}
