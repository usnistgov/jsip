package gov.nist.javax.sdp.parser;
import gov.nist.javax.sdp.fields.*;
import gov.nist.core.*;
import java.text.*;
/** Parser for key field. Ack: bug fix contributed by espen@java.net
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

            KeyField keyField = new KeyField();
            //Espen: Stealing the approach from AttributeFieldParser from from here...
            NameValue nameValue = new NameValue();

            int ptr =   this.lexer.markInputPosition();
            try {
              String name=lexer.getNextToken(':');
              this.lexer.consume(1);
              String value = lexer.getRest();
              nameValue = new NameValue(name.trim(),value.trim());
            } catch (ParseException ex) {
              this.lexer.rewindInputPosition(ptr);
              String rest = this.lexer.getRest();
              if ( rest == null)
                throw new ParseException(this.lexer.getBuffer(),
                                         this.lexer.getPtr());
              nameValue=new NameValue(rest.trim(),null);
            }
            keyField.setType(nameValue.getName());
            keyField.setKeyData((String) nameValue.getValue());
            this.lexer.SPorHT();

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
