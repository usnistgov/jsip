/*
* Conditions Of Use
*
* This software was developed by employees of the National Institute of
* Standards and Technology (NIST), an agency of the Federal Government.
* Pursuant to title 15 Untied States Code Section 105, works of NIST
* employees are not subject to copyright protection in the United States
* and are considered to be in the public domain.  As a result, a formal
* license is not needed to use the software.
*
* This software is provided by NIST as a service and is expressly
* provided "AS IS."  NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
* OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
* AND DATA ACCURACY.  NIST does not warrant or make any representations
* regarding the use of the software or the results thereof, including but
* not limited to the correctness, accuracy, reliability or usefulness of
* the software.
*
* Permission to use this software is contingent upon your acceptance
* of the terms of this agreement
*
* .
*
*/
package gov.nist.core;

import gov.nist.javax.sip.*;

import java.text.ParseException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** A lexical analyzer that is used by all parsers in our implementation.
 *
 *@version 1.2
 *@since 1.1
 *
 *@author M. Ranganathan
 */
public class LexerCore extends StringTokenizer {

    // IMPORTANT - All keyword matches should be between START and END
    public static final int START = 2048;
    public static final int END = START + 2048;
    // IMPORTANT -- This should be < END
    public static final int ID_NO_WHITESPACE = END - 3;
    public static final int ID = END - 1;
    public static final int SAFE = END - 2;
    // Individial token classes.
    public static final int WHITESPACE = END + 1;
    public static final int DIGIT = END + 2;
    public static final int ALPHA = END + 3;
    public static final int IPV6 = END + 4;
    public static final int BACKSLASH = (int) '\\';
    public static final int QUOTE = (int) '\'';
    public static final int AT = (int) '@';
    public static final int SP = (int) ' ';
    public static final int HT = (int) '\t';
    public static final int COLON = (int) ':';
    public static final int STAR = (int) '*';
    public static final int DOLLAR = (int) '$';
    public static final int PLUS = (int) '+';
    public static final int POUND = (int) '#';
    public static final int MINUS = (int) '-';
    public static final int DOUBLEQUOTE = (int) '\"';
    public static final int TILDE = (int) '~';
    public static final int BACK_QUOTE = (int) '`';
    public static final int NULL = (int) '\0';
    public static final int EQUALS = (int) '=';
    public static final int SEMICOLON = (int) ';';
    public static final int SLASH = (int) '/';
    public static final int L_SQUARE_BRACKET = (int) '[';
    public static final int R_SQUARE_BRACKET = (int) ']';
    public static final int R_CURLY = (int) '}';
    public static final int L_CURLY = (int) '{';
    public static final int HAT = (int) '^';
    public static final int BAR = (int) '|';
    public static final int DOT = (int) '.';
    public static final int EXCLAMATION = (int) '!';
    public static final int LPAREN = (int) '(';
    public static final int RPAREN = (int) ')';
    public static final int GREATER_THAN = (int) '>';
    public static final int LESS_THAN = (int) '<';
    public static final int PERCENT = (int) '%';
    public static final int QUESTION = (int) '?';
    public static final int AND = (int) '&';
    public static final int UNDERSCORE = (int) '_';

    // jeand : using concurrent data structure to avoid excessive blocking witnessed during profiling
    protected static final ConcurrentHashMap<Integer, String> globalSymbolTable;
    protected static final ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> lexerTables;
    protected Map<String, Integer> currentLexer;
    protected String currentLexerName;
    protected Token currentMatch;

    static {
        globalSymbolTable = new ConcurrentHashMap<Integer, String>();        
        lexerTables = new ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>>();
    }

    protected void addKeyword(String name, int value) {
        name = Utils.toUpperCase(name);
        // System.out.println("addKeyword " + name + " value = " + value);
        // new Exception().printStackTrace();
        Integer val = Integer.valueOf(value);
        currentLexer.put(name, val);
//        if (!globalSymbolTable.containsKey(val))
        globalSymbolTable.putIfAbsent(val, name);
    }

    public String lookupToken(int value) {
        if (value > START) {
            return (String) globalSymbolTable.get(Integer.valueOf(value));
        } else {
            Character ch = Character.valueOf((char) value);
            return ch.toString();
        }
    }

//    protected Map<String, Integer> addLexer(String lexerName) {
//        currentLexer = (Map<String, Integer>) lexerTables.get(lexerName);
//        if (currentLexer == null) {
//            currentLexer = new Hashtable();
//            lexerTables.put(lexerName, currentLexer);
//        }
//        return currentLexer;
//    }

