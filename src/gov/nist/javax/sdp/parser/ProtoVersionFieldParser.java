package gov.nist.javax.sdp.parser;

import gov.nist.javax.sdp.fields.*;
import gov.nist.core.*;
import java.text.*;

/**
 * Parser for Proto Version.
 *
 * @version JAIN-SDP-PUBLIC-RELEASE $Revision: 1.2 $ $Date: 2004-01-22 13:26:28 $
 *
 * @author Olivier Deruelle <deruelle@antd.nist.gov> 
 * @author M. Ranganathan <mranga@nist.gov>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class ProtoVersionFieldParser extends SDPParser {

	/** Creates new ProtoVersionFieldParser */
	public ProtoVersionFieldParser(String protoVersionField) {
		this.lexer = new Lexer("charLexer", protoVersionField);
	}

	public ProtoVersionField protoVersionField() throws ParseException {
		try {
			this.lexer.match('v');
			this.lexer.SPorHT();
			this.lexer.match('=');
			this.lexer.SPorHT();

			ProtoVersionField protoVersionField = new ProtoVersionField();
			lexer.match(Lexer.ID);
			Token version = lexer.getNextToken();
			protoVersionField.setProtoVersion(
				Integer.parseInt(version.getTokenValue()));
			this.lexer.SPorHT();

			return protoVersionField;
		} catch (Exception e) {
			throw lexer.createParseException();
		}
	}

	public SDPField parse() throws ParseException {
		return this.protoVersionField();
	}

	/**
	    public static void main(String[] args) throws ParseException {
		    String protoVersion[] = {
				"v=0\n"
	                };
	
		    for (int i = 0; i < protoVersion.length; i++) {
		        ProtoVersionFieldParser protoVersionFieldParser=
					new ProtoVersionFieldParser(
	                protoVersion[i] );
			ProtoVersionField protoVersionField =
				protoVersionFieldParser.protoVersionField();
			System.out.println
				("encoded: " +protoVersionField.encode());
		    }
	
		}
	**/

}
/*
 * $Log: not supported by cvs2svn $
 */
