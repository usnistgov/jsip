package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.*;
import gov.nist.core.*;
import java.text.ParseException;
import javax.sip.*;
import java.util.*;

/** Parser for TimeStamp header.
*
*@version  JAIN-SIP-1.1
*
*@author Olivier Deruelle <deruelle@nist.gov>  <br/>
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
* @version 1.0
*/
public class TimeStampParser extends   HeaderParser {
    
    /** Creates a new instance of TimeStampParser 
     * @param timeStamp the header to parse
     */
    public TimeStampParser(String timeStamp) {
        super(timeStamp);
    }
    
    /** Constructor
     * @param lexer the lexer to use to parse the header
     */
    protected TimeStampParser(Lexer lexer) {
        super(lexer);
    }
    
    /** parse the String message
     * @return SIPHeader (TimeStamp object)
     * @throws SIPParseException if the message does not respect the spec.
     */
    public SIPHeader parse() throws ParseException {
        
        if (debug) dbg_enter("TimeStampParser.parse");
        TimeStamp timeStamp=new TimeStamp();
        try {
            headerName(TokenTypes.TIMESTAMP);
            
            timeStamp.setHeaderName(SIPHeaderNames.TIMESTAMP);
           
            this.lexer.SPorHT();
            String firstNumber=this.lexer.number(); 
            
            try {
                float ts;
                
                if (lexer.lookAhead(0) == '.') {
                     this.lexer.match('.');
                     String secondNumber=this.lexer.number(); 
                     
                     String s=new String(firstNumber+"."+secondNumber);
                     ts= Float.parseFloat(s);
                }
                else  ts=Float.parseFloat(firstNumber);
                
                timeStamp.setTimeStamp(ts);
            }
            catch (NumberFormatException ex) {
                 throw createParseException(ex.getMessage());
            } catch (InvalidArgumentException ex) {
                throw createParseException(ex.getMessage());
            }
             
            
            this.lexer.SPorHT();
            if (lexer.lookAhead(0) != '\n')  {
                firstNumber=this.lexer.number();
                
                try {
                    float ts;
                    
                    if (lexer.lookAhead(0) == '.') {
                        this.lexer.match('.');
                        String secondNumber=this.lexer.number();
                        
                        String s=new String(firstNumber+"."+secondNumber);
                        ts= Float.parseFloat(s);
                    }
                    else  ts=Float.parseFloat(firstNumber);
                    
                    timeStamp.setDelay(ts);
                }
                catch (NumberFormatException ex) {
                    throw createParseException(ex.getMessage());
                } catch (InvalidArgumentException ex) {
                    throw createParseException(ex.getMessage());
                }
            }

        }
        finally {
            if (debug) dbg_leave("TimeStampParser.parse");
        }
        
        return  timeStamp;
    }
    
    /** Test program
    public static void main(String args[]) throws ParseException {
        String timeStamp[] = {
            "Timestamp: 54 \n",
            "Timestamp: 52.34 34.5 \n"
        };
        
        for (int i = 0; i < timeStamp.length; i++ ) {
            TimeStampParser parser =
            new TimeStampParser(timeStamp[i]);
            TimeStamp ts= (TimeStamp) parser.parse();
            System.out.println("encoded = " + ts.encode());
        }
        
    }
     */
}

