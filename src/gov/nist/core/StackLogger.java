package gov.nist.core;

import java.util.Properties;

/**
 * interface that loggers should implement so that the stack can log to various
 * loggers impl such as log4j, commons logging, sl4j, ...
 * @author jean.deruelle@gmail.com
 *
 */
public interface StackLogger {
	 /**
     * Dont trace
     */
    public static final int TRACE_NONE = 0;

    /**
     * Trace message processing
     */
    public static final int TRACE_MESSAGES = 16;

    /**
     * Trace exception processing
     */
    public static final int TRACE_EXCEPTION = 17;

    /**
     * Debug trace level (all tracing enabled).
     */
    public static final int TRACE_DEBUG = 32;

	/**
     * log a stack trace. This helps to look at the stack frame.
     */
	public void logStackTrace();
	
	public void logStackTrace(int traceLevel);
	
	/**
     * Get the line count in the log stream.
     *
     * @return
     */
	public int getLineCount();
	
	/**
     * Log an exception.
     *
     * @param ex
     */
    public void logException(Throwable ex);
    /**
     * Log a message into the log file.
     *
     * @param message
     *            message to log into the log file.
     */
    public void logDebug(String message);
    /**
     * Log an error message.
     *
     * @param message --
     *            error message to log.
     */
    public void logFatalError(String message);
    /**
     * Log an error message.
     *
     * @param message --
     *            error message to log.
     *
     */
    public void logError(String message);
    /**
     * @return flag to indicate if logging is enabled.
     */
    public boolean isLoggingEnabled();
    /**
     * Return true/false if loging is enabled at a given level.
     *
     * @param logLevel
     */
    public boolean isLoggingEnabled(int logLevel);
    /**
     * Log an error message.
     *
     * @param message
     * @param ex
     */
    public void logError(String message, Exception ex);
    /**
     * Log a warning mesasge.
     *
     * @param string
     */
    public void logWarning(String string);
    /**
     * Log an info message.
     *
     * @param string
     */
    public void logInfo(String string);
    /**
     * Disable logging altogether.
     *
     */
    public void disableLogging();

    /**
     * Enable logging (globally).
     */
    public void enableLogging();
    
    public void setBuildTimeStamp(String buildTimeStamp);
    
    public void setStackProperties(Properties stackProperties);
}
