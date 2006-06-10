package gov.nist.javax.sip.parser;
import gov.nist.javax.sip.header.*;
import gov.nist.core.*;
import java.text.ParseException;
import javax.sip.*;

/**
 * Accept-Encoding SIP (HTTP) Header parser.
 *
 * @version  JAIN-SIP-1.1 $Revision: 1.4 $ $Date: 2004-01-22 13:26:31 $
 *
 * @author Olivier Deruelle <deruelle@nist.gov>  
 * @author M. Ranganathan <mranga@nist.gov> 
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 * <pre>
 *
 *   The Accept-Encoding request-header field is similar to Accept, but
 *   restricts the content-codings (section 3.5) that are acceptable in
 *   the response.
 *
 * 
 *       Accept-Encoding  = "Accept-Encoding" ":"
 *                      ( encoding *( "," encoding) )
 *       encoding         = ( codings *[ ";" "q" "=" qvalue ] )
 *       codings          = ( content-coding | "*" )
 * 
 *   Examples of its use are:
 * 
 *       Accept-Encoding: compress, gzip
 *       Accept-Encoding:
 *       Accept-Encoding: *
 *       Accept-Encoding: compress;q=0.5, gzip;q=1.0
 *       Accept-Encoding: gzip;q=1.0, identity; q=0.5, *;q=0
 * </pre>
 * 
 */
public class AcceptEncodingParser extends HeaderParser {

	/**
	 * Constructor
	 * @param acceptEncoding message to parse
	 */
	public AcceptEncodingParser(String acceptEncoding) {
		super(acceptEncoding);
	}

	/**
	 * Constructor
	 * @param lexer Lexer to set
	 */
	protected AcceptEncodingParser(Lexer lexer) {
		super(lexer);
	}

	/**
	 * parse the String message
	 * @return SIPHeader (AcceptEncoding object)
	 * @throws ParseException if the message does not respect the spec.
	 */
	public SIPHeader parse() throws ParseException {
		AcceptEncodingList acceptEncodingList = new AcceptEncodingList();
		if (debug)
			dbg_enter("AcceptEncodingParser.parse");

		try {
			headerName(TokenTypes.ACCEPT_ENCODING);
			// empty body is fine for this header.
			if (lexer.lookAhead(0) == '\n') {
				AcceptEncoding acceptEncoding = new AcceptEncoding();
				acceptEncodingList.add(acceptEncoding);
			} else {
				while (lexer.lookAhead(0) != '\n') {
					AcceptEncoding acceptEncoding = new AcceptEncoding();
					if (lexer.lookAhead(0) != ';') {
						// Content-Coding:
						lexer.match(TokenTypes.ID);
						Token value = lexer.getNextToken();
						acceptEncoding.setEncoding(value.getTokenValue());
					}

					while (lexer.lookAhead(0) == ';') {
						this.lexer.match(';');
						this.lexer.SPorHT();
						this.lexer.match('q');
						this.lexer.SPorHT();
						this.lexer.match('=');
						this.lexer.SPorHT();
						lexer.match(TokenTypes.ID);
						Token value = lexer.getNextToken();
						try {
							float qv = Float.parseFloat(value.getTokenValue());
							acceptEncoding.setQValue(qv);
						} catch (NumberFormatException ex) {
							throw createParseException(ex.getMessage());
						} catch (InvalidArgumentException ex) {
							throw createParseException(ex.getMessage());
						}
						this.lexer.SPorHT();
					}

					acceptEncodingList.add(acceptEncoding);
					if (lexer.lookAhead(0) == ',') {
						this.lexer.match(',');
						this.lexer.SPorHT();
					}

				}
			}
			return acceptEncodingList;
		} finally {
			if (debug)
				dbg_leave("AcceptEncodingParser.parse");
		}
	}
}
/*
 * $Log: not supported by cvs2svn $
 */
