package gov.nist.javax.sdp.parser;
import java.util.*;
import gov.nist.core.*;
import gov.nist.javax.sdp.fields.*;
import gov.nist.javax.sdp.*;
import java.text.ParseException;
/** Parser for SDP Announce messages.
 *
 * Acknowledgement: this includes a bug fix submitted by 
 * Rafael Barriuso rbarriuso@dit.upm.es
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
    *
    */
     public SDPAnnounceParser(String sdpAnnounce) {
      int start = 0;String line = null;
      sdpMessage = new Vector();
      // Bug fix by Andreas Bystrom.
      while (start < sdpAnnounce.length()) {
            int add = 0;
            int index = sdpAnnounce.indexOf("\n",start);
            int index2= sdpAnnounce.indexOf("\r",start);

            if(index>0 && index2<0){ 
		// there are only "\n" separators
               line = sdpAnnounce.substring(start,index);
               start=index+1;
            }else if(index<0 && index2>0){ 
			//bug fix: there are only "\r" separators
              line = sdpAnnounce.substring(start,index2);
              start=index2+1;
            }else if(index>0 && index2>0){ 
		// there are "\r\n" or "\n\r" (if exists) separators
                if(index>index2){
                  line = sdpAnnounce.substring(start,index2);
                  start=index+1;
                }else{
                  line = sdpAnnounce.substring(start,index);
                  start=index2+1;
                }
            }else if(index<0 && index2<0) // end
                break;
          sdpMessage.addElement(line);
      }
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
