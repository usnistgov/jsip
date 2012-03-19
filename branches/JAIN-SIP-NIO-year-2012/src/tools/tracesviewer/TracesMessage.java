package tools.tracesviewer;

import java.io.*;

public class TracesMessage implements Serializable {

    String messageFrom = null;
    String messageTo = null;
    String messageTime = null;
    String messageTimeStamp = null;
    String messageString = null;
    String messageFirstLine = null;
    String messageStatusInfo = null;
    String messageTransactionId = null;
    String debugLine = null;

    String beforeDebug;
    String afterDebug;

    public TracesMessage() {
    }

    public TracesMessage(
        String messageFrom,
        String messageTo,
        String messageTime,
        String messageFirstLine,
        String messageString,
        String messageStatusInfo,
        String messageTransactionId,
        String messageTimeStamp,
        String debugLine) {
        this.messageFrom = messageFrom;
        this.messageTo = messageTo;
        this.messageTime = messageTime;
        this.messageString = messageString;
        this.messageFirstLine = messageFirstLine;
        this.messageStatusInfo = messageStatusInfo;
        this.messageTransactionId = messageTransactionId;
        this.messageTimeStamp = messageTimeStamp;
        this.debugLine = debugLine;
    }

    private String getKey() {
        if ( messageTimeStamp != null) {
            return messageFirstLine+ messageTransactionId+":"+messageTimeStamp;
        } else {
            // Generate a random time stamp
            return messageFirstLine+ messageTransactionId+ ":" + (int) ( Math.random()*1000);
        }
    }

    /**
     * To check for insertion into the hash table.
     *
     */
    public int hashCode() {
        return getKey().hashCode();
    }


    public void setFrom(String from) {
        messageFrom = from;
    }

    public void setTo(String to) {
        messageTo = to;
    }

    public void setTime(String time) {
        messageTime = time;
    }

    public void setMessageString(String str) {
        messageString = str;
    }

    public void setFirstLine(String FirstLine) {
        messageFirstLine = FirstLine;
    }

    public void setStatusInfo(String statusInfo) {
        messageStatusInfo = statusInfo;
    }

    public void setTransactionId(String transactionId) {
        messageTransactionId = transactionId;
    }

    public String getFrom() {
        return messageFrom;
    }

    public String getTo() {
        return messageTo;
    }

    public String getTime() {
        return messageTime;
    }

    public String getMessageString() {
        //System.out.println("messageContent:"+messageString);
        return messageString;
        //+
        //"\n-------------------\n"      +
        //"|debugLogLine = " + debugLine +" |"  +
        //"\n-------------------";
    }

    public String getFirstLine() {
        return messageFirstLine;
    }

    public String getStatusInfo() {
        return messageStatusInfo;
    }

    public String getTransactionId() {
        return messageTransactionId;
    }
}
