package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.*;
import java.text.ParseException;

/** 
 * A parser for The SIP contact header.
 * 
 * @version  JAIN-SIP-1.1 $Revision: 1.5 $ $Date: 2004-04-22 22:51:17 $
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
				this.lexer.match('*');
				contact.setWildCardFlag(true);
			} else
				super.parse(contact);
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

	        public static void main(String args[]) throws ParseException {
			String contact[] = {
			"Contact:<sip:utente@127.0.0.1:5000;transport=udp>;expires=3600\n",
			"Contact:BigGuy<sip:utente@127.0.0.1:5000>;expires=3600\n",
			"Contact: sip:4855@166.35.224.216:5060\n",
			"Contact: sip:user@host.company.com\n",
			"Contact: Bo Bob Biggs\n"+ 
		"< sip:user@example.com?Route=%3Csip:sip.example.com%3E >\n",
	                        "Contact: Joe Bob Briggs <sip:mranga@nist.gov>\n",
	                        "Contact: \"Mr. Watson\" <sip:watson@worcester.bell-telephone.com>"+
	                        " ; q=0.7; expires=3600,\"Mr. Watson\" <mailto:watson@bell-telephone.com>"+
	                        ";q=0.1\n",
	                        "Contact: LittleGuy <sip:UserB@there.com;user=phone>"+
	                        ",<sip:+1-972-555-2222@gw1.wcom.com;user=phone>,tel:+1-972-555-2222"+
	                        "\n",
	                        "Contact:*\n",
	                        "Contact:BigGuy<sip:utente@127.0.0.1;5000>;Expires=3600\n"
	                };
				
			for (int i = 0; i < contact.length; i++ ) {
			    ContactParser cp = 
				  new ContactParser(contact[i]);
			    ContactList cl = (ContactList) cp.parse(); 
			    System.out.println("encoded = " + cl.encode());
	                    System.out.println();
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
