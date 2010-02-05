package tools.tracesviewer;

//ifdef J2SDK1.4
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
//endif

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.*;

/**
 * Parse the log files - sort them and organize by call id.
 */
public class LogFileParser extends DefaultHandler {
    protected Hashtable messageLogs;

    private XMLReader xmlReader;

    private String transactionId;

    private String from;

    private String to;

    private String time;

    private String statusMessage;

    private String firstLine;

    private String callId;

    private String isSender;

    private String messageTimeStamp;

    private StringBuffer message;

    public String logDescription;

    public String auxInfo;

    public String logName;

    private String currentTag;

    private String debugLine;

    private String previousDebug = null;

    private TracesMessage messageLog;

    public LogFileParser() {
        messageLogs = new Hashtable();
        try {
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            SAXParser saxParser = saxParserFactory.newSAXParser();
            this.xmlReader = saxParser.getXMLReader();

            xmlReader.setContentHandler(this);

            xmlReader
                    .setFeature("http://xml.org/sax/features/validation", false);
            // parse the xml specification for the event tags.

        } catch (Exception pce) {
            // Parser with specified options can't be built
            pce.printStackTrace();
        }

    }

    public Hashtable parseLogs(InputSource inputSource) {
        try {
            this.xmlReader.parse(inputSource);
            return messageLogs;
        } catch (SAXParseException spe) {
            spe.printStackTrace();
        } catch (SAXException sxe) {
            sxe.printStackTrace();
        } catch (IOException ioe) {
            // I/O error
            ioe.printStackTrace();
        }
        return messageLogs;
    }

    // ===========================================================
    // SAX DocumentHandler methods
    // ===========================================================

    public void startDocument() throws SAXException {
    }

    public void endDocument() throws SAXException {
    }

    public void startElement(String namespaceURI, String lName, // local name
            String qName, // qualified name
            Attributes attrs) throws SAXException {
        currentTag = qName;

        // System.out.println("currentTag:"+currentTag);
        if (qName.equalsIgnoreCase("debug")) {

        }
        if (qName.equalsIgnoreCase("message")) {
            from = attrs.getValue("from");
            to = attrs.getValue("to");
            time = attrs.getValue("time");
            statusMessage = attrs.getValue("statusMessage");
            transactionId = attrs.getValue("transactionId");
            firstLine = attrs.getValue("firstLine");
            callId = attrs.getValue("callId");
            isSender = attrs.getValue("isSender");
            debugLine = attrs.getValue("debugLine");
            messageTimeStamp = attrs.getValue("timeStamp");
            message = new StringBuffer();

        }
        if (qName.equalsIgnoreCase("description")) {
            logName = attrs.getValue("name");
            logDescription = attrs.getValue("logDescription");
            auxInfo = attrs.getValue("auxInfo");
        }

    }

    public void endElement(String namespaceURI, String sName, // simple name
            String qName // qualified name
    ) throws SAXException {
        if (qName.equalsIgnoreCase("message")) {
            boolean sflag = isSender.equals("true");
            messageLog = new TracesMessage(from, to, time, firstLine, message
                    .toString(), statusMessage, transactionId,
                    messageTimeStamp, debugLine);

            MessageLogList messageLogList = (MessageLogList) messageLogs
                    .get(callId);
            if (messageLogList == null) {
                messageLogList = new MessageLogList(new LogComparator());
                messageLogs.put(callId, messageLogList);
            }
            if ( !messageLogList.contains(messageLog))
                messageLogList.add(messageLog);
        }
    }

    public void characters(char buf[], int offset, int len) throws SAXException {
        if (buf == null)
            return;

        if (currentTag.equalsIgnoreCase("message")) {
            // StringBuffer s = new StringBuffer();
            // s.append(new String(buf, offset, len));

            String str = new String(buf, offset, len); // s.toString().trim();

            if (str.equals(""))
                return;
            message.append(str);

        }

        if (currentTag.equalsIgnoreCase("debug")) {
            StringBuffer s = new StringBuffer();
            s.append(new String(buf, offset, len));
            String str = s.toString().trim();

            if (str.equals(""))
                return;

            // System.out.println("cdata:"+str);

            if (messageLog != null) {
                messageLog.beforeDebug = previousDebug;
                // System.out.println("messageLog.beforeDebug:"+messageLog.beforeDebug);
                messageLog.afterDebug = str;
                // System.out.println("messageLog.afterDebug:"+messageLog.afterDebug);
                previousDebug = str;
            } else {
                previousDebug = str;
            }
        }
    }

    /**
     * Generate a file that can be digested by the trace viewer.
     */
    public Hashtable parseLogsFromDebugFile(String logFileName) {
        try {

            // FileWriter fw=new FileWriter(logFileName);
            // fw.write("]]></debug>");
            File file = new File(logFileName);
            long length = file.length();
            char[] cbuf = new char[(int) length];
            FileReader fr = new FileReader(file);
            fr.read(cbuf);
            fr.close();

            StringBuffer sb = new StringBuffer();
            sb.append("<?xml version='1.0' encoding='us-ascii'?>\n").append(
                    "<messages>\n").append(new String(cbuf)).append(
                    "]]></debug></messages>\n");

            // System.out.println(sb.toString());

            InputSource inputSource = new InputSource(new ByteArrayInputStream(
                    sb.toString().getBytes()));
            return this.parseLogs(inputSource);
        } catch (IOException ex) {
            ex.printStackTrace();

            return null;
        }
    }

    /**
     * Generate a file that can be digested by the trace viewer.
     */
    public Hashtable parseLogsFromFile(String logFileName) {
        try {

            // FileWriter fw=new FileWriter(logFileName);
            // fw.write("]]></debug>");
            File file = new File(logFileName);
            long length = file.length();
            char[] cbuf = new char[(int) length];
            FileReader fr = new FileReader(file);
            fr.read(cbuf);
            fr.close();

            StringBuffer sb = new StringBuffer();
            sb.append("<?xml version='1.0' encoding='us-ascii'?>\n").append(
                    "<messages>\n").append(new String(cbuf)).append(
                    "</messages>\n");

            System.out.println(sb.toString());

            InputSource inputSource = new InputSource(new ByteArrayInputStream(
                    sb.toString().getBytes()));
            return this.parseLogs(inputSource);
        } catch (IOException ex) {
            ex.printStackTrace();

            return null;
        }
    }

    public Hashtable parseLogsFromString(String logString) {
        StringBuffer sb = new StringBuffer();
        sb.append("<?xml version='1.0' encoding='us-ascii'?>\n").append(
                "<messages>\n").append(logString).append("</messages>\n");
        // System.out.println(sb.toString());
        InputSource inputSource = new InputSource(new ByteArrayInputStream(sb
                .toString().getBytes()));
        return this.parseLogs(inputSource);
    }

}
