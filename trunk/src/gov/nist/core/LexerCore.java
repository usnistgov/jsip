package gov.nist.core;
import java.util.Hashtable;
import java.util.Vector;
import java.text.ParseException;

/** A lexical analyzer that is used by all parsers in our implementation.
 *
 *
 *@author M. Ranganathan <mranga@nist.gov>
 */
public class LexerCore extends StringTokenizer  {
    
    
    // IMPORTANT - All keyword matches should be between START and END
    public  	static final int START  = 2048;
    public 	static final int END   = START + 2048;
    // IMPORTANT -- This should be < END
    public	static final int ID    = END - 1;
    // Individial token classes.
    public 	static final int WHITESPACE = END + 1;
    public 	static final int DIGIT = END + 2;
    public 	static final int ALPHA = END + 3;
    public	static final int 	BACKSLASH   = (int) '\\'	;
    public	static final int     	QUOTE	    = (int) '\''	;
    public	static final int     	AT          = (int) '@'		;
    public	static final int     	SP	    = (int) ' '		;
    public	static final int     	HT	    = (int) '\t'	;
    public	static final int     	COLON	    = (int) ':'		;
    public	static final int     	STAR	    = (int) '*'		;
    public	static final int     	DOLLAR      = (int) '$'		;
    public	static final int    	PLUS        = (int) '+'		;
    public	static final int     	POUND	    = (int) '#'		;
    public	static final int    	MINUS	    = (int) '-'		;
    public	static final int    	DOUBLEQUOTE = (int) '\"'	;
    public	static final int    	TILDE	    = (int) '~'		;
    public	static final int    	BACK_QUOTE  	 = (int) '`'	;
    public	static final int    	NULL	    	 = (int) '\0'	;
    public	static final int    	EQUALS	    	 = (int) '='	;
    public	static final int    	SEMICOLON   	 = (int) ';'	;
    public	static final int    	SLASH       	 = (int) '/'	;
    public	static final int        L_SQUARE_BRACKET = (int) '['	;
    public	static final int        R_SQUARE_BRACKET = (int) ']'	;
    public	static final int        R_CURLY		 = (int) '}'	;
    public	static final int        L_CURLY          = (int) '{'	;
    public	static final int    	HAT		 = (int) '^'    ;
    public	static final int        BAR		 = (int) '|'    ;
    public	static final int        DOT		 = (int) '.'    ;
    public	static final int        EXCLAMATION	 = (int) '!'    ;
    public	static final int     	LPAREN		 = (int) '('    ;
    public	static final int        RPAREN		 = (int)  ')'   ;
    public	static final int    	GREATER_THAN	 = (int) '>'    ;
    public	static final int        LESS_THAN	 = (int) '<'    ;
    public	static final int        PERCENT		 = (int) '%'	;
    public	static final int        QUESTION	 = (int) '?'	;
    public	static final int     	AND  		 = (int) '&'	;
    public	static final int    	UNDERSCORE  	 = (int) '_'	;
    
    protected static Hashtable globalSymbolTable;
    protected static Hashtable lexerTables;
    protected Hashtable currentLexer;
    protected String    currentLexerName;
    protected Token     currentMatch;
    
    static{
        globalSymbolTable  = new Hashtable();
        lexerTables = new Hashtable();
    }
    
    protected void addKeyword(String name, int value) {
	// System.out.println("addKeyword " + name + " value = " + value);
	// new Exception().printStackTrace();
        Integer val = new Integer(value);
        currentLexer.put(name, val);
        if (! globalSymbolTable.containsKey(val))
            globalSymbolTable.put(val,name);
    }
    
    public String lookupToken(int value) {
        if (value > START) {
            return (String) globalSymbolTable.get(new Integer(value));
        } else {
            Character ch = new Character((char)value);
            return ch.toString();
        }
    }
    
