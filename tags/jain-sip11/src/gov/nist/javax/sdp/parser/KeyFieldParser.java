package gov.nist.javax.sdp.parser;
import gov.nist.javax.sdp.fields.*;
import gov.nist.core.*;
import java.text.*;
/**
 *
 * @author  deruelle
 * @version 1.0
 */
public class KeyFieldParser extends SDPParser {

     /** Creates new KeyFieldParser */
    public KeyFieldParser(String keyField) {
	this.lexer = new Lexer("charLexer",keyField);
    }
    
 
    
    public KeyField keyField() throws ParseException  {
        try{
            this.lexer.match ('k');
            this.lexer.SPorHT();
            this.lexer.match('=');
            this.lexer.SPorHT();
            
            KeyField keyField=new KeyField();
            NameValue nameValue= nameValue();
            String name=nameValue.getName();
            String value=  (String) nameValue.getValue();
            
            keyField.setType(name);
            
            keyField.setKeyData(value);
            
            return keyField;
        }
        catch(Exception e) {
            throw new ParseException(lexer.getBuffer(),lexer.getPtr());
        }  
        
    }

    public SDPField parse() throws ParseException {
	return this.keyField();
    }
    
/**
    public static void main(String[] args) throws ParseException {
	    String key[] = {
			"k=clear:1234124\n",
                        "k=base64:12\n",
                        "k=http://www.cs.ucl.ac.uk/staff/M.Handley/sdp.03.ps\n",
                        "k=prompt\n"
                };

	    for (int i = 0; i < key.length; i++) {
	       KeyFieldParser keyFieldParser=new KeyFieldParser(
                key[i] );
		KeyField keyField=keyFieldParser.keyField();
		System.out.println("toParse: " +key[i]);
		System.out.println("encoded: " +keyField.encode());
	    }

	}
**/



}
