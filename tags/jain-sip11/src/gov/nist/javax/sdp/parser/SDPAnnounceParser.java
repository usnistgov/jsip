package gov.nist.javax.sdp.parser;
import java.util.*;
import gov.nist.core.*;
import gov.nist.javax.sdp.fields.*;
import gov.nist.javax.sdp.*;
import java.text.ParseException;
/**
 *
 */
public class SDPAnnounceParser extends ParserCore {

    protected Lexer lexer;
    protected Vector sdpMessage;

    
    /** Creates new SDPAnnounceParser 
     * @param sdpMessage Vector of messages to parse.
     */
    public SDPAnnounceParser(Vector sdpMessage) {
        this.sdpMessage=sdpMessage;
	
    }

	/** Create a new SDPAnnounceParser.
	*@param sdpMessage message containing the sdp announce message.
	*/
     public SDPAnnounceParser(String sdpAnnounce) {
          sdpMessage = new Vector();
	  int start = 0;
	  sdpMessage = new Vector();
	  // Bug fix by Andreas Bystrom.
	  while (start < sdpAnnounce.length() ) {
  	      int add = 0;
  	      int index = sdpAnnounce.indexOf("\n",start);
  	      if (index == -1) break;
  	      if (sdpAnnounce.charAt(index - 1) == '\r') {
    		index = index - 1;
    		add = 1;
  	      }
   	     String line = sdpAnnounce.substring(start,index);
  	     start = index + 1 + add;
  	    sdpMessage.addElement(line);
	  }
	  
/**
	  while (start < sdpAnnounce.length() ) {
	     int index = sdpAnnounce.indexOf("\r\n",start);
	     if (index == -1) break;
	     String line = sdpAnnounce.substring(start,index);
	     start = index + 2;
	     sdpMessage.addElement(line);
	  }
**/
	
     }

     public SessionDescriptionImpl parse()  throws ParseException {
	SessionDescriptionImpl retval = new SessionDescriptionImpl();
	for (int i = 0 ; i <  sdpMessage.size(); i++) {
		String field = (String) sdpMessage.elementAt(i);
		SDPParser sdpParser = ParserFactory.createParser(field);
		SDPField sdpField = sdpParser.parse();
		retval.addField(sdpField);
	}
	return retval;

     }

}
