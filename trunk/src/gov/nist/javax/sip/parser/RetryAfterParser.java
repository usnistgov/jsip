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
package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.*;
import gov.nist.core.*;
import java.text.ParseException;
import javax.sip.*;

/**
 * Parser for RetryAfter header.
 *
 * @version 1.2
 *
 * @author Olivier Deruelle  
 * @author M. Ranganathan   
 * 
 *
 * @version 1.2 $Revision: 1.6 $ $Date: 2006-07-02 09:51:18 $
 */
public class RetryAfterParser extends HeaderParser {

	/**
	 * Creates a new instance of RetryAfterParser 
	 * @param retryAfter the header to parse
	 */
	public RetryAfterParser(String retryAfter) {
		super(retryAfter);
	}

	/**
	 * Constructor
	 * @param lexer the lexer to use to parse the header
	 */
	protected RetryAfterParser(Lexer lexer) {
		super(lexer);
	}

	/**
	 * parse the String message
	 * @return SIPHeader (RetryAfter object)
	 * @throws SIPParseException if the message does not respect the spec.
	 */
	public SIPHeader parse() throws ParseException {

		if (debug)
			dbg_enter("RetryAfterParser.parse");

		RetryAfter retryAfter = new RetryAfter();
		try {
			headerName(TokenTypes.RETRY_AFTER);

			// mandatory delatseconds:
			String value = lexer.number();
			try {
				int ds = Integer.parseInt(value);
				retryAfter.setRetryAfter(ds);
			} catch (NumberFormatException ex) {
				throw createParseException(ex.getMessage());
			} catch (InvalidArgumentException ex) {
				throw createParseException(ex.getMessage());
			}

			this.lexer.SPorHT();
			if (lexer.lookAhead(0) == '(') {
				String comment = this.lexer.comment();
				retryAfter.setComment(comment);
			}
			this.lexer.SPorHT();

			while (lexer.lookAhead(0) == ';') {
				this.lexer.match(';');
				this.lexer.SPorHT();
				lexer.match(TokenTypes.ID);
				Token token = lexer.getNextToken();
				value = token.getTokenValue();
				if (value.equalsIgnoreCase("duration")) {
					this.lexer.match('=');
					this.lexer.SPorHT();
					value = lexer.number();
					try {
						int duration = Integer.parseInt(value);
						retryAfter.setDuration(duration);
					} catch (NumberFormatException ex) {
						throw createParseException(ex.getMessage());
					} catch (InvalidArgumentException ex) {
						throw createParseException(ex.getMessage());
					}
				} else {
					this.lexer.SPorHT();
					this.lexer.match('=');
					this.lexer.SPorHT();
					lexer.match(TokenTypes.ID);
					Token secondToken = lexer.getNextToken();
					String secondValue = secondToken.getTokenValue();
					retryAfter.setParameter(value, secondValue);
				}
				this.lexer.SPorHT();
			}
		} finally {
			if (debug)
				dbg_leave("RetryAfterParser.parse");
		}

		return retryAfter;
	}

	/** Test program
	public static void main(String args[]) throws ParseException {
	    String rr[] = {
	        "Retry-After: 18000;duration=3600\n",
	        "Retry-After: 120;duration=3600;ra=oli\n",
	        "Retry-After: 1220 (I'm in a meeting)\n",
	        "Retry-After: 1230 (I'm in a meeting);fg=der;duration=23\n"
	    };
	    
	    for (int i = 0; i < rr.length; i++ ) {
	        RetryAfterParser parser =
	        new RetryAfterParser(rr[i]);
	        RetryAfter r= (RetryAfter) parser.parse();
	        System.out.println("encoded = " + r.encode());
	    }
	    
	}
	 */
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.4  2006/06/19 06:47:27  mranga
 * javadoc fixups
 *
 * Revision 1.3  2006/06/16 15:26:28  mranga
 * Added NIST disclaimer to all public domain files. Clean up some javadoc. Fixed a leak
 *
 * Revision 1.2  2005/11/21 23:24:18  jeroen
 * parameter name is case insensitive
 *
 * Revision 1.1.1.1  2005/10/04 17:12:36  mranga
 *
 * Import
 *
 *
 * Revision 1.4  2004/01/22 13:26:32  sverker
 * Issue number:
 * Obtained from:
 * Submitted by:  sverker
 * Reviewed by:   mranga
 *
 * Major reformat of code to conform with style guide. Resolved compiler and javadoc warnings. Added CVS tags.
 *
 * CVS: ----------------------------------------------------------------------
 * CVS: Issue number:
 * CVS:   If this change addresses one or more issues,
 * CVS:   then enter the issue number(s) here.
 * CVS: Obtained from:
 * CVS:   If this change has been taken from another system,
 * CVS:   then name the system in this line, otherwise delete it.
 * CVS: Submitted by:
 * CVS:   If this code has been contributed to the project by someone else; i.e.,
 * CVS:   they sent us a patch or a set of diffs, then include their name/email
 * CVS:   address here. If this is your work then delete this line.
 * CVS: Reviewed by:
 * CVS:   If we are doing pre-commit code reviews and someone else has
 * CVS:   reviewed your changes, include their name(s) here.
 * CVS:   If you have not had it reviewed then delete this line.
 *
 */
