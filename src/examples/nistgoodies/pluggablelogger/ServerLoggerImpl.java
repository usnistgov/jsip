package examples.nistgoodies.pluggablelogger;

import java.util.Properties;

import javax.sip.SipStack;
import javax.sip.header.TimeStampHeader;

import gov.nist.core.ServerLogger;
import gov.nist.javax.sip.LogRecord;
import gov.nist.javax.sip.LogRecordFactory;
import gov.nist.javax.sip.SipStackImpl;
import gov.nist.javax.sip.header.CallID;
import gov.nist.javax.sip.message.SIPMessage;

public class ServerLoggerImpl implements ServerLogger {
   
    private SipStackImpl sipStack;

    private LogRecordFactory logRecordFactory;
    
    
    public ServerLoggerImpl() {
        this.logRecordFactory = new LogRecordFactoryImpl();
    }

    @Override
    public void closeLogFile() {
    
    }

    @Override
    public void logException(Exception exception) {
        sipStack.getStackLogger().logStackTrace();
    }

    @Override
    public void logMessage(SIPMessage message, String source, String destination, boolean isSender, long timeStamp) {
        String firstLine = message.getFirstLine();
        String tid = message.getTransactionId();
        String callId = message.getCallId().getCallId();
        
        LogRecord logRecord = logRecordFactory.createLogRecord(message.encode(), source, destination, timeStamp, isSender, firstLine, tid, callId, 
                0);
        sipStack.getStackLogger().logInfo(logRecord.toString());
        
    }

    @Override
    public void logMessage(SIPMessage message, String from, String to, String status, boolean sender) {
        logMessage(message, from, to, status, sender, System.currentTimeMillis());
    }

    @Override
    public void logMessage(SIPMessage message, String source, String destination, String status, boolean isSender,
            long timeStamp) {
        // TODO Auto-generated method stub
        CallID cid = (CallID) message.getCallId();
        String callId = null;
        if (cid != null)
            callId = cid.getCallId();
        String firstLine = message.getFirstLine().trim();
        String tid = message.getTransactionId();
        TimeStampHeader tshdr = (TimeStampHeader) message.getHeader(TimeStampHeader.NAME);
        long tsval = tshdr == null ? 0 : tshdr.getTime();
        LogRecord logRecord = logRecordFactory.createLogRecord(message.encode(), source, destination, timeStamp, isSender, firstLine, tid, callId, 
                tsval);
        sipStack.getStackLogger().logInfo(logRecord.toString());
     
    }

    @Override
    public void setSipStack(SipStack sipStack) {
        this.sipStack = (SipStackImpl) sipStack;
       
    }

    @Override
    public void setStackProperties(Properties properties) {
       
    }
    
   
}
