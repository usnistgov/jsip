/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;

import gov.nist.core.*;
import java.text.ParseException;

/**
* AllowEvents SIPHeader.
*
*@author M. Ranganathan <mranga@nist.gov> NIST/ITL ANTD. <br/>
*@author Olivier Deruelle <deruelle@nist.gov><br/>
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*@version JAIN-SIP-1.1
*/
public class AllowEvents extends SIPHeader 
implements javax.sip.header.AllowEventsHeader {
    
        /** method field
         */    
	protected String eventType;

        /** default constructor
         */        
        public AllowEvents() { 
            super(ALLOW_EVENTS);
        }
	
        /** constructor
         * @param m String to set
         */        
        public AllowEvents(String m ) { 
            super(ALLOW_EVENTS);
            eventType= m;
        }
        
    /**
     * Sets the eventType defined in this AllowEventsHeader.
     *
     * @param eventType - the String defining the method supported
     * in this AllowEventsHeader
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the Strings defining the eventType supported
     */
    public void setEventType(String eventType) throws ParseException {
    	if (eventType==null) throw new  NullPointerException("JAIN-SIP Exception,"+
    	"AllowEvents, setEventType(), the eventType parameter is null");	
    	this.eventType=eventType;
    }

    /**
     * Gets the eventType of the AllowEventsHeader. 
     *
     * @return the String object identifing the eventTypes of AllowEventsHeader.
     */
    public String getEventType() {
    	return eventType;	
    }    
    
     /** Return body encoded in canonical form.
         * @return body encoded as a string.
         */        
	protected String encodeBody() {
		return eventType;
	}
    
}