/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;
import gov.nist.core.*;
import java.util.*;
import java.text.ParseException;
import javax.sip.header.*;

/**
* List of AllowEvents headers. 
* The sip message can have multiple AllowEvents headers which are strung
* together in a list.
*
*@author M. Ranganathan <mranga@nist.gov> <br/>
*@author Olivier Deruelle <deruelle@nist.gov><br/>
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*@version JAIN-SIP-1.1
*
*/
public class AllowEventsList extends SIPHeaderList {
    
        /** default constructor
         */   
	public AllowEventsList() {
	    super( AllowEvents.class,AllowEventsHeader.NAME);
	}

        /**
         * Gets an Iterator of all the methods of the AllowEventsHeader. Returns an empty
         *
         * Iterator if no methods are defined in this AllowEvents Header.
         *
         *
         *
         * @return Iterator of String objects each identifing the methods of
         *
         * AllowEventsHeader.
         *
         * @since JAIN SIP v1.1
         *
         */
        public ListIterator getMethods() {
            ListIterator li = super.hlist.listIterator();
            LinkedList ll = new LinkedList();
            while (li.hasNext()) {
                AllowEvents allowEvents = (AllowEvents) li.next();
                ll.add(allowEvents.getEventType());
            }
            return ll.listIterator();
        }
        
        /**
         * Sets the methods supported defined by this AllowEventsHeader.
         *
         *
         *
         * @param methods - the Iterator of Strings defining the methods supported
         *
         * in this AllowEventsHeader
         *
         * @throws ParseException which signals that an error has been reached
         *
         * unexpectedly while parsing the Strings defining the methods supported.
         *
         * @since JAIN SIP v1.1
         *
         */
        public void setMethods(List methods) throws ParseException {
            ListIterator it = methods.listIterator();
            while (it.hasNext()) {
                AllowEvents allowEvents = new AllowEvents();
                allowEvents.setEventType((String) it.next());
                this.add(allowEvents);
            }
        }
        
}
