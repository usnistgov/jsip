
package gov.nist.javax.sdp.parser;
import  gov.nist.core.*;

public class Lexer extends LexerCore {
    public Lexer(String lexerName, String buffer) {
	super(lexerName,buffer);

    }

    

    public void selectLexer(String lexerName) {}

    public static String getFieldName(String line) {
	int i = line.indexOf("=");
	if (i == -1 ) return null;
	else return  line.substring(0,i);
    }


}

