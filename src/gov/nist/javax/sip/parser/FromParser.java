package gov.nist.javax.sip.parser;
import gov.nist.javax.sip.address.*;
import gov.nist.javax.sip.header.*;
import java.text.ParseException;
import gov.nist.core.*;

/** From header parser.
 *
 * @version  JAIN-SIP-1.1 $Revision: 1.5 $ $Date: 2004-01-22 13:26:31 $
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
					sipUri.removeUriParms();
				}
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
 */