    //public abstract void selectLexer(String lexerName);

    public void selectLexer(String lexerName) {
        this.currentLexerName = lexerName;
    }

    protected LexerCore() {
        this.currentLexer = new ConcurrentHashMap<String, Integer>();
        this.currentLexerName = "charLexer";
    }

    /** Initialize the lexer with a buffer.
     */
    public LexerCore(String lexerName, String buffer) {
        super(buffer);
        this.currentLexerName = lexerName;
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
    public String getNextId() {
        return ttoken();
    }
    
    /** Get the next ip.
     */
    public String getNextIp() {
        return tIpv6address();
    }

    public String getNextIdNoWhiteSpace() {
        return ttokenNoWhiteSpace();
    }

    // call this after you call match
    public Token getNextToken() {
        return this.currentMatch;

    }

    /** Look ahead for one token.
     */
    public Token peekNextToken() throws ParseException {
        return (Token) peekNextToken(1)[0];
    }

    public Token[] peekNextToken(int ntokens) throws ParseException {
        int old = ptr;
        Token[] retval = new Token[ntokens];
        for (int i = 0; i < ntokens; i++) {
            Token tok = new Token();
            if (startsId()) {
                String id = ttoken();
                tok.tokenValue = id;
                String idUppercase = Utils.toUpperCase(id);
                if (currentLexer.containsKey(idUppercase)) {
                    Integer type = (Integer) currentLexer.get(idUppercase);
                    tok.tokenType = type.intValue();
                } else
                    tok.tokenType = ID;
            } else {
                char nextChar = getNextChar();
                tok.tokenValue = String.valueOf(nextChar);
                if (isAlpha(nextChar)) {
                    tok.tokenType = ALPHA;
                } else if (isDigit(nextChar)) {
                    tok.tokenType = DIGIT;
                } else
                    tok.tokenType = (int) nextChar;
            }
            retval[i] = tok;
        }
        savedPtr = ptr;
        ptr = old;
        return retval;
    }

    /** Match the given token or throw an exception if no such token
     * can be matched.
     */
    public Token match(int tok) throws ParseException {
        if (Debug.parserDebug) {
            Debug.println("match " + tok);
        }
        if (tok > START && tok < END) {
            if (tok == ID) {
                // Generic ID sought.
                if (!startsId())
                    throw new ParseException(buffer + "\nID expected", ptr);
                String id = getNextId();
                this.currentMatch = new Token();
                this.currentMatch.tokenValue = id;
                this.currentMatch.tokenType = ID;
            } else if (tok == SAFE) {
                if (!startsSafeToken())
                    throw new ParseException(buffer + "\nID expected", ptr);
                String id = ttokenSafe();
                this.currentMatch = new Token();
                this.currentMatch.tokenValue = id;
                this.currentMatch.tokenType = SAFE;
            } else if (tok == ID_NO_WHITESPACE) {
                if (!startsIdNoWhiteSpace())
                    throw new ParseException(buffer + "\nID no white space expected", ptr);
                String id = getNextIdNoWhiteSpace();
                this.currentMatch = new Token();
                this.currentMatch.tokenValue = id;
                this.currentMatch.tokenType = ID_NO_WHITESPACE;
            } else {
                String nexttok = getNextId();
                Integer cur = currentLexer.get(Utils.toUpperCase(nexttok));

                if (cur == null || cur.intValue() != tok)
                    throw new ParseException(
                        buffer + "\nUnexpected Token : " + nexttok,
                        ptr);
                this.currentMatch = new Token();
                this.currentMatch.tokenValue = nexttok;
                this.currentMatch.tokenType = tok;
            }
        } else if (tok > END) {
            // Character classes.
            char next = lookAhead(0);
            if (tok == DIGIT) {
                if (!isDigit(next))
                    throw new ParseException(buffer + "\nExpecting DIGIT", ptr);
                this.currentMatch = new Token();
                this.currentMatch.tokenValue =
                    String.valueOf(next);
                this.currentMatch.tokenType = tok;
                consume(1);

            } else if (tok == ALPHA) {
                if (!isAlpha(next))
                    throw new ParseException(buffer + "\nExpecting ALPHA", ptr);
                this.currentMatch = new Token();
                this.currentMatch.tokenValue =
                    String.valueOf(next);
                this.currentMatch.tokenType = tok;
                consume(1);

            } else if (tok == IPV6){
                String ip = getNextIp();
                this.currentMatch = new Token();
                this.currentMatch.tokenValue = ip;
                this.currentMatch.tokenType = IPV6;
            }

        } else {
            // This is a direct character spec.
            char ch = (char) tok;
            char next = lookAhead(0);
            if (next == ch) {
                /*this.currentMatch = new Token();
                this.currentMatch.tokenValue =
                    String.valueOf(ch);
                this.currentMatch.tokenType = tok;*/
                consume(1);
            } else
                throw new ParseException(
                    buffer + "\nExpecting  >>>" + ch + "<<< got >>>"
                    + next + "<<<", ptr);
        }
        return this.currentMatch;
    }

    public void SPorHT() {
        try {
            char c = lookAhead(0);
            while (c == ' ' || c == '\t') {
                consume(1);
                c = lookAhead(0);
            }
        } catch (ParseException ex) {
            // Ignore
        }
    }
    
    /**
     * JvB: utility function added to validate tokens
     *
     * @see RFC3261 section 25.1:
     * token       =  1*(alphanum / "-" / "." / "!" / "%" / "*"
                     / "_" / "+" / "`" / "'" / "~" )

     * @param c - character to check
     * @return true iff character c is a valid token character as per RFC3261
     */
    public static final boolean isTokenChar( char c ) {
        if ( isAlphaDigit(c) ) return true;
        else switch (c)
        {
            case '-':
            case '.':
            case '!':
            case '%':
            case '*':
            case '_':
            case '+':
            case '`':
            case '\'':
            case '~':
                return true;
            default:
                return false;
        }
    }


    public boolean startsId() {
        try {
            char nextChar = lookAhead(0);
            return isTokenChar(nextChar);
        } catch (ParseException ex) {
            return false;
        }
    }

    public boolean startsIdNoWhiteSpace() {
        try {
            char nextChar = lookAhead(0);
            return nextChar != ' ' && nextChar != '\t' && nextChar != '\n';
        } catch (ParseException ex) {
            return false;
        }
    }

    public boolean startsSafeToken() {
        try {
            char nextChar = lookAhead(0);
            if (isAlphaDigit(nextChar)) {
                return true;
            }
            else {
                switch (nextChar) {
                    case '_':
                    case '+':
                    case '-':
                    case '!':
                    case '`':
                    case '\'':
                    case '.':
                    case '/':
                    case '}':
                    case '{':
                    case ']':
                    case '[':
                    case '^':
                    case '|':
                    case '~':
                    case '%': // bug fix by Bruno Konik, JvB copied here
                    case '#':
                    case '@':
                    case '$':
                    case ':':
                    case ';':
                    case '?':
                    case '\"':
                    case '*':
                    case '=': // Issue 155 on java.net
                        return true;
                    default:
                        return false;
                }
            }
        } catch (ParseException ex) {
            return false;
        }
    }

    public String ttoken() {
        int startIdx = ptr;
        try {
            while (hasMoreChars()) {
                char nextChar = lookAhead(0);
                if ( isTokenChar(nextChar) ) {
                    consume(1);
                } else {
                    break;
                }
            }
            return String.valueOf(buffer, startIdx, ptr - startIdx);
        } catch (ParseException ex) {
            return null;
        }
    }
    
    public String tIpv6address() {
        try {
            String hostName = String.valueOf(buffer, ptr, buffer.length - ptr );
            HostNameParser hnp = new HostNameParser(hostName);
            HostPort hp = hnp.hostPort(true);
            int length = hp.getHost().hostname.length();
            consume(length);
            return hp.getHost().hostname;
        } catch (ParseException ex) {
            return null;
        }
    }

    public String ttokenNoWhiteSpace() {
        int startIdx = ptr;
        try {
            while (hasMoreChars()) {
                char nextChar = lookAhead(0);
                if ( nextChar == ' ' || nextChar == '\n' || nextChar == '\t' ) {
			break;
                } else {
                     consume(1);
                }
            }
            return String.valueOf(buffer, startIdx, ptr - startIdx);
        } catch (ParseException ex) {
            return null;
        }
    }

    public String ttokenSafe() {
        int startIdx = ptr;
        try {
            while (hasMoreChars()) {
                char nextChar = lookAhead(0);
                if (isAlphaDigit(nextChar)) {
                    consume(1);
                }
                else {
                    boolean isValidChar = false;
                    switch (nextChar) {
                        case '_':
                        case '+':
                        case '-':
                        case '!':
                        case '`':
                        case '\'':
                        case '.':
                        case '/':
                        case '}':
                        case '{':
                        case ']':
                        case '[':
                        case '^':
                        case '|':
                        case '~':
                        case '%': // bug fix by Bruno Konik, JvB copied here
                        case '#':
                        case '@':
                        case '$':
                        case ':':
                        case ';':
                        case '?':
                        case '\"':
                        case '*':
                            isValidChar = true;
                    }
                    if (isValidChar) {
                        consume(1);
                    }
                    else {
                        break;
                    }
                }
            }
            return String.valueOf(buffer, startIdx, ptr - startIdx);
        } catch (ParseException ex) {
            return null;
        }
    }
    
    public String ttokenGenValue() {
        int startIdx = ptr;
        try {
            while (hasMoreChars()) {
                char nextChar = lookAhead(0);
                if (isAlphaDigit(nextChar)) {
                    consume(1);
                }
                else {
                    boolean isValidChar = false;
                    switch (nextChar) {
                        case '_':
                        case '+':
                        case '-':
                        case '!':
                        case '`':
                        case '\'':
                        case '.':
                        case '/':
                        case '}':
                        case '{':
                        case ']':
                        case '[':
                        case '^':
                        case '|':
                        case '~':
                        case '%': // bug fix by Bruno Konik, JvB copied here
                        case '#':
                        case '@':
                        case '$':
                        case ':':
                        case '?':
                        case '\"':
                        case '*':
                            isValidChar = true;
                    }
                    if (isValidChar) {
                        consume(1);
                    }
                    else {
                        break;
                    }
                }
            }
            return String.valueOf(buffer, startIdx, ptr - startIdx);
        } catch (ParseException ex) {
            return null;
        }
    }

    static final char ALPHA_VALID_CHARS = Character.MAX_VALUE;
    static final char DIGIT_VALID_CHARS = Character.MAX_VALUE - 1;
    static final char ALPHADIGIT_VALID_CHARS = Character.MAX_VALUE - 2;
    public void consumeValidChars(char[] validChars) {
        int validCharsLength = validChars.length;
        try {
            while (hasMoreChars()) {
                char nextChar = lookAhead(0);
                boolean isValid = false;
                for (int i = 0; i < validCharsLength; i++) {
                    char validChar = validChars[i];
                    switch(validChar) {
                        case ALPHA_VALID_CHARS:
                            isValid = isAlpha(nextChar);
                            break;
                        case DIGIT_VALID_CHARS:
                            isValid = isDigit(nextChar);
                            break;
                        case ALPHADIGIT_VALID_CHARS:
                            isValid = isAlphaDigit(nextChar);
                            break;
                        default:
                            isValid = nextChar == validChar;
                    }
                    if (isValid) {
                        break;
                    }
                }
                if (isValid) {
                    consume(1);
                }
                else {
                    break;
                }
            }
        } catch (ParseException ex) {

        }
    }

    /** Parse a comment string cursor is at a ". Leave cursor at closing "
    *@return the substring containing the quoted string excluding the
    * closing quote.
    */
    public String quotedString() throws ParseException {
        int startIdx = ptr + 1;
        if (lookAhead(0) != '\"')
            return null;
        consume(1);
        while (true) {
            char next = getNextChar();
            if (next == '\"') {
                // Got to the terminating quote.
                break;
            } else if (next == '\0') {
                throw new ParseException(
                    String.valueOf(this.buffer) + " :unexpected EOL",
                    this.ptr);
            } else if (next == '\\') {
                consume(1);
            }
        }
        return String.valueOf(buffer, startIdx, ptr - startIdx - 1);
    }

    /** Parse a comment string cursor is at a "(". Leave cursor at )
    *@return the substring containing the comment excluding the
    * closing brace.
    */
    public String comment() throws ParseException {
        StringBuilder retval = new StringBuilder();
        if (lookAhead(0) != '(')
            return null;
        consume(1);
        while (true) {
            char next = getNextChar();
            if (next == ')') {
                break;
            } else if (next == '\0') {
                throw new ParseException(
                    this.buffer + " :unexpected EOL",
                    this.ptr);
            } else if (next == '\\') {
                retval.append(next);
                next = getNextChar();
                if (next == '\0')
                    throw new ParseException(
                        this.buffer + " : unexpected EOL",
                        this.ptr);
                retval.append(next);
            } else {
                retval.append(next);
            }
        }
        return retval.toString();
    }

    /** Return a substring containing no semicolons.
    *@return a substring containing no semicolons.
    */
    public String byteStringNoSemicolon() {
        StringBuilder retval = new StringBuilder();
        try {
            while (true) {
                char next = lookAhead(0);
                // bug fix from Ben Evans.
                if (next == '\0' || next == '\n' || next == ';' || next == ',' ) {
                    break;
                } else {
                    consume(1);
                    retval.append(next);
                }
            }
        } catch (ParseException ex) {
            return retval.toString();
        }
        return retval.toString();
    }

    /**
     * Scan until you see a slash or an EOL.
     *
     * @return substring containing no slash.
     */
    public String byteStringNoSlash() {
        StringBuilder retval = new StringBuilder();
        try {
            while (true) {
                char next = lookAhead(0);
                // bug fix from Ben Evans.
                if (next == '\0' || next == '\n' || next == '/'  ) {
                    break;
                } else {
                    consume(1);
                    retval.append(next);
                }
            }
        } catch (ParseException ex) {
            return retval.toString();
        }
        return retval.toString();
    }

    /** Return a substring containing no commas
    *@return a substring containing no commas.
    */

    public String byteStringNoComma() {
        StringBuilder retval = new StringBuilder();
        try {
            while (true) {
                char next = lookAhead(0);
                if (next == '\n' || next == ',') {
                    break;
                } else {
                    consume(1);
                    retval.append(next);
                }
            }
        } catch (ParseException ex) {
        }
        return retval.toString();
    }

    public static String charAsString(char ch) {
        return String.valueOf(ch);
    }

    /** Lookahead in the inputBuffer for n chars and return as a string.
     * Do not consume the input.
     */
    public String charAsString(int nchars) {
        return String.valueOf(buffer, ptr, nchars);
    }

    /** Get and consume the next number.
     *@return a substring corresponding to a number
     *(i.e. sequence of digits).
     */
    public String number() throws ParseException {

        int startIdx = ptr;
        try {
            if (!isDigit(lookAhead(0))) {
                throw new ParseException(
                    buffer + ": Unexpected token at " + lookAhead(0),
                    ptr);
            }
            consume(1);
            while (true) {
                char next = lookAhead(0);
                if (isDigit(next)) {
                    consume(1);
                } else
                    break;
            }
            return String.valueOf(buffer, startIdx, ptr - startIdx);
        } catch (ParseException ex) {
            return String.valueOf(buffer, startIdx, ptr - startIdx);
        }
    }

    /** Mark the position for backtracking.
     *@return the current location of the pointer.
     */
    public int markInputPosition() {
        return ptr;
    }

    /** Rewind the input ptr to the marked position.
     *@param position - the position to rewind the parser to.
     */
    public void rewindInputPosition(int position) {
        this.ptr = position;
    }

    /** Get the rest of the String
     * @return rest of the buffer.
     */
    public String getRest() {
        if (ptr > bufferLen)
            return null;
        else if ( ptr == bufferLen ) return "";
        else  return String.valueOf(buffer, ptr, bufferLen - ptr);  
    }

    /** Get the sub-String until the character is encountered
     * @param c the character to match
     * @return the substring that matches.
     */
    public String getString(char c) throws ParseException {
        StringBuilder retval = new StringBuilder();
        while (true) {
            char next = lookAhead(0);
            //System.out.println(" next = [" + next + ']' + "ptr = " + ptr);
            //System.out.println(next == '\0');

            if (next == '\0') {
                throw new ParseException(
                    this.buffer + "unexpected EOL",
                    this.ptr);
            } else if (next == c) {
                consume(1);
                break;
            } else if (next == '\\') {
                consume(1);
                char nextchar = lookAhead(0);
                if (nextchar == '\0') {
                    throw new ParseException(
                        this.buffer + "unexpected EOL",
                        this.ptr);
                } else {
                    consume(1);
                    retval.append(nextchar);
                }
            } else {
                consume(1);
                retval.append(next);
            }
        }
        return retval.toString();
    }

    /** Get the read pointer.
     */
    public int getPtr() {
        return this.ptr;
    }

    /** Get the buffer.
     */
    public String getBuffer() {
        return String.valueOf(buffer);
    }

    /** Create a parse exception.
     */
    public ParseException createParseException() {
        return new ParseException(getBuffer(), this.ptr);
    }
}
