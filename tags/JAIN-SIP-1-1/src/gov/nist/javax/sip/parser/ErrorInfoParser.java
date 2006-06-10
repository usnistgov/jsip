package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.*;
import gov.nist.javax.sip.address.*;
import java.text.ParseException;

/**
 * Parser for ErrorInfo header.
 *
 * @version  JAIN-SIP-1.1 $Revision: 1.5 $ $Date: 2004-08-10 21:35:43 $
 *
 * @author Olivier Deruelle <deruelle@nist.gov>  <br/>
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class ErrorInfoParser extends ParametersParser {

	/**
	 * Creates a new instance of ErrorInfoParser
	 * @param errorInfo the header to parse 
	 */
	public ErrorInfoParser(String errorInfo) {
		super(errorInfo);
	}

	/**
	 * Constructor
	 * @param lexer the lexer to use to parse the header
	 */
	protected ErrorInfoParser(Lexer lexer) {
		super(lexer);
	}

	/**
	 * parse the ErrorInfo String header
	 * @return SIPHeader (ErrorInfoList object)
	 * @throws SIPParseException if the message does not respect the spec.
	 */
	public SIPHeader parse() throws ParseException {

		if (debug)
			dbg_enter("ErrorInfoParser.parse");
		ErrorInfoList list = new ErrorInfoList();

		try {
			headerName(TokenTypes.ERROR_INFO);

			while (lexer.lookAhead(0) != '\n') {
				ErrorInfo errorInfo = new ErrorInfo();
				errorInfo.setHeaderName(SIPHeaderNames.ERROR_INFO);

				this.lexer.SPorHT();
				this.lexer.match('<');
				URLParser urlParser = new URLParser((Lexer) this.lexer);
				GenericURI uri = urlParser.uriReference();
				errorInfo.setErrorInfo(uri);
				this.lexer.match('>');
				this.lexer.SPorHT();

				super.parse(errorInfo);
				list.add(errorInfo);

				while (lexer.lookAhead(0) == ',') {
					this.lexer.match(',');
					this.lexer.SPorHT();

					errorInfo = new ErrorInfo();

					this.lexer.SPorHT();
					this.lexer.match('<');
					urlParser = new URLParser((Lexer) this.lexer);
					uri = urlParser.uriReference();
					errorInfo.setErrorInfo(uri);
					this.lexer.match('>');
					this.lexer.SPorHT();

					super.parse(errorInfo);
					list.add(errorInfo);
				}
			}

			return list;
		} finally {
			if (debug)
				dbg_leave("ErrorInfoParser.parse");
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
