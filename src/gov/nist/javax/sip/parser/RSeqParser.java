package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.*;
import java.text.ParseException;
import javax.sip.*;

/**
 * Parser for RSeq header.
 *
 * @version JAIN-SIP-1.1 $Revision: 1.4 $ $Date: 2004-01-22 13:26:31 $
 *
 * @author Olivier Deruelle <deruelle@nist.gov>  <br/>
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class RSeqParser extends HeaderParser {

	/**
	 * Creates a new instance of RSeqParser
	 * @param rseq the header to parse 
	 */
	public RSeqParser(String rseq) {
		super(rseq);
	}

	/**
	 * Constructor
	 * param lexer the lexer to use to parse the header
	 */
	protected RSeqParser(Lexer lexer) {
		super(lexer);
	}

	/**
	 * parse the String message
	 * @return SIPHeader ( RSeq object)
	 * @throws SIPParseException if the message does not respect the spec.
	 */
	public SIPHeader parse() throws ParseException {

		if (debug)
			dbg_enter("RSeqParser.parse");
		RSeq rseq = new RSeq();
		try {
			headerName(TokenTypes.RSEQ);

			rseq.setHeaderName(SIPHeaderNames.RSEQ);

			String number = this.lexer.number();
			try {
				rseq.setSequenceNumber(Integer.parseInt(number));
			} catch (InvalidArgumentException ex) {
				throw createParseException(ex.getMessage());
			}
			this.lexer.SPorHT();

			this.lexer.match('\n');

			return rseq;
		} finally {
			if (debug)
				dbg_leave("RSeqParser.parse");
		}
	}

	/** Test program
	public static void main(String args[]) throws ParseException {
	String r[] = {
	        "RSeq: 988789 \n"
	        };
		
	for (int i = 0; i < r.length; i++ ) {
	    RSeqParser parser = 
		  new RSeqParser(r[i]);
	    RSeq rs= (RSeq) parser.parse();
	    System.out.println("encoded = " + rs.encode());
	}		
	}
	 */

}
/*
 * $Log: not supported by cvs2svn $
 */
