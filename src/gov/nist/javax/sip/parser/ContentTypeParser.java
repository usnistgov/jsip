/*
 * ContentTypeParser.java
 *
 * Created on February 26, 2002, 2:42 PM
 */

package gov.nist.javax.sip.parser;
import gov.nist.core.*;
import gov.nist.javax.sip.header.*;
import java.text.ParseException;

/**
 * Parser for content type header.
 *
 * @version  JAIN-SIP-1.1 $Revision: 1.5 $ $Date: 2004-07-28 14:13:55 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 *<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class ContentTypeParser extends ParametersParser {

	public ContentTypeParser(String contentType) {
		super(contentType);
	}

	protected ContentTypeParser(Lexer lexer) {
		super(lexer);
	}

	public SIPHeader parse() throws ParseException {

		ContentType contentType = new ContentType();
		if (debug)
			dbg_enter("ContentTypeParser.parse");

		try {
			this.headerName(TokenTypes.CONTENT_TYPE);

			// The type:
			lexer.match(TokenTypes.ID);
			Token type = lexer.getNextToken();
			this.lexer.SPorHT();
			contentType.setContentType(type.getTokenValue());

			// The sub-type:
			lexer.match('/');
			lexer.match(TokenTypes.ID);
			Token subType = lexer.getNextToken();
			this.lexer.SPorHT();
			contentType.setContentSubType(subType.getTokenValue());
			super.parse(contentType);
			this.lexer.match('\n');
		} finally {
			if (debug)
				dbg_leave("ContentTypeParser.parse");
		}
		return contentType;

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
