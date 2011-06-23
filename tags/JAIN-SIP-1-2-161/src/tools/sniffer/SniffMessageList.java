package tools.sniffer;

import java.util.*;

/**
 *A list of Sniff messages. Each one corresponds to a call Id.
 * Acknowledgement:
 * This code was contributed by Tim Bardzil <bardzil@colorado.edu>.
 * This code was completed as part of a class project in TLEN 5843
 * Singaling Protocols, taught by Professor Douglas C. Sicker, Ph.D. at
 * the University of Colorado, Boulder.
 * Minor modifications to the code were made by M. Ranganathan .
 *
 *@author Tim Bardzil <bardzil@colorado.edu>
 */
public class SniffMessageList extends ArrayList {
    protected static String fileName;

    public String getCallID() {
        SniffMessage temp = (SniffMessage) super.get(0);
        return temp.getCallID();
    }

    public String toXML() {

        String xmlMessages = new String();
        ListIterator i = super.listIterator();
        while (i.hasNext()) {
            xmlMessages += ((SniffMessage) i.next()).toXML();
        }

        return xmlMessages;
    }
}
