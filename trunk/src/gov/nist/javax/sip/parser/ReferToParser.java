
package gov.nist.javax.sip.parser;
import java.text.ParseException;
import gov.nist.javax.sip.address.*;
import gov.nist.javax.sip.header.*;
import gov.nist.core.*;

/** To Header parser.
*
*@version  JAIN-SIP-1.1
*
*@author Olivier Deruelle <deruelle@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public class ReferToParser extends AddressParametersParser {
    
    /** Creates new ToParser
     * @param String to set
     */
    public ReferToParser(String referTo) {
        super(referTo);
    }
    
    protected ReferToParser(Lexer lexer) {
        super(lexer);
    }
    public SIPHeader parse() throws ParseException {
           
	headerName(TokenTypes.REFER_TO);
	ReferTo referTo = new ReferTo();
	super.parse(referTo);
	this.lexer.match('\n');
        return referTo;
   }
    
    
    public static void main(String args[]) throws ParseException {
        String to[] = {
           "Refer-To: <sip:+1-650-555-2222@ss1.wcom.com;user=phone>;tag=5617\n",
           "Refer-To: T. A. Watson <sip:watson@bell-telephone.com>\n",
           "Refer-To: LittleGuy <sip:UserB@there.com>\n",
           "Refer-To: sip:mranga@120.6.55.9\n",
           "Refer-To: sip:mranga@129.6.55.9 ; tag=696928473514.129.6.55.9\n"
        };
        
        for (int i = 0; i < to.length; i++ ) {
            ReferToParser tp =
            new ReferToParser(to[i]);
            ReferTo t = (ReferTo) tp.parse();
            System.out.println("encoded = " + t.encode());
        }
        
    }
    
    
    
}
