package gov.nist.javax.sip.parser;
import gov.nist.javax.sip.address.*;
import gov.nist.javax.sip.header.*;
import java.text.ParseException;
import gov.nist.core.*;

/** From header parser.
 *
 * @version  JAIN-SIP-1.1 $Revision: 1.6 $ $Date: 2004-04-22 22:51:17 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class FromParser extends AddressParametersParser {

	public FromParser(String from) {
		super(from);
	}

	protected FromParser(Lexer lexer) {
		super(lexer);
	}

	public SIPHeader parse() throws ParseException {

		From from = new From();

		this.lexer.match(TokenTypes.FROM);
		this.lexer.SPorHT();
		this.lexer.match(':');
		this.lexer.SPorHT();
		super.parse(from);
		this.lexer.match('\n');
		if (((AddressImpl) from.getAddress()).getAddressType()
			== AddressImpl.ADDRESS_SPEC) {
			// the parameters are header parameters.
			if (from.getAddress().getURI() instanceof SipUri) {
				SipUri sipUri = (SipUri) from.getAddress().getURI();
				NameValueList parms = sipUri.getUriParms();
				if (parms != null && !parms.isEmpty()) {
					from.setParameters(parms);
				}
				sipUri.removeUriParms();
			}
		}

		return from;

	}

	/**
	
	        public static void main(String args[]) throws ParseException {
		String from[] = {
		"From: foobar at com<sip:4855@166.34.120.100 >;tag=1024181795\n",
		"From: sip:user@company.com\n",
		"From: sip:caller@university.edu\n",
	        "From: sip:localhost\n",
	        "From: \"A. G. Bell\" <sip:agb@bell-telephone.com> ;tag=a48s\n"
	         };
				
			for (int i = 0; i < from.length; i++ ) {
			    try {
			       FromParser fp = new FromParser(from[i]);
			       From f = (From) fp.parse();
			       System.out.println("encoded = " + f.encode());
			    } catch (ParseException ex) {
				System.out.println(ex.getMessage());
			    } 
			}
				
		}
	
	**/
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.5  2004/01/22 13:26:31  sverker
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
