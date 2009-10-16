package examples.nistgoodies.pluggablelogger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.SimpleLayout;

import gov.nist.core.StackLogger;

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

    @Override
    public void disableLogging() {
        enabled = false;
    }

    @Override
    public void enableLogging() {
        enabled = true;

    }

    @Override
    public int getLineCount() {
        return 0;
    }

    @Override
    public boolean isLoggingEnabled() {   
        return enabled;
    }

    @Override
    public boolean isLoggingEnabled(int sipLogLevel) {
       System.out.println("level " + logger.getLevel());
       int levelSet = levelMap.get( logger.getLevel().toString());
       return sipLogLevel <= levelSet;
    }

    @Override
    public void logDebug(String string) {
       logger.debug(string);

    }

    @Override
    public void logError(String string) {
        logger.error(string);
    }

    @Override
    public void logError(String string, Exception exception) {
      logger.error(string,exception);

    }

    @Override
    public void logException(Throwable throwable) {
        logger.error("Exception occured",throwable);
    }

    @Override
    public void logFatalError(String string) {     
        logger.fatal("Fatal error " + string);
    }

    @Override
    public void logInfo(String string) { 
        logger.info(string);
    }

    @Override
    public void logStackTrace() {
        if (enabled) {
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

    @Override
    public void logStackTrace(int level) {
        if ( this.isLoggingEnabled(level)) {
            logStackTrace();
        }

    }

    @Override
    public void logWarning(String message) {
        logger.warn(message);
    }

    @Override
    public void setBuildTimeStamp(String timeStamp) {
       logger.info("BuildTimeStamp = " + timeStamp);
    }

    @Override
    public void setStackProperties(Properties properties) {
        logger.info("StackProperties " + properties);
    }

    @Override
    public String getLoggerName() {
        return logger.getName();
    }


}
