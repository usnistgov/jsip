
package gov.nist.javax.sdp.parser;
import gov.nist.javax.sdp.fields.*;
import gov.nist.core.*;
import java.text.*;

/**
 *
 * @author  deruelle
 * @version 1.0
 */
public class BandwidthFieldParser extends SDPParser {

     /** Creates new BandwidthFieldParser */
    public BandwidthFieldParser(String bandwidthField) {
	this.lexer = new Lexer("charLexer",bandwidthField);
    }
    
    public BandwidthField bandwidthField() throws ParseException  {
        try{
            this.lexer.match ('b');
            this.lexer.SPorHT();
            this.lexer.match('=');
            this.lexer.SPorHT();
            
            BandwidthField bandwidthField=new BandwidthField();
            
                NameValue nameValue=nameValue(':');
                String name=nameValue.getName();
                String value= (String) nameValue.getValue();
                
                bandwidthField.setBandwidth(Integer.parseInt(value.trim()) );
                bandwidthField.setBwtype(name);
                
                this.lexer.SPorHT();
                return bandwidthField;
            }
            catch(Exception e) {
		e.printStackTrace();
                throw new ParseException(lexer.getBuffer(),lexer.getPtr());
            }  
    }

    public SDPField parse() throws ParseException { 
	return this.bandwidthField();
    }
    
/**
    public static void main(String[] args) throws ParseException {
	    String bandwidth[] = {
			"b=X-YZ:128\n",
			"b=CT: 31\n",
			"b=AS:0 \n",
                        "b= AS:4\n"
                };

	    for (int i = 0; i < bandwidth.length; i++) {
	        BandwidthFieldParser bandwidthFieldParser=new BandwidthFieldParser(
                bandwidth[i] );
		System.out.println("toParse: " + bandwidth[i]);
		BandwidthField bandwidthField = bandwidthFieldParser.bandwidthField();
		System.out.println("encoded: " + bandwidthField.encode());
	    }

	}
**/


}
