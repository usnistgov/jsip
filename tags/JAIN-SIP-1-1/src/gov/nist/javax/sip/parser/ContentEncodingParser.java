package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.*;
import gov.nist.core.*;
import java.text.ParseException;

/**
 * Parser for ContentLanguage header.
 *
 * @version  JAIN-SIP-1.1 $Revision: 1.5 $ $Date: 2004-07-28 14:13:55 $
 *
 * @author Olivier Deruelle <deruelle@nist.gov>  <br/>
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 * @version 1.0
 */
public class ContentEncodingParser extends HeaderParser {

	/**
	 * Creates a new instance of ContentEncodingParser
	 * @param contentEncoding the header to parse 
	 */
	public ContentEncodingParser(String contentEncoding) {
		super(contentEncoding);
	}

	/**
	 * Constructor
	 * @param lexer the lexer to use to parse the header
	 */
	protected ContentEncodingParser(Lexer lexer) {
		super(lexer);
	}

	/**
	 * parse the ContentEncodingHeader String header
	 * @return SIPHeader (ContentEncodingList object)
	 * @throws SIPParseException if the message does not respect the spec.
	 */
	public SIPHeader parse() throws ParseException {

		if (debug)
			dbg_enter("ContentEncodingParser.parse");
		ContentEncodingList list = new ContentEncodingList();

		try {
			headerName(TokenTypes.CONTENT_ENCODING);

			while (lexer.lookAhead(0) != '\n') {
				ContentEncoding cl = new ContentEncoding();
				cl.setHeaderName(SIPHeaderNames.CONTENT_ENCODING);

				this.lexer.SPorHT();
				this.lexer.match(TokenTypes.ID);

				Token token = lexer.getNextToken();
				cl.setEncoding(token.getTokenValue());

				this.lexer.SPorHT();
				list.add(cl);

				while (lexer.lookAhead(0) == ',') {
					cl = new ContentEncoding();
					this.lexer.match(',');
					this.lexer.SPorHT();
					this.lexer.match(TokenTypes.ID);
					this.lexer.SPorHT();
					token = lexer.getNextToken();
					cl.setEncoding(token.getTokenValue());
					this.lexer.SPorHT();
					list.add(cl);
				}
			}

			return list;
		} catch (ParseException ex) {
			throw createParseException(ex.getMessage());
		} finally {
			if (debug)
				dbg_leave("ContentEncodingParser.parse");
		}
	}

	
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.4  2004/01/22 13:26:31  sverker
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
