package gov.nist.javax.sdp.parser;

import gov.nist.javax.sdp.fields.*;
import gov.nist.core.*;
import java.text.ParseException;

/** Parser for Repeat field.
*
*@version  JAIN-SIP-1.1
*
*@author Olivier Deruelle <deruelle@nist.gov>
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
 */
public class RepeatFieldParser extends SDPParser {

	/** Creates new RepeatFieldsParser */
	public RepeatFieldParser(String repeatField) {
		lexer = new Lexer("charLexer", repeatField);
	}

	/** Get the typed time
	 * @param String tokenValue to set
	 * @return TypedTime
	 */
	public TypedTime getTypedTime(String tokenValue) {
		TypedTime typedTime = new TypedTime();

		if (tokenValue.endsWith("d")) {
			typedTime.setUnit("d");
			String t = tokenValue.replace('d', ' ');

			typedTime.setTime(Integer.parseInt(t.trim()));
		} else if (tokenValue.endsWith("h")) {
			typedTime.setUnit("h");
			String t = tokenValue.replace('h', ' ');
			typedTime.setTime(Integer.parseInt(t.trim()));
		} else if (tokenValue.endsWith("m")) {
			typedTime.setUnit("m");
			String t = tokenValue.replace('m', ' ');
			typedTime.setTime(Integer.parseInt(t.trim()));
		} else {
			typedTime.setUnit("s");
			if (tokenValue.endsWith("s")) {
				String t = tokenValue.replace('s', ' ');
				typedTime.setTime(Integer.parseInt(t.trim()));
			} else
				typedTime.setTime(Integer.parseInt(tokenValue.trim()));
		}
		return typedTime;
	}

	/** parse the field string
	 * @return RepeatFields 
	 */
	public RepeatField repeatField() throws ParseException {
		try {

			this.lexer.match('r');
			this.lexer.SPorHT();
			this.lexer.match('=');
			this.lexer.SPorHT();

			RepeatField repeatField = new RepeatField();

			lexer.match(LexerCore.ID);
			Token repeatInterval = lexer.getNextToken();
			this.lexer.SPorHT();
			TypedTime typedTime = getTypedTime(repeatInterval.getTokenValue());
			repeatField.setRepeatInterval(typedTime);

			lexer.match(LexerCore.ID);
			Token activeDuration = lexer.getNextToken();
			this.lexer.SPorHT();
			typedTime = getTypedTime(activeDuration.getTokenValue());
			repeatField.setActiveDuration(typedTime);

			// The offsets list:
			while (lexer.lookAhead(0) != '\n') {
				lexer.match(LexerCore.ID);
				Token offsets = lexer.getNextToken();
				this.lexer.SPorHT();
				typedTime = getTypedTime(offsets.getTokenValue());
				repeatField.addOffset(typedTime);
			}

			return repeatField;
		} catch (Exception e) {
			throw lexer.createParseException();
		}
	}

	public SDPField parse() throws ParseException {
		return this.repeatField();
	}

	/**
	    
	    public static void main(String[] args) throws ParseException {
	        String repeat[] = {
	                        "r=604800s 3600s 0s 90000s\n",
				"r=7d 1h 0 25h\n",
	                        "r=7 6 5 4 3 2 1 0 \n" 
	                };
	
		    for (int i = 0; i < repeat.length; i++) {
		        RepeatFieldParser repeatFieldParser=new RepeatFieldParser(
	                repeat[i] );
		        RepeatField repeatFields=repeatFieldParser.repeatField();
			System.out.println("toParse: " +repeat[i]);
			System.out.println("encoded: " +repeatFields.encode());
		    }
	
		}
	**/
}
/*
 * $Log: not supported by cvs2svn $
 */
