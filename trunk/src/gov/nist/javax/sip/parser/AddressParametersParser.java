package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.*;
import gov.nist.javax.sip.address.*;
import java.text.ParseException;

/**
 * Address parameters parser.
 *
 * @version  JAIN-SIP-1.1 $Revision: 1.4 $ $Date: 2004-01-22 13:26:31 $
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
			super.parse(addressParametersHeader);
		} finally {
			dbg_leave("AddressParametersParser.parse");
		}
	}
}
/*
 * $Log: not supported by cvs2svn $
 */
