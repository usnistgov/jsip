package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.*;
import java.text.ParseException;

/** 
 * A parser for The SIP contact header.
 * 
 * @version  JAIN-SIP-1.1 $Revision: 1.7 $ $Date: 2005-10-14 19:40:09 $
 */
public class ContactParser extends AddressParametersParser {

	public ContactParser(String contact) {
		super(contact);
	}

	protected ContactParser(Lexer lexer) {
		super(lexer);
		this.lexer = lexer;
	}

	public SIPHeader parse() throws ParseException {
		// past the header name and the colon.
		headerName(TokenTypes.CONTACT);
		ContactList retval = new ContactList();
		while (true) {
			Contact contact = new Contact();
			if (lexer.lookAhead(0) == '*') {
				final char next = lexer.lookAhead(1);
				if (next==' '||next=='\t'||next=='\r'||next=='\n') {
					this.lexer.match('*');
					contact.setWildCardFlag(true);
				} else {
					super.parse(contact);
				}
			} else {
				super.parse(contact);
			}
			retval.add(contact);
			this.lexer.SPorHT();
			if (lexer.lookAhead(0) == ',') {
				this.lexer.match(',');
				this.lexer.SPorHT();
			} else if (lexer.lookAhead(0) == '\n' || 
					lexer.lookAhead(0) == '\0')
				break;
			else
				throw createParseException("unexpected char");
		}
		return retval;
	}


}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.6  2004/07/28 14:13:54  mranga
 * Submitted by:  mranga
 *
 * Move out the test code to a separate test/unit class.
 * Fixed some encode methods.
 *
 * Revision 1.5  2004/04/22 22:51:17  mranga
 * Submitted by:  Thomas Froment
 * Reviewed by:   mranga
 *
 * Fixed corner cases.
 *
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
