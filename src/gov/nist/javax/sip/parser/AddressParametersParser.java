package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.*;
import gov.nist.javax.sip.address.*;
import java.text.ParseException;

/**
 * Address parameters parser.
 *
 * @version  JAIN-SIP-1.1 $Revision: 1.5 $ $Date: 2004-04-22 22:51:17 $
 * @author M. Ranganathan <mranga@nist.gov>  
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
class AddressParametersParser extends ParametersParser {

	protected AddressParametersParser(Lexer lexer) {
		super(lexer);
	}

	protected AddressParametersParser(String buffer) {
		super(buffer);
	}

	protected void parse(AddressParametersHeader addressParametersHeader)
		throws ParseException {
		dbg_enter("AddressParametersParser.parse");
		try {
			AddressParser addressParser = new AddressParser(this.getLexer());
			AddressImpl addr = addressParser.address();
			addressParametersHeader.setAddress(addr);
			lexer.SPorHT();
			if ( this.lexer.hasMoreChars() &&
			     this.lexer.lookAhead(0) != '\0' &&
			     this.lexer.lookAhead(0) != '\n' &&
			     this.lexer.startsId()) {

			     super.parseNameValueList(addressParametersHeader);
			      
				
			}  else super.parse(addressParametersHeader);
		
		} catch (ParseException ex) {
			throw ex;
		} finally {
			dbg_leave("AddressParametersParser.parse");
		}
	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.4  2004/01/22 13:26:31  sverker
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
