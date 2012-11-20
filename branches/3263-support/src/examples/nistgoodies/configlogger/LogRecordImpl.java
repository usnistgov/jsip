package examples.nistgoodies.configlogger;

import gov.nist.javax.sip.LogRecord;

public class LogRecordImpl implements LogRecord {

    private String message;
    private String source;
    private String destination;
    private long timeStamp;
    private String tid;
    private String firstLine;
    private String callId;
    private long timestampVal;

    /**
     * Constructor for our custom log record
     *
     * @param message --
     *            the message to log
     * @param source --
     *            the source
     * @param destination --
     *            the destination
     * @param timeStamp --
     *            the reception time
     * @param isSender --
     *            the flag indicates whether we are sending or recieving the
     *            record
     * @param firstLine --
     *            the messge first line
     * @param tid --
     *            the transaction id
     * @param callId --
     *            the callId
     * @param timestampVal --
     *            the timestamp Header value.
     */
    public LogRecordImpl(String message, String source, String destination,
            long timeStamp, boolean isSender, String firstLine, String tid,
            String callId, long timestampVal) {
        this.message = message;
        this.source = source;
        this.destination = destination;
        this.timeStamp = timeStamp;
        this.firstLine = firstLine;
        this.tid = tid;
        this.callId = callId;
        this.timestampVal = timestampVal;
    }

    public boolean equals(Object other) {
        if (!(other instanceof LogRecordImpl)) {
            return false;
        } else {
            LogRecordImpl otherLog = (LogRecordImpl) other;
            return otherLog.message.equals(message)
                && otherLog.timeStamp == timeStamp;
        }
    }


    public String toString() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("------------  Message BEGIN ----------- \n");
        sbuf.append("timeStamp = " + this.timeStamp + "\n");
        sbuf.append(this.message);
        return sbuf.toString();
    }

}
