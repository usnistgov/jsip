/*
* Conditions Of Use
*
* This software was developed by employees of the National Institute of
* Standards and Technology (NIST), and others.
* This software is has been contributed to the public domain.
* As a result, a formal license is not needed to use the software.
*
* This software is provided "AS IS."
* NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
* OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
* AND DATA ACCURACY.  NIST does not warrant or make any representations
* regarding the use of the software or the results thereof, including but
* not limited to the correctness, accuracy, reliability or usefulness of
* the software.
*
*
*/
/*******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
 * Author: M. Ranganathan (mranga@nist.gov)                                     *
 *******************************************************************************/
/**
 * Implements  Parser torture tests.
 *
 *@author  M. Ranganathan < mranga@nist.gov >
 *
 */

package test.torture;

import gov.nist.javax.sip.address.*;
import gov.nist.javax.sip.parser.*;
import gov.nist.javax.sip.header.*;
import gov.nist.javax.sip.message.*;
import java.io.*;
import java.util.Hashtable;
import java.util.Enumeration;
// ifdef J2SDK1.4
import javax.xml.parsers.SAXParserFactory;
// endif
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import java.text.ParseException;

public class Torture extends DefaultHandler implements ParseExceptionListener,
        TagNames, TokenValues, ConfigurationSwitches {

    // variables that I read from the xml file header.
    protected static boolean debug = true;

    protected static long startTime;

    protected static int counter = 0;

    protected String testMessage;

    protected String encodedMessage;

    protected String testDescription;

    protected boolean debugFlag;

    protected boolean abortOnFail;

    protected String failureReason;

    protected String statusMessage;

    protected String exceptionHeader;

    protected String exceptionMessage;

    protected String exceptionClassName;

    protected boolean failureFlag;

    protected boolean successFlag;

    protected boolean outputParsedStructures;

    protected static boolean outputResults;

    private PrintStream testLogWriter;

    private Hashtable exceptionTable;

    private StringMsgParser stringParser;

    // These track which context the parser is currently parsing.
    private boolean messageContext;

    private boolean exceptionTextContext;

    private boolean sipHeaderContext;

    private boolean sipURLContext;

    private boolean testCaseContext;

    private boolean descriptionContext;

    private boolean expectExceptionContext;

    private boolean expectExtensionContext;

    private boolean exceptionMessageContext;

    private String testMessageType;

    private static final String XML_DOCTYPE_IDENTIFIER = "<?xml version='1.0' encoding='us-ascii'?>";

    class MyEntityResolver implements  EntityResolver {

        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {

            return new InputSource( Torture.class.getResourceAsStream("torture.dtd"));
        }

    }

    class TestLogWriter extends PrintStream {
        public void println(String stuff) {
            if (outputResults)
                super.println();
        }

        public TestLogWriter(FileOutputStream fos) {
            super(fos);
        }

    }

    class ExpectedException {
        protected boolean fired;

        protected String exceptionHeader;

        protected String exceptionMessage;

        protected Class exceptionClass;

        protected ExpectedException(String className, String headerString) {
            try {
                exceptionClass = Class.forName(className);
                exceptionHeader = headerString;
                fired = false;
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
                System.err.println(ex.getMessage());
                testLogWriter.close();
                System.exit(0);
            }
        }
    }

    private void setFailure() {
        failureFlag = true;
    }

    public void handleException(ParseException ex, SIPMessage sipMessage,
            Class headerClass, String headerText, String messageText)
            throws ParseException {
        String exceptionString = headerText;
        if (debugFlag) {
            System.out.println(exceptionString);
        }

        if (exceptionTable == null) {
            formatFailureDiagnostic(ex,
                    "unexpected exception (no exceptions expected)");
            setFailure();
            if (debugFlag)
                throw ex;
        } else {
            ExpectedException e = (ExpectedException) exceptionTable
                    .get(headerText.trim());
            if (e == null) {
                // Check if there is an exception handler for
                // wildcard match.
                e = (ExpectedException) exceptionTable.get(messageText.trim());
            }
            if (e == null) {
                formatFailureDiagnostic(ex, "unexpected exception "
                        + ex.getMessage());
                setFailure();
                if (debugFlag)
                    throw ex;
            } else if (!ex.getClass().equals(e.exceptionClass)) {
                setFailure();
                formatFailureDiagnostic(ex, " got " + ex.getClass()
                        + " expecting " + e.exceptionClass.getName());
                if (debugFlag)
                    throw ex;
            } else {
                e.exceptionMessage = headerText;
                e.fired = true;
                successFlag = true;
            }
        }
    }

    public void characters(char[] ch, int start, int length) {
        String str = new String(ch, start, length);
        if (str.trim() == "")
            return;
        if (messageContext) {
            if (testMessage == null)
                testMessage = str;
            else
                testMessage += str;
        } else if (descriptionContext) {
            if (testDescription == null)
                testDescription = str;
            else
                testDescription += str;
        } else if (exceptionTextContext) {
            if (exceptionHeader == null)
                exceptionHeader = str;
            else
                exceptionHeader += str;
        } else if (sipHeaderContext) {
            if (testMessage == null)
                testMessage = str;
            else
                testMessage += str;
        } else if (sipURLContext) {
            if (testMessage == null)
                testMessage = str;
            else
                testMessage += str;
        } else if (exceptionMessageContext) {
            if (exceptionMessage == null)
                exceptionMessage = str;
            else
                exceptionMessage += str;
        }
    }

    public void startElement(String namespaceURI, String local, String name,
            Attributes attrs) throws SAXException {
        if (name.compareTo(TORTURE) == 0) {
            outputParsedStructures = false;
            outputResults = false;
            failureFlag = false;
            debugFlag = false;

            String outputParsedString = attrs.getValue(OUTPUT_PARSED_STRUCTURE);
            if (outputParsedString != null
                    && outputParsedString.toLowerCase().compareTo(TRUE) == 0) {
                outputParsedStructures = true;
            }
            String debugString = attrs.getValue(DEBUG);
            if (debugString != null
                    && debugString.toLowerCase().compareTo(TRUE) == 0) {
                debugFlag = true;
            }
            String abortString = attrs.getValue(ABORT_ON_FAIL);
            if (abortString != null && abortString.compareTo(TRUE) == 0) {
                abortOnFail = true;
            } else
                abortOnFail = false;

            outputResults = false;
            String outputResultsString = attrs.getValue(OUTPUT_RESULTS);
            if (outputResultsString != null
                    && outputResultsString.compareTo(TRUE) == 0) {
                outputResults = true;
            } else
                outputResults = false;

            if (outputResults) {

                String testlog = attrs.getValue(TESTLOG);
                if (testlog != null) {
                    try {
                        if (outputResults)
                            testLogWriter = new TestLogWriter(
                                    new FileOutputStream(testlog, true));
                    } catch (IOException ex) {
                        System.err.println("Cannot open " + testlog
                                + " for write");
                        System.exit(0);
                    }
                } else {
                    testLogWriter = System.out;
                }
            }

            emitString(XML_DOCTYPE_IDENTIFIER);

            emitTag(TEST_OUTPUT);
            startTime = System.currentTimeMillis();

        } else if (name.compareTo(TESTCASE) == 0) {
            // Starting a new test
            counter++;
            failureReason = "";
            statusMessage = "";
            failureFlag = false;
            successFlag = false;
            messageContext = false;
            testCaseContext = false;
            messageContext = false;
            descriptionContext = false;
            expectExtensionContext = false;
            sipHeaderContext = false;
            sipURLContext = false;
            testDescription = null;
            testMessage = null;
            encodedMessage = null;
            exceptionMessage = null;
            exceptionTable = new Hashtable();
        } else if (name.compareTo(MESSAGE) == 0) {
            testMessageType = MESSAGE;
            messageContext = true;
        } else if (name.compareTo(SIP_HEADER) == 0) {
            testMessageType = SIP_HEADER;
            sipHeaderContext = true;
        } else if (name.compareTo(SIP_URL) == 0) {
            testMessageType = SIP_URL;
            sipURLContext = true;
        } else if (name.compareTo(DESCRIPTION) == 0) {
            descriptionContext = true;
        } else if (name.compareTo(EXPECT_EXCEPTION) == 0) {
            expectExceptionContext = true;
            exceptionClassName = attrs.getValue(EXCEPTION_CLASS);
        } else if (name.compareTo(EXCEPTION_MESSAGE) == 0) {
            exceptionMessageContext = true;
        } else if (name.compareTo(EXCEPTION_TEXT) == 0) {
            exceptionTextContext = true;
        } else
            throw new SAXException("Unrecognized tag " + name);

    }

    public void error(SAXParseException e) throws SAXParseException {
        throw e;
    }

    public void ignorableWhitespace(char buf[], int offset, int len)
            throws SAXException {
        // Ignore it
    }

    public void endElement(String namespaceURI, String local, String name)
            throws SAXException {
        if (name.compareTo(TESTCASE) == 0) {
            testCaseContext = false;
            stringParser = new StringMsgParser();
            // stringParser.disableInputTracking();

//            stringParser.setParseExceptionListener(this);
            SIPMessage sipMessage = null;
            SIPHeader sipHeader = null;
            SipUri sipURL = null;
            try {
                if (testMessageType.equals(MESSAGE)) {
                    sipMessage = stringParser.parseSIPMessage(testMessage.getBytes(), true, false, (ParseExceptionListener) this);
                    encodedMessage = sipMessage.encode();
                } else if (testMessageType.equals(SIP_HEADER)) {
                    sipHeader = stringParser.parseSIPHeader(testMessage);
                    encodedMessage = sipHeader.encode();
                } else if (testMessageType.equals(SIP_URL)) {
                    sipURL = stringParser.parseSIPUrl(testMessage.trim());
                    encodedMessage = sipURL.encode();
                } else
                    throw new SAXException("Torture: Internal error");
            } catch (ParseException ex) {
                ex.printStackTrace();
                try {
                    handleException(ex, null, new SipUri().getClass(),
                            testMessage, null);
                } catch (ParseException ex1) {
                    ex1.printStackTrace();
                    System.out.println("Unexpected excception!");
                    System.exit(0);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("Unexpected excception!");
                System.exit(0);

            }
            Enumeration e = exceptionTable.elements();
            while (e.hasMoreElements()) {
                ExpectedException ex = (ExpectedException) e.nextElement();
                if (!ex.fired) {
                    formatFailureDiagnostic(ex,
                            "Expected Exception not trapped ");
                } else {
                    formatStatusMessage(ex, "Expected exception generated ");
                }
            }

            emitBeginTag(TESTCASE);
            if (!failureFlag) {
                emitAttribute(STATUS, "PASSED");
            } else {
                emitAttribute(STATUS, "FAILED");
            }
            emitEndTag();
            emitTaggedCData(DESCRIPTION, testDescription);
            if (encodedMessage != null)
                emitTaggedCData(MESSAGE, encodedMessage);
            else
                emitTaggedCData(MESSAGE, testMessage);
            if (failureFlag) {
                emitTaggedData(DIAGNOSTIC_MESSAGES, failureReason);
            }
            if (successFlag) {
                emitTaggedData(DIAGNOSTIC_MESSAGES, statusMessage);
            }
            if (outputParsedStructures) {
                if (sipMessage != null) {
                    emitTaggedCData(PARSED_OUTPUT, sipMessage.toString());
                } else if (sipHeader != null) {
                    emitTaggedCData(PARSED_OUTPUT, sipHeader.toString());
                } else if (sipURL != null) {
                    emitTaggedCData(PARSED_OUTPUT, sipURL.toString());
                }
            }
            emitEndTag(TESTCASE);
            if (failureFlag && abortOnFail) {
                if (testLogWriter != null)
                    testLogWriter.close();
                System.out.println("Failed -- bailing out!");
                System.exit(0);
            }

        } else if (name.compareTo(DESCRIPTION) == 0) {
            descriptionContext = false;
        } else if (name.compareTo(MESSAGE) == 0) {
            messageContext = false;
        } else if (name.compareTo(EXCEPTION_TEXT) == 0) {
            exceptionTextContext = false;
        } else if (name.compareTo(EXPECT_EXCEPTION) == 0) {
            expectExceptionContext = false;
            ExpectedException ex = new ExpectedException(exceptionClassName,
                    exceptionHeader);
            if (exceptionHeader != null) {
                this.exceptionTable.put(exceptionHeader.trim(), ex);
            } else {
                this.exceptionTable.put(testMessage.trim(), ex);
            }
            exceptionHeader = null;
        } else if (name.compareTo(EXCEPTION_MESSAGE) == 0) {
            exceptionMessageContext = false;
            exceptionMessage = null;
        } else if (name.compareTo(SIP_HEADER) == 0) {
            sipHeaderContext = false;
        } else if (name.compareTo(SIP_URL) == 0) {
            sipURLContext = false;
        } else if (name.compareTo(TORTURE) == 0) {
            emitEndTag(TEST_OUTPUT);
            if (outputResults)
                testLogWriter.close();
        }

    }

    protected void emitBeginTag(String tagName) {
        if (outputResults)
            testLogWriter.println(LESS_THAN + tagName);
    }

    protected void emitTag(String tagName) {
        if (outputResults)
            testLogWriter.println(LESS_THAN + tagName + GREATER_THAN);
    }

    protected void emitTaggedData(String tag, String data) {
        if (outputResults) {
            emitTag(tag);
            testLogWriter.println(data);
            emitEndTag(tag);
        }
    }

    /* A stand alone tag */
    protected void emitBeginEndTag(String tagName) {
        if (outputResults) {
            testLogWriter.println(LESS_THAN + tagName + SLASH + GREATER_THAN);
        }
    }

    protected void emitEndTag() {
        if (outputResults)
            testLogWriter.println(GREATER_THAN);
    }

    protected void emitEndTag(String tagName) {
        if (outputResults)
            testLogWriter.println(LESS_THAN + SLASH + tagName + GREATER_THAN);
    }

    protected void emitTaggedCData(String str) {
        if (outputResults)
            testLogWriter.println("<![CDATA[" + str + "]]>");

    }

    protected void emitTaggedCData(String tag, String str) {
        if (outputResults) {
            emitTag(tag);
            testLogWriter.println("<![CDATA[" + str + "]]>");
            emitEndTag(tag);
        }

    }

    protected void emitString(String str) {
        if (outputResults)
            testLogWriter.println(str);
    }

    protected void emitAttribute(String attrName, String attrval) {
        if (outputResults)
            testLogWriter.println(attrName + EQUALS + "\"" + attrval + "\"");
    }

    protected String emitStringAttribute(String attrName, String attrval) {
        return attrName + EQUALS + "\"" + attrval + "\"" + "\n";
    }

    protected String emitStringTag(String tag) {
        return "<" + tag + ">" + "\n";
    }

    protected String emitStringBeginTag(String tag) {
        return "<" + tag + "\n";
    }

    protected String emitStringEndTag() {
        return ">" + "\n";
    }

    protected String emitStringEndTag(String tag) {
        return "</" + tag + ">\n";
    }

    protected String emitStringCData(String msg) {
        return "<![CDATA[" + msg + "]]>\n";
    }

    protected String emitStringCData(String tag, String msg) {
        return emitStringTag(tag) + emitStringCData(msg)
                + emitStringEndTag(tag);
    }

    protected void formatFailureDiagnostic(ParseException ex, String message) {

        failureReason += emitStringBeginTag(DIAGNOSTIC);
        failureReason += emitStringAttribute(REASON, message);
        failureReason += emitStringEndTag();

        failureReason += emitStringAttribute(EXCEPTION_CLASS, ex.getClass()
                .getName());

        failureReason += emitStringCData(EXCEPTION_TEXT, ex.getMessage());

        // DataOutputStream dos = new DataOutputStream(bos);
        ByteArrayOutputStream bos = new ByteArrayOutputStream(4096);
        ex.printStackTrace(new PrintStream(bos, true));
        failureReason += emitStringCData(STACK_TRACE, bos.toString());
        failureReason += emitStringCData(MESSAGE, ex.getMessage());
        failureReason += emitStringEndTag(DIAGNOSTIC);
    }

    protected void formatFailureDiagnostic(String hdr, String reason) {
        failureReason += emitStringBeginTag(DIAGNOSTIC);
        failureReason += emitStringAttribute(STATUS, "BAD");
        failureReason += emitStringAttribute(REASON, reason);
        failureReason += emitStringEndTag();
        failureReason += emitStringCData(EXCEPTION_TEXT, hdr);
        failureReason += emitStringEndTag(DIAGNOSTIC);
    }

    protected void formatFailureDiagnostic(ExpectedException ex, String reason) {
        failureReason += emitStringBeginTag(DIAGNOSTIC);
        failureReason += emitStringAttribute(STATUS, "BAD");
        failureReason += emitStringAttribute(REASON, reason);
        failureReason += emitStringAttribute(EXCEPTION_CLASS, ex.exceptionClass
                .getName());
        failureReason += emitStringEndTag();
        failureReason += emitStringCData(EXCEPTION_TEXT, ex.exceptionHeader);
        failureReason += emitStringEndTag(DIAGNOSTIC);
    }

    protected void formatStatusMessage(String hdr, String reason) {
        statusMessage += emitStringBeginTag(DIAGNOSTIC);
        statusMessage += emitStringAttribute(STATUS, "OK");
        statusMessage += emitStringAttribute(REASON, reason);
        statusMessage += emitStringEndTag();
        statusMessage += emitStringCData(EXCEPTION_TEXT, hdr);
        statusMessage += emitStringEndTag(DIAGNOSTIC);
    }

    protected void formatStatusMessage(ExpectedException ex, String reason) {
        statusMessage += emitStringBeginTag(DIAGNOSTIC);
        statusMessage += emitStringAttribute(STATUS, "OK");
        statusMessage += emitStringAttribute(REASON, reason);
        statusMessage += emitStringAttribute(EXCEPTION_CLASS, ex.exceptionClass
                .getName());
        statusMessage += emitStringEndTag();
        statusMessage += emitStringCData(EXCEPTION_TEXT, ex.exceptionHeader);
        statusMessage += emitStringEndTag(DIAGNOSTIC);
    }

    public Torture () {

    }
    public void doTests() throws Exception {
        String fileName ;
        fileName = "torture.xml";

        /* The tests do not check for content length */
        StringMsgParser.setComputeContentLengthFromMessage(true);

        try {
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            XMLReader saxParser = saxParserFactory.newSAXParser()
                    .getXMLReader();

            saxParser.setEntityResolver(new MyEntityResolver());
            saxParser.setContentHandler(this);
            saxParser
                    .setFeature("http://xml.org/sax/features/validation", true);
            saxParser.parse(new InputSource ( Torture.class.getResourceAsStream(fileName)));
            System.out.println("Elapsed time = "
                    + (System.currentTimeMillis() - startTime) / counter);

        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }

    }

}
