package gov.nist.javax.sip.parser;
import gov.nist.core.*;
import java.text.ParseException;
import java.util.Vector;

/** Base parser class.
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/

public abstract class Parser extends ParserCore implements TokenTypes {

	protected  ParseException 
		createParseException(String exceptionString) {
		return new ParseException
		(lexer.getBuffer() + ":" + exceptionString, lexer.getPtr());
	}
		

	protected Lexer getLexer() {
		return (Lexer) this.lexer;
	}

	protected String sipVersion() throws ParseException {
		if (debug) dbg_enter("sipVersion" );
		try {
	        Token tok = lexer.match(SIP);
		if (! tok.getTokenValue().equals("SIP") ) 
			createParseException("Expecting SIP");
		lexer.match('/');
		tok = lexer.match(ID);
		if (! tok.getTokenValue().equals("2.0") ) 
			createParseException("Expecting SIP/2.0");

		return "SIP/2.0";
		} finally {
			if (debug) dbg_leave("sipVersion");
		}
	}

	/** parses a method. Consumes if a valid method has been found.
	*/
	protected String method() throws ParseException {
		try {
		if (debug) dbg_enter("method" );
		Vector tokens = this.lexer.peekNextToken(1);
		Token token = (Token) tokens.elementAt(0);
		if (token.getTokenType() == INVITE 	 ||
		    token.getTokenType() == ACK 	 ||
		    token.getTokenType() == OPTIONS 	 ||
		    token.getTokenType() == BYE 	 ||
		    token.getTokenType() == REGISTER	 ||
                    token.getTokenType() == CANCEL 	 ||
                    token.getTokenType() == SUBSCRIBE ||
                    token.getTokenType() == NOTIFY 	 ||
		    token.getTokenType() == ID ) {
		    lexer.consume();
		    return token.getTokenValue();
		} else {
		   throw createParseException
		   ("Invalid Method" );
		}
		} finally {
		      if (Debug.debug) dbg_leave("method");
		}
	}

}
