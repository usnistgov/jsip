package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.*;
import java.text.ParseException;

/** Parser for Organization header.
 *
 * @version  JAIN-SIP-1.1 $Revision: 1.4 $ $Date: 2004-01-22 13:26:31 $
 *
 * @author Olivier Deruelle <deruelle@nist.gov>  <br/>
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class OrganizationParser extends HeaderParser {

	/**
	 * Creates a new instance of OrganizationParser 
	 * @param organization the header to parse 
	 */
	public OrganizationParser(String organization) {
		super(organization);
	}

	/**
	 * Constructor
	 * @param lexer the lexer to use to parse the header
	 */
	protected OrganizationParser(Lexer lexer) {
		super(lexer);
	}

	/**
	 * parse the String header
	 * @return SIPHeader (Organization object) 
	 * @throws SIPParseException if the message does not respect the spec.
	 */
	public SIPHeader parse() throws ParseException {

		if (debug)
			dbg_enter("OrganizationParser.parse");
		Organization organization = new Organization();
		try {
			headerName(TokenTypes.ORGANIZATION);

			organization.setHeaderName(SIPHeaderNames.ORGANIZATION);

			this.lexer.SPorHT();
			String value = this.lexer.getRest();

			organization.setOrganization(value.trim());

			return organization;
		} finally {
			if (debug)
				dbg_leave("OrganizationParser.parse");
		}
	}

	/** Test program
	public static void main(String args[]) throws ParseException {
	String o[] = {
	        "Organization: Boxes by Bob\n"
	        };
		
	for (int i = 0; i <o.length; i++ ) {
	    OrganizationParser parser = 
		  new OrganizationParser(o[i]);
	    Organization org= (Organization) parser.parse();
	    System.out.println("encoded = " + org.encode());
	}		
	}
	 */
}
