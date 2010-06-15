package examples.nistgoodies.pluggablelogger;

import gov.nist.core.StackLogger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

public class StackLoggerImpl implements StackLogger {
    
    private static Logger logger = Logger.getLogger(StackLoggerImpl.class) ;
    
    
    private static HashMap<String,Integer> levelMap = new HashMap<String,Integer>();
    
    private static HashMap<Integer,String> inverseLevelMap = new HashMap<Integer,String>();
    
    boolean enabled = true;
 
    private static void putMap(String level, int jsipLevel) {
        levelMap.put(level, jsipLevel);
        inverseLevelMap.put(new Integer(jsipLevel), level);
    }
    static {
        putMap(Level.DEBUG.toString(), new Integer(TRACE_DEBUG));
        putMap(Level.INFO.toString(), new Integer(TRACE_INFO));
        putMap(Level.TRACE.toString(), new Integer(TRACE_TRACE));
        putMap(Level.ERROR.toString(), new Integer(TRACE_ERROR));
        putMap(Level.WARN.toString(), new Integer(TRACE_WARN));
        putMap(Level.FATAL.toString(), new Integer(TRACE_FATAL));
        putMap(Level.OFF.toString(), new Integer(TRACE_NONE));
        logger.addAppender(new ConsoleAppender(new SimpleLayout()));
    }
    
    
    public StackLoggerImpl( ) {
        logger.setLevel(Level.DEBUG);
        logger.addAppender(new ConsoleAppender());
    }
    
 
    public static void setLogger(Logger logger) {
        StackLoggerImpl.logger = logger;
    }

    public void disableLogging() {
        enabled = false;
    }

    public void enableLogging() {
        enabled = true;

    }

    public int getLineCount() {
        return 0;
    }

    public boolean isLoggingEnabled() {   
        return enabled;
    }

    public boolean isLoggingEnabled(int sipLogLevel) {
       int levelSet = levelMap.get( logger.getLevel().toString());
       return sipLogLevel <= levelSet;
    }

    public void logDebug(String string) {
       logger.debug(string);

    }

    public void logError(String string) {
        logger.error(string);
    }

    public void logError(String string, Exception exception) {
      logger.error(string,exception);

    }

    public void logException(Throwable throwable) {
        logger.error("Exception occured",throwable);
    }

    public void logFatalError(String string) {     
        logger.fatal("Fatal error " + string);
    }

    public void logInfo(String string) { 
        logger.info(string);
    }

    public void logStackTrace() {
        if (this.isLoggingEnabled(TRACE_DEBUG)) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            StackTraceElement[] ste = new Exception().getStackTrace();
            // Skip the log writer frame and log all the other stack frames.
            for (int i = 1; i < ste.length; i++) {
                String callFrame = "[" + ste[i].getFileName() + ":"
                        + ste[i].getLineNumber() + "]";
                pw.print(callFrame);
            }
            pw.close();
            String stackTrace = sw.getBuffer().toString();
            logDebug(stackTrace);

        }
        
    }

    public void logStackTrace(int level) {
        if ( this.isLoggingEnabled(level)) {
            logStackTrace();
        }

    }

    public void logWarning(String message) {
        logger.warn(message);
    }
    
    public void logTrace(String message) {
        logger.trace(message);
    }

    public void setBuildTimeStamp(String timeStamp) {
       logger.info("BuildTimeStamp = " + timeStamp);
    }

    public void setStackProperties(Properties properties) {
        logger.info("StackProperties " + properties);
    }

    public String getLoggerName() {
        return logger.getName();
    }


}
