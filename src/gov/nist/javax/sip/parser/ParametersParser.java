
package gov.nist.javax.sip.parser;
import  gov.nist.javax.sip.header.*;
import  gov.nist.javax.sip.address.*;
import  gov.nist.core.*;
import  java.text.ParseException;


/** parameters parser header.
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public abstract class ParametersParser  extends HeaderParser {

	protected ParametersParser(Lexer lexer) {
		super((Lexer)lexer);
	}

	protected ParametersParser(String buffer) {
		super(buffer);
	}

	protected void parse(ParametersHeader parametersHeader)
	throws ParseException {
		this.lexer.SPorHT();
		while (lexer.lookAhead(0) == ';') {
		   this.lexer.consume(1);
		   // eat white space
                   this.lexer.SPorHT();
		   NameValue nv = nameValue();
		   parametersHeader.setParameter(nv);
		   // eat white space
                   this.lexer.SPorHT();
		}
	}

}
