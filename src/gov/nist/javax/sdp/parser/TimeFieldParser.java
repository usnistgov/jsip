/*
 * TimeFieldParser.java
 *
 * Created on February 25, 2002, 9:58 AM
 */

package gov.nist.javax.sdp.parser;
import gov.nist.javax.sdp.fields.*;
import gov.nist.core.*;
import java.text.*;
/**
 *
 * @author  deruelle
 * @version 1.0
 */
public class TimeFieldParser extends SDPParser {

    /** Creates new TimeFieldParser */
    public TimeFieldParser(String timeField) {
          lexer = new Lexer("charLexer",timeField);
    }
    
    
    /** Get the typed time
     * @param String tokenValue to set
     * @return TypedTime
     */
    public TypedTime getTypedTime(String tokenValue) {
        TypedTime typedTime=new TypedTime();
       
        if (tokenValue.endsWith("d") ){
             typedTime.setUnit("d");
             String t=tokenValue.replace('d',' ');
         
             typedTime.setTime(Integer.parseInt(t.trim()));
        }
        else
            if (tokenValue.endsWith("h") ){
                 typedTime.setUnit("h");
                 String t=tokenValue.replace('h',' ');
                 typedTime.setTime(Integer.parseInt(t.trim()));
            }
        else 
            if (tokenValue.endsWith("m") ) {
                 typedTime.setUnit("m");
                 String t=tokenValue.replace('m',' ');
                 typedTime.setTime(Integer.parseInt(t.trim()));
            }
        else {
              typedTime.setUnit("s");
              if (tokenValue.endsWith("s") ) {
                   String t=tokenValue.replace('s',' ');
                   typedTime.setTime(Integer.parseInt(t.trim()));
              }
              else typedTime.setTime(Integer.parseInt(tokenValue.trim()));
        }
        return typedTime;
    }
    
    
    private long getTime() throws ParseException {
        try {
        String startTime = this.lexer.number();
        return Long.parseLong(startTime);
        } catch (NumberFormatException ex) {
                throw  lexer.createParseException();
        }
        
    }
    
   /** parse the field string
     * @return TimeField 
    */
    public TimeField timeField() throws ParseException  {
        try{
            this.lexer.match ('t');
            this.lexer.SPorHT();
            this.lexer.match('=');
            this.lexer.SPorHT();
            
            TimeField timeField=new TimeField();
            
            long st = this.getTime();
            timeField.setStartTime(st);
            this.lexer.SPorHT();
            
            st = this.getTime();
            timeField.setStopTime(st);
            
            return timeField;
        }
        catch(Exception e) {
            throw lexer.createParseException();
        }  
    }
    
    

    public SDPField parse() throws ParseException {
		return this.timeField();
    }
    
    
/**
    public static void main(String[] args) throws ParseException {
        String time[] = {
                        "t=3033434331 3042462419\n",
			"t=7d 1h \n",
                        "t=3149328700 0 \n",
                        "t=0 0\n"
                };


	    for (int i = 0; i < time.length; i++) {
	        TimeFieldParser timeFieldParser=new TimeFieldParser(
                time[i] );
	        TimeField timeField=timeFieldParser.timeField();
		System.out.println("encoded: " +timeField.encode());
	    }

	}
**/


}
