package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.*;
import java.text.ParseException;
import javax.sip.*;
import javax.sip.message.Request;

import gov.nist.core.*;

/**
 * Parser for CSeq headers.
 * 
 * @version JAIN-SIP-1.1 $Revision: 1.6 $ $Date: 2005-04-21 00:01:59 $
 * 
 * @author M. Ranganathan <mranga@nist.gov>
 * @author Olivier Deruelle <deruelle@nist.gov><a href=" {@docRoot}
 *         /uncopyright.html">This code is in the public domain. </a>
 *  
 */
public class CSeqParser extends HeaderParser {

    public CSeqParser(String cseq) {
        super(cseq);
    }

    protected CSeqParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        try {
            CSeq c = new CSeq();

            this.lexer.match(TokenTypes.CSEQ);
            this.lexer.SPorHT();
            this.lexer.match(':');
            this.lexer.SPorHT();
            String number = this.lexer.number();
            c.setSequenceNumber(Integer.parseInt(number));
            this.lexer.SPorHT();
            String m = method();
            
            // Use the manifest constant to speed up equality checks.
            if ( m.equalsIgnoreCase(Request.INVITE)) m = Request.INVITE;
            else if ( m.equalsIgnoreCase(Request.ACK)) m = Request.ACK;
            else if ( m.equalsIgnoreCase(Request.BYE)) m = Request.BYE;
            else if ( m.equalsIgnoreCase(Request.INFO)) m = Request.INFO;
            else if ( m.equalsIgnoreCase(Request.NOTIFY)) m = Request.NOTIFY;
            else if ( m.equalsIgnoreCase(Request.PRACK)) m = Request.PRACK;
            else if ( m.equalsIgnoreCase(Request.CANCEL)) m = Request.CANCEL;
            
            c.setMethod(m);
            this.lexer.SPorHT();
            this.lexer.match('\n');
            return c;
        } catch (NumberFormatException ex) {
            Debug.printStackTrace(ex);
            throw createParseException("Number format exception");
        } catch (InvalidArgumentException ex) {
            Debug.printStackTrace(ex);
            throw createParseException(ex.getMessage());
        }
    }

    /**
     *  
     */
}
/*
 * $Log: not supported by cvs2svn $ Revision 1.5 2004/07/28 14:13:54 mranga Submitted
 * by: mranga
 * 
 * Move out the test code to a separate test/unit class. Fixed some encode
 * methods.
 * 
 * Revision 1.4 2004/01/22 13:26:31 sverker Issue number: Obtained from:
 * Submitted by: sverker Reviewed by: mranga
 * 
 * Major reformat of code to conform with style guide. Resolved compiler and
 * javadoc warnings. Added CVS tags.
 * 
 * CVS: ----------------------------------------------------------------------
 * CVS: Issue number: CVS: If this change addresses one or more issues, CVS:
 * then enter the issue number(s) here. CVS: Obtained from: CVS: If this change
 * has been taken from another system, CVS: then name the system in this line,
 * otherwise delete it. CVS: Submitted by: CVS: If this code has been
 * contributed to the project by someone else; i.e., CVS: they sent us a patch
 * or a set of diffs, then include their name/email CVS: address here. If this
 * is your work then delete this line. CVS: Reviewed by: CVS: If we are doing
 * pre-commit code reviews and someone else has CVS: reviewed your changes,
 * include their name(s) here. CVS: If you have not had it reviewed then delete
 * this line.
 *  
 */
