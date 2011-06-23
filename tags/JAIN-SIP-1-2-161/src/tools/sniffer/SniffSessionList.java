package tools.sniffer;

import java.util.*;

/**
* A list of Sniff Sessions.
* Acknowledgement:
* This code was contributed by Tim Bardzil <bardzil@colorado.edu>.
* This code was completed as part of a class project in TLEN 5843
* Singaling Protocols, taught by Professor Douglas C. Sicker, Ph.D. at
* the University of Colorado, Boulder.
* Minor modifications to the code were made by M. Ranganathan .
*
*@author Tim Bardzil <bardzil@colorado.edu>
*
*/

public class SniffSessionList extends ArrayList {

    /**
     * Add a new SniffMessage to the SniffSessionList.
     * Create a new Sniff Session if this is a new call id.
     */
    public void add(SniffMessage sniffMessage) {
        boolean newSession = true;
        ListIterator i = super.listIterator();
        while (i.hasNext()) {
            SniffMessageList temp = (SniffMessageList) i.next();
            if (temp.getCallID().equals(sniffMessage.getCallID())) {
                temp.add(sniffMessage);
                newSession = false;
            }
        }
        if (newSession == true) {
            SniffMessageList newMessageList = new SniffMessageList();
            newMessageList.add(sniffMessage);
            super.add(newMessageList);
        }
    }

    /**
    * Return a string consisting of formatted messages that can be fed
    * to the trace viewer.
    */
    public String toXML() {
        ListIterator li = super.listIterator();
        String xmlMessages =
            "<description\n "
                + "logDescription = "
                + "\" sniffer capture "
                + "\"\n name = "
                + "\" snifferTrace \" /> \n";
        int i = 0;
        while (li.hasNext()) {
            SniffMessageList sml = (SniffMessageList) li.next();
            xmlMessages += sml.toXML();
        }
        return xmlMessages;
    }

    /**
    * Return an array of call identifiers for the traces.
    */
    public String[] getCallIds() {
        ListIterator li = super.listIterator();
        String[] retval = new String[this.size()];
        int i = 0;
        while (li.hasNext()) {
            SniffMessageList temp = (SniffMessageList) li.next();
            retval[i++] = temp.getCallID();
        }
        return retval;
    }
}
