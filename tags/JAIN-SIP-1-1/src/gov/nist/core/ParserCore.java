package gov.nist.core;

import java.text.ParseException;

/** Generic parser class.
* All parsers inherit this class.
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public abstract class ParserCore {
	public static final boolean debug = Debug.parserDebug;

	protected static int nesting_level;

	protected LexerCore lexer;

	
	protected NameValue nameValue(char separator) throws ParseException  {
		if (debug) dbg_enter("nameValue");
		try {
               
		lexer.match(LexerCore.ID);
		Token name = lexer.getNextToken();
		// eat white space.
		lexer.SPorHT();
		try {
         
                    
		        boolean quoted = false;

			char la = lexer.lookAhead(0);
                       
			if (la == separator ) {
				lexer.consume(1);
				lexer.SPorHT();
				String str = null;
				if (lexer.lookAhead(0) == '\"')  {
					 str = lexer.quotedString();
					  quoted = true;
				} else {
				   lexer.match(LexerCore.ID);
				   Token value = lexer.getNextToken();
				   str = value.tokenValue;
				}
				NameValue nv = 
				new NameValue(name.tokenValue,str);
				if (quoted) nv.setQuotedValue();
				return nv;
			}  else {
				return new NameValue(name.tokenValue,null);
			}
		} catch (ParseException ex) {
			return new NameValue(name.tokenValue,null);
		}

		} finally {
			if (debug) dbg_leave("nameValue");
		}


	}

	protected  void dbg_enter(String rule) {
		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < nesting_level ; i++) 
			stringBuffer.append(">");
		    
		if (debug)  {
			System.out.println(
				stringBuffer + rule + 
				"\nlexer buffer = \n" + 
				lexer.getRest());
		}
		nesting_level++;
	}

	protected void dbg_leave(String rule) {
		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < nesting_level ; i++) 
			stringBuffer.append("<");
		    
		if (debug)  {
			System.out.println(
				stringBuffer +
				rule + 
				"\nlexer buffer = \n" + 
				lexer.getRest());
		}
		nesting_level --;
	}
	
	protected NameValue nameValue() throws ParseException  {
		return nameValue('=');
	}
	
	protected void peekLine(String rule) {
		if (debug) {
			Debug.println(rule +" " + lexer.peekLine());
		}
	}
}


/*
 * $Log: not supported by cvs2svn $
 */