    protected Hashtable addLexer(String lexerName) {
        currentLexer = (Hashtable) lexerTables.get(lexerName);
        if (currentLexer == null) {
            currentLexer = new Hashtable();
            lexerTables.put(lexerName,currentLexer);
        }
        return currentLexer;
    }
    
    
    //public abstract void selectLexer(String lexerName);

    public void selectLexer(String lexerName) {
		this.currentLexerName = lexerName;
    }
    
    
    protected LexerCore() {
        this.currentLexer = new Hashtable();
        this.currentLexerName = "charLexer";
    }
    
    
    public LexerCore(String lexerName) {
	this();
        this.currentLexerName = lexerName;
    }
    
    
    /** Initialize the lexer with a buffer.
     */
    public LexerCore(String lexerName, String buffer) {
        this(lexerName);
        this.buffer =  buffer;
    }
    
    /** Peek the next id but dont move the buffer pointer forward.
     */
    
    public String peekNextId() {
        int oldPtr = ptr;
        String retval = ttoken();
        savedPtr = ptr;
        ptr = oldPtr;
        return retval;
    }
    
    
    /** Get the next id.
     */
    public String getNextId() { return ttoken(); }
    
    // call this after you call match
    public Token getNextToken() {
        return this.currentMatch;
        
    }
    
    /** Look ahead for one token.
     */
    public Token peekNextToken() throws ParseException {
        return (Token) peekNextToken(1).elementAt(0);
    }
    
    
    public Vector peekNextToken(int ntokens) throws ParseException {
        int old = ptr;
        Vector retval = new Vector();
        for (int i = 0; i < ntokens; i++) {
            Token tok = new Token();
            if (startsId()) {
                String id = ttoken();
                tok.tokenValue = id;
                if (currentLexer.containsKey(id.toUpperCase())) {
                    Integer type = (Integer) currentLexer.get(id.toUpperCase());
                    tok.tokenType  = type.intValue();
                } else tok.tokenType = ID;
            } else {
                char nextChar = getNextChar();
                tok.tokenValue =
                new StringBuffer().append(nextChar).toString();
                if ( isAlpha(nextChar)) {
                    tok.tokenType = ALPHA ;
                } else if (isDigit(nextChar)) {
                    tok.tokenType =  DIGIT ;
                } else tok.tokenType = (int) nextChar;
            }
            retval.addElement(tok);
        }
        savedPtr = ptr;
        ptr = old;
        return retval;
    }
    
    /** Match the given token or throw an exception if no such token
     * can be matched.
     */
    public Token  match(int tok)  throws ParseException {
        if (Debug.parserDebug) {
            Debug.println("match " + tok);
        }
        if (tok > START && tok < END ) {
            if (tok == ID) {
                // Generic ID sought.
                if (!startsId())
                    throw new ParseException(buffer + "\nID expected",ptr);
                String id = getNextId();
                this.currentMatch = new Token();
                this.currentMatch.tokenValue = id;
                this.currentMatch.tokenType = ID;
            } else  {
                String nexttok = getNextId();
                Integer cur =
                (Integer) currentLexer.get(nexttok.toUpperCase());
                
                if (cur == null || cur.intValue() != tok)
                    throw new ParseException
                    (buffer + "\nUnexpected Token : "+
                    nexttok,ptr);
                this.currentMatch = new Token();
                this.currentMatch.tokenValue = nexttok;
                this.currentMatch.tokenType = tok;
            }
        } else if (tok > END ) {
            // Character classes.
            char next = lookAhead(0);
            if (tok == DIGIT) {
                if (! isDigit(next))
                    throw new
                    ParseException(buffer + "\nExpecting DIGIT",ptr);
                this.currentMatch = new Token();
                this.currentMatch.tokenValue =
                new StringBuffer().append(next).toString();
                this.currentMatch.tokenType = tok;
                consume(1);
                
            } else if (tok == ALPHA) {
                if (! isAlpha(next))
                    throw new ParseException
                    (buffer + "\nExpecting ALPHA",ptr);
                this.currentMatch = new Token();
                this.currentMatch.tokenValue =
                new StringBuffer().append(next).toString();
                this.currentMatch.tokenType = tok;
                consume(1);
                
            }
            
        } else {
            // This is a direct character spec.
            Character ch = new Character((char)tok);
            char next = lookAhead(0);
            if (next == ch.charValue()) {
                this.currentMatch = new Token();
                this.currentMatch.tokenValue =
                new StringBuffer().append(ch.charValue()).toString();
                this.currentMatch.tokenType = tok;
                consume(1);
            } else throw new
            ParseException(buffer + "\nExpecting  " + ch.charValue() , ptr);
        }
        return this.currentMatch;
    }
    
    
    public void SPorHT() {
        try {
            while(lookAhead(0) == ' ' || lookAhead(0) == '\t')
                consume(1);
        } catch (ParseException ex) {
            // Ignore
        }
    }
    public boolean startsId() {
        try {
            char nextChar = lookAhead(0);
            return (isAlpha(nextChar) ||
            isDigit(nextChar) ||
            nextChar == '_'   ||
            nextChar == '+'   ||
            nextChar == '-'   ||
            nextChar == '!'   ||
            nextChar == '`'   ||
            nextChar == '\''  ||
            nextChar == '~'   ||
            nextChar == '.'   ||
            nextChar == '*'   ) ;
        } catch (ParseException ex) {
            return false;
        }
    }
    
