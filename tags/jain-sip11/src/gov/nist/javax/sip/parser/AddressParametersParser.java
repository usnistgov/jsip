
package gov.nist.javax.sip.parser;
import  gov.nist.javax.sip.header.*;
import  gov.nist.javax.sip.address.*;
import  gov.nist.core.*;
import  java.text.ParseException;


/** Address parameters parser.
*
*@version  JAIN-SIP-1.1
*@author M. Ranganathan <mranga@nist.gov>  
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
class AddressParametersParser  extends ParametersParser {

	protected AddressParametersHeader addressParametersHeader;

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
		this.addressParametersHeader  = addressParametersHeader;
		AddressParser addressParser = new AddressParser
				(this.getLexer());
		AddressImpl addr = addressParser.address();
		addressParametersHeader.setAddress(addr);
		super.parse(addressParametersHeader);
		} finally {
		   dbg_leave("AddressParametersParser.parse");
		}

	}

}
	
