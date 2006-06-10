package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.*;
import gov.nist.core.*;
import java.text.ParseException;

/** parameters parser header.
 *
 * @version  JAIN-SIP-1.1 $Revision: 1.5 $ $Date: 2004-04-22 22:51:17 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public abstract class ParametersParser extends HeaderParser {

	protected ParametersParser(Lexer lexer) {
		super((Lexer) lexer);
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

	protected void parseNameValueList(ParametersHeader parametersHeader) 
		throws ParseException{
		parametersHeader.removeParameters();
		while (true) {
		        this.lexer.SPorHT();
			NameValue nv = nameValue();
			parametersHeader.setParameter(nv.getName(), (String) nv.getValue());
			// eat white space
			this.lexer.SPorHT();
			if (lexer.lookAhead(0) != ';')  break;
			else lexer.consume(1);
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
