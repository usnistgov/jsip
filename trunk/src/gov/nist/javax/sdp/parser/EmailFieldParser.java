package gov.nist.javax.sdp.parser;

import gov.nist.javax.sdp.fields.*;
import java.text.ParseException;

/** 
 * Parser for Email Field
 *
 * @version JAIN-SDP-PUBLIC-RELEASE $Revision: 1.3 $ $Date: 2004-12-13 15:01:39 $
 *
 * @author Olivier Deruelle <deruelle@nist.gov> 
 * @author M. Ranganathan <mranga@nist.gov> 
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class EmailFieldParser extends SDPParser {

	/** Creates new EmailFieldParser */
	public EmailFieldParser(String emailField) {
		this.lexer = new Lexer("charLexer", emailField);
	}

	public String getDisplayName(String rest) {
		String retval = null;

		try {
			int begin = rest.indexOf("(");
			int end = rest.indexOf(")");

			if (begin != -1) {
				// e=mjh@isi.edu (Mark Handley)
				retval = rest.substring(begin + 1, end);
			} else {
				// The alternative RFC822 name quoting convention 
				// is also allowed for
				// email addresses. ex: e=Mark Handley <mjh@isi.edu>
				int ind = rest.indexOf("<");
				if (ind != -1) {
					retval = rest.substring(0, ind);
				} else {
					// There is no display name !!!
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return retval;
	}

	public Email getEmail(String rest) {
		Email email = new Email();

		try {
			int begin = rest.indexOf("(");

			if (begin != -1) {
				// e=mjh@isi.edu (Mark Handley)
				String emailTemp = rest.substring(0, begin);
				int i = emailTemp.indexOf("@");
				if (i != -1) {
					email.setUserName(emailTemp.substring(0, i));
					email.setHostName(emailTemp.substring(i + 1));
				} else {
					// Pb: the email is not well formatted
				}
			} else {
				// The alternative RFC822 name quoting convention is 
				// also allowed for
				// email addresses. ex: e=Mark Handley <mjh@isi.edu>
				int ind = rest.indexOf("<");
				int end = rest.indexOf(">");

				if (ind != -1) {
					String emailTemp = rest.substring(ind + 1, end);
					int i = emailTemp.indexOf("@");
					if (i != -1) {
						email.setUserName(emailTemp.substring(0, i));
						email.setHostName(emailTemp.substring(i + 1));
					} else {
						// Pb: the email is not well formatted
					}

				} else {
					int i = rest.indexOf("@");
					int j = rest.indexOf("\n");
					if (i != -1) {
						email.setUserName(rest.substring(0, i));
						email.setHostName(rest.substring(i + 1));
					} else {
						// Pb: the email is not well formatted
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return email;
	}

	public EmailField emailField() throws ParseException {
		try {

			this.lexer.match('e');
			this.lexer.SPorHT();
			this.lexer.match('=');
			this.lexer.SPorHT();

			EmailField emailField = new EmailField();
			EmailAddress emailAddress = new EmailAddress();

			String rest = lexer.getRest();

			String displayName = getDisplayName(rest.trim());
			emailAddress.setDisplayName(displayName);
			Email email = getEmail(rest);
			emailAddress.setEmail(email);

			emailField.setEmailAddress(emailAddress);
			return emailField;
		} catch (Exception e) {
			throw new ParseException(lexer.getBuffer(), lexer.getPtr());
		}
	}

	public SDPField parse() throws ParseException {
		return this.emailField();
	}


/*
	    public static void main(String[] args) throws ParseException {
		    String email[] = {
				"e=mjh@isi.edu (Mark Handley)\n",
				"e=Mark Handley <mjh@isi.edu>\n",
	                        "e= <mjh@isi.edu>\n",
	                        "e=mjh@isi.edu\n"
	                };
	
		    for (int i = 0; i < email.length; i++) {
		       EmailFieldParser emailFieldParser=new EmailFieldParser(
	                email[i] );
			EmailField emailFields=emailFieldParser.emailField();
			System.out.println("toParse: " +email[i]);
			System.out.println("encoded: " +emailFields.encode());
		    }
	
		}
*/

}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.2  2004/01/22 13:26:28  sverker
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
