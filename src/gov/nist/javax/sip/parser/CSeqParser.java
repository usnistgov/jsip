package gov.nist.javax.sip.parser;
import gov.nist.javax.sip.header.*;
import java.text.ParseException;
import javax.sip.*;
import gov.nist.core.*;

/** Parser for CSeq headers.
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  
*@author Olivier Deruelle <deruelle@nist.gov>
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public class CSeqParser extends HeaderParser {

       public   CSeqParser(String cseq) {
		super(cseq);
       }

        protected  CSeqParser(Lexer lexer) {
		super(lexer);
	}
	
	public  SIPHeader  parse() throws ParseException {
             try {
		CSeq c= new CSeq();
		
		this.lexer.match (TokenTypes.CSEQ);
		this.lexer.SPorHT();
		this.lexer.match(':');
		this.lexer.SPorHT();
                String number=this.lexer.number();
                c.setSequenceNumber(Integer.parseInt(number));
                this.lexer.SPorHT();
		String m = method();
                c.setMethod(m);
                this.lexer.SPorHT();
		this.lexer.match('\n');
                  return c;
              } 
              catch (NumberFormatException ex) {
                   Debug.printStackTrace(ex);
		   throw createParseException("Number format exception");
              } catch (InvalidArgumentException ex) {
                  Debug.printStackTrace(ex); 
                  throw createParseException(ex.getMessage());
              }
	}

/**
        public static void main(String args[]) throws ParseException {
		String cseq[] = {
			"CSeq: 17 INVITE\n",
			"CSeq: 17 ACK\n",
			"CSeq : 18   BYE\n",
                        "CSeq:1 CANCEL\n",
                        "CSeq: 3 BYE\n"
                };
			
		for (int i = 0; i < cseq.length; i++ ) {
		    CSeqParser cp = 
			  new CSeqParser(cseq[i]);
		    CSeq c = (CSeq) cp.parse();
		    System.out.println("encoded = " + c.encode());
		}
			
	}
**/
	
       

}