    public String ttoken() {
        StringBuffer nextId =  new StringBuffer();
        try {
            while(hasMoreChars()) {
                char nextChar = lookAhead(0);
                //Debug.println("nextChar = " + nextChar);
                if (isAlpha(nextChar) ||
                isDigit(nextChar) ||
                nextChar == '_'   ||
                nextChar == '+'   ||
                nextChar == '-'   ||
                nextChar == '!'   ||
                nextChar == '`'   ||
                nextChar == '\''  ||
                nextChar == '~'   ||
                nextChar == '.'   ||
                nextChar == '*'   )  {
                    consume(1);
                    nextId.append(nextChar);
                } else break;
                
            }
            return nextId.toString();
        } catch (ParseException ex) {
            return nextId.toString();
        }
    }
    
    public String ttokenAllowSpace() {
        StringBuffer nextId =  new StringBuffer();
        try {
            while(hasMoreChars()) {
                char nextChar = lookAhead(0);
                //Debug.println("nextChar = " + nextChar);
                if (isAlpha(nextChar) ||
                isDigit(nextChar) ||
                nextChar == '_'   ||
                nextChar == '+'   ||
                nextChar == '-'   ||
                nextChar == '!'   ||
                nextChar == '`'   ||
                nextChar == '\''  ||
                nextChar == '~'   ||
                nextChar == '.'   ||
                nextChar == ' '   ||
                nextChar == '\t'  ||
                nextChar == '*'   )  {
                    nextId.append(nextChar);
                    consume(1);
                } else break;
                
            }
            return nextId.toString();
        }  catch (ParseException ex) {
            return nextId.toString();
        }
    }
    
    
    // Assume the cursor is at a quote.
    public String quotedString() throws ParseException {
        StringBuffer retval = new StringBuffer();
        if (lookAhead(0) != '\"') return null;
        consume(1);
        while(true) {
            char next = getNextChar();
            if (next == '\"' ) {
		// Got to the terminating quote.
		 break;
	    } else if (next == '\0')  {
		   throw new ParseException
		   ( this.buffer + " :unexpected EOL", this.ptr);
            } else if (next == '\\') {
                retval.append(next);
                next = getNextChar();
                retval.append(next);
            } else {
                retval.append(next);
            }
        }
        return retval.toString();
    }
    
