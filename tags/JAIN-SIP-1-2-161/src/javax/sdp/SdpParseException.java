/*
 * SdpException.java
 *
 * Created on December 18, 2001, 11:08 AM
 */

package javax.sdp;

/** The SdpParseException encapsulates the information thrown when an error occurs
 * during SDP parsing.
 * @author deruelle
 * @version 1.0
 */
public class SdpParseException extends SdpException {
   
    private int lineNumber;
    private int charOffset;
    
    /** Constructs a new SdpParseException when the parser needs to throw an exception
     * indicating a parsing failure.
     * @param lineNumber SDP line number that caused the exception.
     * @param charOffset offset of the characeter that caused the exception.
     * @param message a String containing the text of the exception message
     * @param rootCause the Throwable exception that interfered with the Codelet's 
     * normal operation, making this Codelet exception necessary.
     */    
    public SdpParseException(int lineNumber,int charOffset,String message,
    Throwable rootCause){
        super(message,rootCause);
        this.lineNumber=lineNumber;
        this.charOffset=charOffset;
    }
    
     /** Constructs a new SdpParseException when the parser needs to throw an exception
     * indicating a parsing failure.
     * @param lineNumber SDP line number that caused the exception.
     * @param charOffset offset of the characeter that caused the exception.
     * @param message a String containing the text of the exception message
     */
    public SdpParseException(int lineNumber,int charOffset,String message){
        super(message);
        this.lineNumber=lineNumber;
        this.charOffset=charOffset;
    }
    
    /** Returns the line number where the error occured
     * @return the line number where the error occured
     */    
    public int getLineNumber(){
        return lineNumber;
    }
    
    /** Returns the char offset where the error occured.
     * @return the char offset where the error occured.
     */    
    public int getCharOffset(){
        return charOffset;
    }
    
    
    /** Returns the message stored when the exception was created.
     * @return the message stored when the exception was created.
     */    
    public String getMessage() {
        return super.getMessage();
    }
    
}
