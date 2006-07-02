/*
* Conditions Of Use 
* 
* This software was developed by employees of the National Institute of
* Standards and Technology (NIST), an agency of the Federal Government.
* Pursuant to title 15 Untied States Code Section 105, works of NIST
* employees are not subject to copyright protection in the United States
* and are considered to be in the public domain.  As a result, a formal
* license is not needed to use the software.
* 
* This software is provided by NIST as a service and is expressly
* provided "AS IS."  NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
* OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
* AND DATA ACCURACY.  NIST does not warrant or make any representations
* regarding the use of the software or the results thereof, including but
* not limited to the correctness, accuracy, reliability or usefulness of
* the software.
* 
* Permission to use this software is contingent upon your acceptance
* of the terms of this agreement.
* 
*/
/***************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).    *
 ***************************************************************************/

package gov.nist.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

/**
 * A wrapper around log4j that is used for logging debug and errors. You can replace this 
 * file if you want to change the way in which messages are logged.
 * 
 * @version 1.2
 * 
 * @author M. Ranganathan <br/>
 * @author M.Andrews
 * @author Jeroen van Bemmel
 * 
 */

public class LogWriter {

	
	private Logger logger;

	private String stackName;

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
	 * Name of the log file in which the trace is written out (default is
	 * /tmp/sipserverlog.txt)
	 */
	private String logFileName = "debuglog.txt";

	
	/**
	 * Flag to indicate that logging is enabled.
	 */
	private volatile boolean needsLogging = false;

	private int lineCount;

	

	/**
	 * trace level
	 */

	protected  int traceLevel = TRACE_NONE;

	/**
	 * log a stack trace. This helps to look at the stack frame.
	 */
	public void logStackTrace() {
		if (needsLogging) {
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

			this.logDebug(stackTrace);

		}
	}

	public int getLineCount() {
		return lineCount;
	}

	private Logger getLogger() {
		return logger;
	}

	public void logException(Throwable ex) {

		if (needsLogging) {

			this.getLogger().error(ex.getMessage(), ex);
		}
	}

	public void logThrowable(Throwable throwable) {
		if (needsLogging) {

			getLogger().error(throwable);
		}
	}

	/**
	 * Set the log file name
	 * 
	 * @param name
	 *            is the name of the log file to set.
	 */
	public void setLogFileName(String name) {
		
		this.logFileName = name;
	}

	/**
	 * Counts the line number so that the debug log can be correlated to the
	 * message trace.
	 * 
	 * @param message --
	 *            message to count the lines for.
	 */
	private void countLines(String message) {
		char[] chars = message.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == '\n')
				lineCount++;
		}

	}

	/**
	 * Prepend the line and file where this message originated from
	 * 
	 * @param message
	 * @return re-written message.
	 */
	private String enhanceMessage(String message) {

		StackTraceElement[] stackTrace = new Exception().getStackTrace();
		StackTraceElement elem = stackTrace[2];
		String className = elem.getClassName();
		String methodName = elem.getMethodName();
		String fileName = elem.getFileName();
		int lineNumber = elem.getLineNumber();
		String newMessage = className + "." + methodName + "(" + fileName + ":"
				+ lineNumber + ") [" + message + "]";
		return newMessage;

	}

	/**
	 * Log a message into the log file.
	 * 
	 * @param message
	 *            message to log into the log file.
	 */
	public void logDebug(String message) {
		if (needsLogging) {
			String newMessage = this.enhanceMessage(message);
			countLines(newMessage);
			getLogger().debug(newMessage);
		}

	}

	/**
	 * Set the trace level for the stack.
	 */
	private void setTraceLevel(int level) {
		traceLevel = level;
	}

	/**
	 * Get the trace level for the stack.
	 */
	public int getTraceLevel() {
		return traceLevel;
	}

	/**
	 * Log an error message.
	 * 
	 * @param message --
	 *            error message to log.
	 */
	public void logFatalError(String message) {
		Logger logger = this.getLogger();
		String newMsg = this.enhanceMessage(message);
		countLines(newMsg);
		logger.fatal(newMsg);

	}

	/**
	 * Log an error message.
	 * 
	 * @param message --
	 *            error message to log.
	 * 
	 */
	public void logError(String message) {
		Logger logger = this.getLogger();
		String newMsg = this.enhanceMessage(message);
		countLines(newMsg);
		logger.error(newMsg);

	}

	/**
	 * @param configurationProperties
	 */
	public LogWriter(Properties configurationProperties) {
		String logLevel = configurationProperties
				.getProperty("gov.nist.javax.sip.TRACE_LEVEL");

		
		this.logFileName = configurationProperties
				.getProperty("gov.nist.javax.sip.DEBUG_LOG");
		
		this.stackName = configurationProperties
				.getProperty("javax.sip.STACK_NAME");
		String category = this.stackName;
		//	This is the default log file name
		
		if ( this.logFileName == null && logLevel != null ) 
			this.logFileName = stackName + "debuglog.txt";
		
		logger = Logger.getLogger(category);
		if (logLevel != null) {
			try {
				int ll = 0;
				if (logLevel.equals("DEBUG")) {
					ll = TRACE_DEBUG;
				} else if (logLevel.equals("TRACE")) {
					ll = TRACE_MESSAGES;
				} else if (logLevel.equals("ERROR")) {
					ll = TRACE_EXCEPTION;
				} else if (logLevel.equals("NONE")) {
					ll = TRACE_NONE;
				} else {
					ll = Integer.parseInt(logLevel);
				}

				this.setTraceLevel(ll);
				this.needsLogging = true;
				if (traceLevel == TRACE_DEBUG) {
					logger.setLevel(Level.DEBUG);
				} else if (traceLevel == TRACE_MESSAGES) {
					logger.setLevel(Level.INFO);
				} else if (traceLevel == TRACE_EXCEPTION) {
					logger.setLevel(Level.ERROR);
				} else if (traceLevel == TRACE_NONE) {
					logger.setLevel(Level.OFF);
					this.needsLogging = false;
				}

				FileAppender fa = null;
				if (this.needsLogging) {
					try {
						fa = new FileAppender(new SimpleLayout(),this.logFileName);
					} catch (FileNotFoundException fnf) { 
				
						// Likely due to some directoy not existing. Create them
						File logfile = new File( this.logFileName );
						logfile.getParentFile().mkdirs();
						logfile.delete();
						
						try {
							fa = new FileAppender(new SimpleLayout(),this.logFileName);						
						} catch (IOException ioe) {
							ioe.printStackTrace();	// give up
						}
					} catch (IOException ex) {
						ex.printStackTrace();
					}
					if (fa!=null) logger.addAppender(fa);
				}
				
			} catch (NumberFormatException ex) {
				ex.printStackTrace();
				System.out.println("LogWriter: Bad integer " + logLevel);
				System.out.println("logging dislabled ");
				needsLogging = false;
			}
		} else {
			this.needsLogging = false;

		}

	}

	/**
	 * @return flag to indicate if logging is enabled.
	 */
	public boolean isLoggingEnabled() {

		return this.needsLogging;
	}
	
	public boolean isLoggingEnabled( int logLevel ) {
		return this.needsLogging && logLevel < traceLevel;
	}

	public void logError(String message, Exception ex) {
		Logger logger = this.getLogger();
		logger.error(message, ex);

	}

	public void logWarning(String string) {
		getLogger().warn(string);
		
	}
	
	public void disableLogging() {
		this.needsLogging = false;
	}

	public void enableLogging() {
		this.needsLogging = true;
		
	}

}
