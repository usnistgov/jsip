package tools.sniffer;

import java.util.*;
import java.text.*;
import gov.nist.javax.sip.parser.*;
import gov.nist.javax.sip.message.*;

/**
* A parser for Sniff files. This  is the main
* workhorse that reads a Ethereal Sniff file and
* converts it to a fromat that can be used by the trace viewer application
* Acknowledgement:
* This code was contributed by Tim Bardzil <bardzil@colorado.edu>.
* This code was completed as part of a class project in TLEN 5843
* Singaling Protocols, taught by Professor Douglas C. Sicker, Ph.D. at
* the University of Colorado, Boulder.
*
*@author Tim Bardzil <bardzil@colorado.edu> (original)
*@author M. Ranganathan  (ported to 1.2)
*Jeff Adams submitted a patch for this file.
*
*/

public class SniffMessage implements ParseExceptionListener {
    String time;
    String sourceIP;
    String destIP;
    SIPMessage sipMessage;

    public SniffMessage() {
    }

    public SniffMessage(ArrayList sniffMsgList) throws ParseException {
        getTime(sniffMsgList);
        getIPAddresses(sniffMsgList);
        getSipMessage(sniffMsgList);
    }

    private void getTime(ArrayList sniffMsgList) {
        Iterator i = sniffMsgList.iterator();
        //Date d = new Date(System.currentTimeMillis());
        while (i.hasNext()) {
            String line = (String) i.next();
            if (line.startsWith("Arrival Time")) {
                time = line.substring(line.indexOf(":") + 1).trim();
                time = time.substring(0, time.length() - 6);
                SimpleDateFormat formatter =
                    new SimpleDateFormat("MMM dd',' yyyy hh:mm:ss.SSS");
                ParsePosition pos = new ParsePosition(0);
                Date d = formatter.parse(time, pos);
                time = String.valueOf(d.getTime());
                break;
            }
        }
    }

    private void getIPAddresses(ArrayList sniffMsgList) {
        Iterator i = sniffMsgList.iterator();
        while (i.hasNext()) {
            String line = (String) i.next();
            if (line.startsWith("Internet Protocol")) {
                StringTokenizer st = new StringTokenizer(line, ",");
                while (st.hasMoreTokens()) {
                    String temp = st.nextToken().trim();
                    if (temp.startsWith("Src Addr:")) {
                        StringTokenizer st2 = new StringTokenizer(temp, ":");
                        st2.nextToken(); //skip Src Addr:
                        sourceIP = st2.nextToken().trim();
                    }
                    if (temp.startsWith("Dst Addr:")) {
                        StringTokenizer st2 = new StringTokenizer(temp, ":");
                        st2.nextToken(); //skip Src Addr:
                        destIP = st2.nextToken().trim();
                    }
                }
                break;
            }
        }
    }

    private int indexOfSDP(ArrayList sniffMsgList) {
        Iterator i = sniffMsgList.iterator();
        while (i.hasNext()) {
            String line = (String) i.next();
            if (line.startsWith("Session Description Protocol")) {
                return sniffMsgList.indexOf(line);
            }
        }
        return sniffMsgList.size();
    }

    private int indexOfSIP(ArrayList sniffMsgList) {
        Iterator i = sniffMsgList.iterator();
        while (i.hasNext()) {
            String line = (String) i.next();
            if (line.startsWith("Session Initiation Protocol")) {
                return sniffMsgList.indexOf(line);
            }
        }
        return sniffMsgList.size();
    }

    private void getSipMessage(ArrayList sniffMsgList) throws ParseException {
        int sipIndex = indexOfSIP(sniffMsgList);
        int sdpIndex = indexOfSDP(sniffMsgList);
        String msgBuffer = new String();

        //get SIP message
        for (int i = sipIndex + 1; i < sdpIndex; i++) {
            String line = (String) sniffMsgList.get(i);
            if (line.startsWith("Request-Line")
                || line.startsWith("Status-Line")) {
                msgBuffer =
                    msgBuffer
                        + line.substring(line.indexOf(":") + 1).trim()
                        + "\r\n";
            } else if (line.startsWith("Message Header")) {
                //do nothing
            } else if (line.startsWith("Message body")) {
                //do nothing (start of SDP)
            } else {
                msgBuffer = msgBuffer + line.trim() + "\r\n";
            }
        }

        msgBuffer = msgBuffer + "\r\n";

        //get SDP if it exsits
        for (int j = sdpIndex; j < sniffMsgList.size(); j++) {
            String line = (String) sniffMsgList.get(j);
            if (line.indexOf("(") > 0 && line.indexOf(")") > 0) {
                msgBuffer =
                    msgBuffer
                        + line.charAt(line.indexOf(":") - 2)
                        + "="
                        + line.substring(line.indexOf(":") + 1).trim()
                        + "\r\n";
            }
        }

        //parse SIP message
        StringMsgParser parser = new StringMsgParser();
        sipMessage = parser.parseSIPMessage(msgBuffer.getBytes(), true, false, new SniffMessage());
    }

    public String getCallID() {

        return sipMessage.getCallId().getCallId();
    }

    public String toXML() {
        String xmlMessage = new String();

        String message = sipMessage.encode();
        String statusMessage = "";
        String firstLine = new String();
        if (sipMessage.getClass().isInstance(new SIPRequest())) {
            SIPRequest sipReq = (SIPRequest) sipMessage;
            firstLine = sipReq.getRequestLine().encode().trim();
        } else if (sipMessage.getClass().isInstance(new SIPResponse())) {
            SIPResponse sipRes = (SIPResponse) sipMessage;
            firstLine = sipRes.getStatusLine().encode().trim();
        }
        xmlMessage += "<message from=\""
            + sourceIP
            + "\" to=\""
            + destIP
            + "\" time=\""
            + time
            + "\" isSender=\""
            + true
            + "\" callId=\""
            + sipMessage.getCallId().getCallId()
            + "\" statusMessage=\""
            + statusMessage
            + "\" transactionId=\""
            + sipMessage.getTransactionId()
            + "\" firstLine=\""
            + firstLine
            + "\">\n";
        xmlMessage += "<![CDATA[";
        xmlMessage += message;
        xmlMessage += "]]>\n";
        xmlMessage += "</message>\n";

        return xmlMessage;
    }

    public void handleException(
        ParseException ex,
        SIPMessage sipMessage,
        Class headerClass,
        String headerText,
        String messageText)
        throws ParseException {
        System.out.println("Error line = " + headerText);

        try {
            if (headerClass
                .equals(Class.forName("gov.nist.javax.sip.header.From"))
                || headerClass.equals(
                    Class.forName("gov.nist.javax.sip.header.To"))
                || headerClass.equals(
                    Class.forName("gov.nist.javax.sip.header.ViaList"))
                || headerClass.equals(
                    Class.forName("gov.nist.javax.sip.header.CSeq"))
                || headerClass.equals(
                    Class.forName("gov.nist.javax.sip.header.CallId")))
                throw ex;
        } catch (ClassNotFoundException e) {
            System.out.println("could not find class -- internal error");
            e.printStackTrace();
            System.exit(0);
        }

    }
}
