package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.*;
import gov.nist.core.*;
import java.text.ParseException;

/**
 * Parser for ContentLanguage header.
 *
 * @version  JAIN-SIP-1.1 $Revision: 1.4 $ $Date: 2004-01-22 13:26:31 $
 *
 * @author Olivier Deruelle <deruelle@nist.gov>  <br/>
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class ContentDispositionParser extends ParametersParser {

	/**
	 * Creates a new instance of ContentDispositionParser
	 * @param contentDisposition the header to parse 
	 */
	public ContentDispositionParser(String contentDisposition) {
		super(contentDisposition);
	}

	/**
	 * Constructor
	 * @param lexer the lexer to use to parse the header
	 */
	protected ContentDispositionParser(Lexer lexer) {
		super(lexer);
	}

	/**
	 * parse the ContentDispositionHeader String header
	 * @return SIPHeader (ContentDispositionList object)
	 * @throws SIPParseException if the message does not respect the spec.
	 */
	public SIPHeader parse() throws ParseException {

		if (debug)
			dbg_enter("ContentDispositionParser.parse");

		try {
			headerName(TokenTypes.CONTENT_DISPOSITION);

			ContentDisposition cd = new ContentDisposition();
			cd.setHeaderName(SIPHeaderNames.CONTENT_DISPOSITION);

			this.lexer.SPorHT();
			this.lexer.match(TokenTypes.ID);

			Token token = lexer.getNextToken();
			cd.setDispositionType(token.getTokenValue());
			this.lexer.SPorHT();
			super.parse(cd);

			this.lexer.SPorHT();
			this.lexer.match('\n');

			return cd;
		} catch (ParseException ex) {
			throw createParseException(ex.getMessage());
		} finally {
			if (debug)
				dbg_leave("ContentDispositionParser.parse");
		}
	}

	/** Test program
	public static void main(String args[]) throws ParseException {
	    String r[] = {
	        "Content-Disposition: session\n",
	        "Content-Disposition: render;handling=hand;optional=opt \n"
	    };
	    
	    for (int i = 0; i < r.length; i++ ) {
	        ContentDispositionParser parser =
	        new ContentDispositionParser(r[i]);
	        ContentDisposition cd= (ContentDisposition) parser.parse();
	        System.out.println("encoded = " + cd.encode());
	    }    
	}
	*/
}
/*
 * $Log: not supported by cvs2svn $
 */
