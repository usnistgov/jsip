package tools.sniffer;

import java.io.*;
import java.util.*;
import tools.tracesviewer.*;

/**
* Code to convert Ethereal frames to the XML format that the trace viewer
* application likes.
* @author Tim Bardzil <bardzil@colorado.edu>.
*
* Acknowledgement:
* This code was contributed by Tim Bardzil <bardzil@colorado.edu>.
* This code was completed as part of a class project in TLEN 5843
* Singaling Protocols, taught by Professor Douglas C. Sicker, Ph.D. at
* the University of Colorado, Boulder.
* Minor modifications to the code were made by M. Ranganathan .
*
*/
public class SniffFileParser {
    SniffSessionList sniffSessionList;

    public SniffFileParser(String messageFile) {
        String buffer = new String();
        ArrayList sniffMsgList;
        sniffSessionList = new SniffSessionList();

        try {
            BufferedReader in = new BufferedReader(new FileReader(messageFile));
            buffer = in.readLine();
            while (buffer != null) { //read until EOF
                sniffMsgList = new ArrayList();
                while (buffer != null
                    && buffer.length() > 0) { //read one frame
                    sniffMsgList.add(buffer.trim());
                    buffer = in.readLine();
                }
                SniffMessage sniffMsg = new SniffMessage(sniffMsgList);
                sniffSessionList.add(sniffMsg);
                buffer = in.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SniffSessionList getSniffSessionList() {
        return sniffSessionList;
    }

    /**
     * The main entry point.
     *
     *@param args is the argument file. args[0] is the Ethereral packet
     * sniffer output to convert to the format that the trace viewer
     * likes.
     */
    public static void main(String args[]) {
        String fileName = args[0];
        SniffMessageList.fileName = fileName;
        if (args[0] == null) {
            System.out.println("Please specify sniffer file");
            System.out.println("Bailing Out!");
            System.exit(0);
        }
        SniffFileParser sfp = new SniffFileParser(args[0]);
        SniffSessionList sniffSessions = sfp.getSniffSessionList();
        //String[] sessionNames = sniffSessions.getCallIds();
        LogFileParser parser = new LogFileParser();
        Hashtable traces = parser.parseLogsFromString(sniffSessions.toXML());
        new TracesViewer(traces, fileName, "Ethereal Sniffer Trace", null)
            .show();

    }
}
