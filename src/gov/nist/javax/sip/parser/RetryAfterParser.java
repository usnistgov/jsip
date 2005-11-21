package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.*;
import gov.nist.core.*;
import java.text.ParseException;
import javax.sip.*;

/**
 * Parser for RetryAfter header.
 *
 * @version  JAIN-SIP-1.1
 *
 * @author Olivier Deruelle <deruelle@nist.gov> 
 * @author M. Ranganathan <mranga@nist.gov>  
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 * @version JAIN-SIP-1.1 $Revision: 1.5 $ $Date: 2005-11-21 23:25:35 $
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