      // Assume the cursor is at a "("
     public String comment() throws ParseException {
           StringBuffer retval = new StringBuffer();
	   if (lookAhead(0) != '(') return null;
	   consume(1);
           while(true) {
                char next = getNextChar();
                if (next == ')' ) { 
		   break;
		} else if (next == '\0') {
		   throw new ParseException( this.buffer + " :unexpected EOL",
			this.ptr);
                } else if (next == '\\') {
                    retval.append(next);
                    next = getNextChar();
		    if (next == '\0') 
		       throw new ParseException( this.buffer + 
			" : unexpected EOL", this.ptr);
                    retval.append(next);
                } else {
                    retval.append(next);
                }
           }
           return retval.toString();
    }
    
    
    public String byteStringNoSemicolon() {
        StringBuffer retval = new StringBuffer();
        try  {
            while(true) {
                char next = lookAhead(0);
                if (next == '\n' || next == ';' ) {
                    break;
                } else {
                    consume(1);
                    retval.append(next);
                }
            }
        } catch (ParseException ex) {
            return retval.toString();
        } finally {
            return retval.toString();
        }
    }
    
    public String byteStringNoComma() {
        StringBuffer retval = new StringBuffer();
        try {
            while(true) {
                char next = lookAhead(0);
                if ( next == '\n' || next == ',') {
                    break;
                } else {
                    consume(1);
                    retval.append(next);
                }
            }
        } catch (ParseException ex) {
        }  finally {
            return retval.toString();
        }
    }
    
    
    public static String charAsString(char ch) {
        return new Character(ch).toString();
    }
    
    /** Lookahead in the inputBuffer for n chars and return as a string.
     * Do not consume the input.
     */
    public String charAsString(int nchars) {
        
        StringBuffer retval = new StringBuffer();
        try {
            for (int i = 0; i < nchars; i++) {
                retval.append(lookAhead(i));
            }
            return retval.toString();
        } catch (ParseException ex) {
            return retval.toString();
            
        }
    }
    
    /** Get and consume the next number.
     */
    public String number() throws ParseException {
        
        StringBuffer retval = new StringBuffer();
        try {
            if (! isDigit(lookAhead(0)) ) {
                throw new ParseException
                (buffer + ": Unexpected token at " +lookAhead(0) ,ptr);
            }
            retval.append(lookAhead(0));
            consume(1);
            while(true) {
                char next = lookAhead(0);
                if ( isDigit(next)) {
                    retval.append(next);
                    consume(1);
                } else break;
            }
            return retval.toString();
        } catch (ParseException ex) {
            return retval.toString();
        }
    }
    
    /** Mark the position for backtracking.
     */
    public int markInputPosition() {
        return ptr;
    }
    
    /** Rewind the input ptr to the marked position.
     */
    public void rewindInputPosition(int position) {
        this.ptr = position;
    }
    
    /** Get the rest of the String
     * @return String
     */
    public String getRest() {
        if (ptr >= buffer.length()) return null;
        else return buffer.substring(ptr);
    }
    
    /** Get the sub-String until the character is encountered
     * @param char c the character to match
     * @return String
     */
    public String getString(char c) throws ParseException {
        StringBuffer retval = new StringBuffer();
        try {
            while(true) {
                char next = lookAhead(0);
		//System.out.println(" next = [" + next + ']' + "ptr = " + ptr);
		//System.out.println(next == '\0');
		
		if (next == '\0' )  {
		    throw new ParseException( this.buffer  + 
			"unexpected EOL", this.ptr);
		} else if ( next == c ) {
                    consume(1);
                    break;
                } else if (next == '\\')  {
                    consume(1);
		    char nextchar = lookAhead(0);
		    if (nextchar == '\0')  {
		       throw new ParseException( this.buffer + 
				"unexpected EOL", this.ptr);
		    } else {
			consume(1);
		        retval.append(nextchar);
		    }
		} else {
                    consume(1);
                    retval.append(next);
                }
            }
        }  finally {
            return retval.toString();
        }
    }
    
    
    
    
    
    /** Get the read pointer.
     */
    public int getPtr() { return this.ptr; }
    
    /** Get the buffer.
     */
    public String getBuffer() { return this.buffer; }

    /** Create a parse exception. 
     */
     public ParseException createParseException() {
	  return new ParseException(this.buffer,this.ptr);
     }
}
