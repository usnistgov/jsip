/*
 * InformationFieldParser.java
 *
 * Created on February 19, 2002, 5:28 PM
 */
package gov.nist.javax.sdp.parser;

import gov.nist.javax.sdp.fields.*;
import java.text.*;

/**
 * @author  deruelle
 * @version JAIN-SDP-PUBLIC-RELEASE $Revision: 1.2 $ $Date: 2004-01-22 13:26:28 $
 */
public class InformationFieldParser extends SDPParser {

	/** Creates new InformationFieldParser */
	public InformationFieldParser(String informationField) {
		this.lexer = new Lexer("charLexer", informationField);
	}

	public InformationField informationField() throws ParseException {
		try {
			this.lexer.match('i');
			this.lexer.SPorHT();
			this.lexer.match('=');
			this.lexer.SPorHT();

			InformationField informationField = new InformationField();
			String rest = lexer.getRest();
			informationField.setInformation(rest.trim());

			return informationField;
		} catch (Exception e) {
			throw new ParseException(lexer.getBuffer(), lexer.getPtr());
		}
	}

	public SDPField parse() throws ParseException {
		return this.informationField();
	}

	/**
	    public static void main(String[] args) throws ParseException {
		    String information[] = {
				"i=A Seminar on the session description protocol\n"
	                };
	
		    for (int i = 0; i < information.length; i++) {
		       InformationFieldParser informationFieldParser=new InformationFieldParser(
	                information[i] );
			InformationField informationField=
	                    informationFieldParser.informationField();
	                
			System.out.println("encoded: " +informationField.encode());
		    }
	
		}
	**/
}
/*
 * $Log: not supported by cvs2svn $
 */
