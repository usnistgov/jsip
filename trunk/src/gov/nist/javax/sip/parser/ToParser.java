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
public class ToParser extends AddressParametersParser {
    
    /** Creates new ToParser
     * @param String to set
     */
    public ToParser(String to) {
        super(to);
    }
    
    protected ToParser(Lexer lexer) {
        super(lexer);
    }
    public SIPHeader parse() throws ParseException {
           
	headerName(TokenTypes.TO);
	To to = new To();
	super.parse(to);
	this.lexer.match('\n');
	if (((AddressImpl)to.getAddress()).getAddressType() ==
		AddressImpl.ADDRESS_SPEC) {
		// the parameters are header parameters.
		if (to.getAddress().getURI() instanceof SipUri) {
		   SipUri sipUri = (SipUri) to.getAddress().getURI();
		   NameValueList parms = sipUri.getUriParms();
		   if (parms != null && !parms.isEmpty()) {
		      to.setParameters(parms);
		      sipUri.removeUriParms();
		   }
		}
	}
        return to;
   }
    
    
/**
    public static void main(String args[]) throws ParseException {
        String to[] = {
           "To: <sip:+1-650-555-2222@ss1.wcom.com;user=phone>;tag=5617\n",
           "To: T. A. Watson <sip:watson@bell-telephone.com>\n",
           "To: LittleGuy <sip:UserB@there.com>\n",
           "To: sip:mranga@120.6.55.9\n",
           "To: sip:mranga@129.6.55.9 ; tag=696928473514.129.6.55.9\n"
        };
        
        for (int i = 0; i < to.length; i++ ) {
	    System.out.println("toParse = " + to[i]);
            ToParser tp =
            new ToParser(to[i]);
            To t = (To) tp.parse();
            System.out.println("encoded = " + t.encode());
        }
        
    }
**/
    
    
    
}
