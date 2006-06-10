package gov.nist.javax.sip.parser;
import java.text.ParseException;
import gov.nist.javax.sip.address.*;
import gov.nist.javax.sip.header.*;
import gov.nist.core.*;

/**
 * To Header parser.
 *
 * @version JAIN-SIP-1.1 $Revision: 1.6 $ $Date: 2004-04-22 22:51:18 $
 *
 * @author Olivier Deruelle <deruelle@nist.gov>  <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class ToParser extends AddressParametersParser {

	/**
	 * Creates new ToParser
	 * @param to String to set
	 */
	public ToParser(String to) {
		super(to);
	}

	protected ToParser(Lexer lexer) {
		super(lexer);
	}

	public SIPHeader parse() throws ParseException {

		headerName(TokenTypes.TO);
		To to = new To();
		super.parse(to);
		this.lexer.match('\n');
		if (((AddressImpl) to.getAddress()).getAddressType()
			== AddressImpl.ADDRESS_SPEC) {
			// the parameters are header parameters.
			if (to.getAddress().getURI() instanceof SipUri) {
				SipUri sipUri = (SipUri) to.getAddress().getURI();
				NameValueList parms = sipUri.getUriParms();
				if (parms != null && !parms.isEmpty()) {
					to.setParameters(parms);
				}
				sipUri.removeUriParms();
			}
		}
		return to;
	}

	/**

	    public static void main(String args[]) throws ParseException {
	        String to[] = {
	           "To: <sip:+1-650-555-2222@ss1.wcom.com;user=phone>;tag=5617\n",
	           "To: T. A. Watson <sip:watson@bell-telephone.com;param=something>\n",
	           "To: LittleGuy <sip:UserB@there.com;tag=foo>;tag=bar\n",
	           "To: sip:mranga@120.6.55.9\n",
	           "To: sip:mranga@129.6.55.9;tag=696928473514.129.6.55.9\n",
	           "To: sip:mranga@129.6.55.9; tag=696928473514.129.6.55.9\n",
	           "To: sip:mranga@129.6.55.9 ;tag=696928473514.129.6.55.9\n",
	           "To: sip:mranga@129.6.55.9 ; tag=696928473514.129.6.55.9\n"
	        };
	        
	        for (int i = 0; i < to.length; i++ ) {
		    System.out.println("toParse = " + to[i]);
	            ToParser tp =
	            new ToParser(to[i]);
	            To t = (To) tp.parse();
	            System.out.println("encoded = " + t.encode());
	        }
	        
	    }
	**/
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.5  2004/01/22 13:26:32  sverker
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
