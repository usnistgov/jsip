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
* of the terms of this agreement
*  
* .
* 
*/
/*******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).       *
 ******************************************************************************/

package gov.nist.javax.sip.stack;
import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.PrintWriter;

import gov.nist.javax.sip.LogRecord;
import gov.nist.javax.sip.message.*;
import gov.nist.javax.sip.header.*;
import gov.nist.core.*;

import java.util.Properties;

import javax.sip.header.TimeStampHeader;

/**
 * Log file wrapper class.
 * Log messages into the message trace file and also write the log into the
 * debug file if needed. This class keeps an XML formatted trace around for
 * later access via RMI. The trace can be viewed with a trace viewer (see
 * tools.traceviewerapp).
 *
 * @version 1.2 $Revision: 1.26 $ $Date: 2007-04-10 15:07:56 $
 *
 * @author M. Ranganathan   <br/>
 *
 * 
 */
public class ServerLog {

	private boolean logContent;

	
	protected LogWriter logWriter;

	/**
	 * Dont trace
	 */
	public static final int TRACE_NONE = 0;

	public static final int TRACE_MESSAGES = LogWriter.TRACE_MESSAGES;
	
	/**
	 * Trace exception processing
	 */
	public static final int TRACE_EXCEPTION = 17;

	/**
	 * Debug trace level (all tracing enabled).
	 */
	public static final int TRACE_DEBUG = 32;

	/**
	 * Name of the log file in which the trace is written out
	 * (default is null)
	 */
	private String logFileName;

	/**
	 * Print writer that is used to write out the log file.
	 */
	protected PrintWriter printWriter;

	/**
	 * print stream for writing out trace
	 */
	protected PrintStream traceWriter = System.out;

	/**
	 * Name to assign for the log.
	protected String logRootName;
	 */

	/**
	 * Set auxililary information to log with this trace.
	 */
	protected String auxInfo;

	protected String description;

	protected String stackIpAddress;

	private SIPTransactionStack sipStack;
	


	private Properties configurationProperties;

	public ServerLog(SIPTransactionStack sipStack,Properties configurationProperties ) {
	    // Debug log file. Whatever gets logged by us also makes its way into debug log.
		this.logWriter = sipStack.logWriter;
		this.sipStack = sipStack;
		this.setProperties(configurationProperties);
	}
	
	private void setProperties( Properties configurationProperties) {
		this.configurationProperties = configurationProperties;
		// Set a descriptive name for the message trace logger.
		this.description = 
			configurationProperties.getProperty
			("javax.sip.STACK_NAME") ;
		this.stackIpAddress = configurationProperties.getProperty
			("javax.sip.IP_ADDRESS") ;
		this.logFileName = 
			 configurationProperties.getProperty
			("gov.nist.javax.sip.SERVER_LOG") ;
		String logLevel =
			configurationProperties.getProperty(
				"gov.nist.javax.sip.TRACE_LEVEL");

		if (logLevel != null) {
			try {
				int ll;
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
			} catch (NumberFormatException ex) {
				System.out.println("ServerLog: WARNING Bad integer " + logLevel);
				System.out.println("logging dislabled ");
				this.setTraceLevel(0);
			}
		}
		checkLogFile();
		
	}

	public void setStackIpAddress(String ipAddress) {
		this.stackIpAddress = ipAddress;
	}

	

	//public static boolean isWebTesterCatchException=false;
	//public static String webTesterLogFile=null;

	/**
	 *  Debugging trace stream.
	 */
	private PrintStream trace = System.out;

	/**
	 * default trace level
	 */
	protected int traceLevel = TRACE_MESSAGES;

	public void closeLogFile() {
		if ( printWriter != null ) {
			printWriter.close();
			printWriter = null;
		}
	}
	public void checkLogFile() {
		if (logFileName == null || traceLevel < TRACE_MESSAGES) {
			// Dont create a log file if tracing is
			// disabled.
			return;
		}
		try {
			File logFile = new File(logFileName);
			if (!logFile.exists()) {
				logFile.createNewFile();
				printWriter = null;
			}
			// Append buffer to the end of the file.
			if (printWriter == null) {
				String s = configurationProperties.getProperty
						("gov.nist.javax.sip.LOG_MESSAGE_CONTENT");
				this.logContent =  (s != null && s.equals("true"));
				FileWriter fw = new FileWriter(logFileName, true);
				printWriter = new PrintWriter(fw, true);
				printWriter.println(
					"<!-- "
						+ "Use the  Trace Viewer in src/tools/tracesviewer to"		 	+
						" view this  trace  \n" 						+
						"Here are the stack configuration properties \n"			+
						"javax.sip.IP_ADDRESS= " 						+ 
						configurationProperties.getProperty("javax.sip.IP_ADDRESS") + "\n" 	+
						"javax.sip.STACK_NAME= " 						+ 
						configurationProperties.getProperty("javax.sip.STACK_NAME") + "\n" 	+
						"javax.sip.ROUTER_PATH= " 						+ 
						configurationProperties.getProperty("javax.sip.ROUTER_PATH") + "\n" 	+
						"javax.sip.OUTBOUND_PROXY= " 						+ 
						configurationProperties.getProperty("javax.sip.OUTBOUND_PROXY") + "\n" 	+
						 "-->");
				if (auxInfo != null) {
					/* printWriter.println(
						"<description\n logDescription=\""
							+ description
							+ "\"\n name=\""
							+ stackIpAddress
							+ "\"\n auxInfo=\""
							+ auxInfo
							+ "\"/>\n "); */
					if (sipStack.isLoggingEnabled()) {
					    	logWriter.logDebug(
						"Here are the stack configuration properties \n"			+
						"javax.sip.IP_ADDRESS= " 						+ 
						configurationProperties.getProperty("javax.sip.IP_ADDRESS") + "\n" 	+
						"javax.sip.ROUTER_PATH= " 						+ 
						configurationProperties.getProperty("javax.sip.ROUTER_PATH") + "\n" 	+
						"javax.sip.OUTBOUND_PROXY= " 						+ 
						configurationProperties.getProperty("javax.sip.OUTBOUND_PROXY") + "\n" 	+
						"gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS= " 					  + 
						configurationProperties.getProperty("gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS") + "\n" +
						"gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS= " 					  + 
						configurationProperties.getProperty("gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS") + "\n" +
						"gov.nist.javax.sip.REENTRANT_LISTENER= " 						  + 
						configurationProperties.getProperty("gov.nist.javax.sip.REENTRANT_LISTENER")  		  +
						"gov.nist.javax.sip.THREAD_POOL_SIZE= " 						  + 
						configurationProperties.getProperty("gov.nist.javax.sip.THREAD_POOL_SIZE")  		  +
						"\n" );
						logWriter.logDebug(" ]]> ");
						logWriter.logDebug("</debug>");
						logWriter.logDebug(
							"<description\n logDescription=\""
								+ description
								+ "\"\n name=\""
								+ stackIpAddress
								+ "\"\n auxInfo=\""
								+ auxInfo
								+ "\"/>\n ");
						logWriter.logDebug("<debug>");
						logWriter.logDebug("<![CDATA[ ");
					}
				} else {
					/* printWriter.println(
						"<description\n logDescription=\""
							+ description
							+ "\"\n name=\""
							+ stackIpAddress
							+ "\" />\n"); */
					if (sipStack.isLoggingEnabled()) {
					    	logWriter.logDebug(
						"Here are the stack configuration properties \n" +						 
						configurationProperties + "\n");
						logWriter.logDebug(" ]]>");
						logWriter.logDebug("</debug>");
						logWriter.logDebug(
							"<description\n logDescription=\""
								+ description
								+ "\"\n name=\""
								+ stackIpAddress
								+ "\" />\n");
						logWriter.logDebug("<debug>");
						logWriter.logDebug("<![CDATA[ ");
					}
				}
			}
		} catch (IOException ex) {

		}
	}

	

	/**
	 * Global check for whether to log or not. To minimize the time
	 * return false here.
	 *
	 * @return true -- if logging is globally enabled and false otherwise.
	 *
	 */
	public boolean needsLogging() {
		return logFileName != null;
	}

	/**
	 * Set the log file name
	 * @param name is the name of the log file to set.
	 */
	public void setLogFileName(String name) {
		logFileName = name;
	}

	/**
	 * return the name of the log file.
	 */
	public String getLogFileName() {
		return logFileName;
	}

	/**
	 * Log a message into the log file.
	 * @param message message to log into the log file.
	 */
	private void logMessage(String message) {
		// String tname = Thread.currentThread().getName();
		checkLogFile();
		String logInfo = message;
		if (printWriter == null) {
			System.out.println(logInfo);
		} else {
			printWriter.println(logInfo);
		}
		if (sipStack.isLoggingEnabled()) {
			logWriter.logDebug(" ]]>");
			logWriter.logDebug("</debug>");
			logWriter.logDebug(logInfo);
			logWriter.logDebug("<debug>");
			logWriter.logDebug("<![CDATA[ ");
		}
	}

	
	private  void logMessage(
		String message,
		String from,
		String to,
		boolean sender,
		String callId,
		String firstLine,
		String status,
		String tid,
		long time,
		long timestampVal) {

	
		LogRecord log =
			this.sipStack.logRecordFactory.createLogRecord(
				message,
				from,
				to,
				time,
				sender,
				firstLine,
				tid,
				callId,
				timestampVal);
		if ( log != null )
			logMessage(log.toString());
	}

	

	

	/**
	 * Log a message into the log directory.
	 * @param message a SIPMessage to log
	 * @param from from header of the message to log into the log directory
	 * @param to to header of the message to log into the log directory
	 * @param sender is the server the sender
	 * @param time is the time to associate with the message.
	 */
	public void logMessage(
		SIPMessage message,
		String from,
		String to,
		boolean sender,
		long time) {
		checkLogFile();
		CallID cid = (CallID) message.getCallId();
		String callId = null;
		if (cid != null)
			callId = cid.getCallId();
		String firstLine = message.getFirstLine().trim();
		String inputText = (logContent ? message.encode() : message.encodeMessage() ) ;
		String tid = message.getTransactionId();
		TimeStampHeader tsHdr = (TimeStampHeader) message.getHeader(TimeStampHeader.NAME);
		long tsval = tsHdr == null ? 0 : tsHdr.getTime();
		logMessage(
			inputText,
			from,
			to,
			sender,
			callId,
			firstLine,
			null,
			tid,
			time,
			tsval);
	}

	
	

	/**
	 * Log a message into the log directory.
	 * @param message a SIPMessage to log
	 * @param from from header of the message to log into the log directory
	 * @param to to header of the message to log into the log directory
	 * @param status the status to log. 
	 * @param sender is the server the sender or receiver (true if sender).
	 * @param time is the reception time.
	 */
	public void logMessage(
		SIPMessage message,
		String from,
		String to,
		String status,
		boolean sender,
		long time) {
		checkLogFile();
		CallID cid = (CallID) message.getCallId();
		String callId = null;
		if (cid != null)
			callId = cid.getCallId();
		String firstLine = message.getFirstLine().trim();
		String encoded = (logContent ? message.encode() : message.encodeMessage() ) ;
		String tid = message.getTransactionId();
		TimeStampHeader tshdr = (TimeStampHeader) message.getHeader(TimeStampHeader.NAME);
		long tsval = tshdr == null? 0: tshdr.getTime();
		logMessage(
			encoded,
			from,
			to,
			sender,
			callId,
			firstLine,
			status,
			tid,
			time,
			tsval);
	}

	/**
	 * Log a message into the log directory. Time stamp associated with the
	 * message is the current time.
	 * @param message a SIPMessage to log
	 * @param from from header of the message to log into the log directory
	 * @param to to header of the message to log into the log directory
	 * @param status the status to log.
	 * @param sender is the server the sender or receiver (true if sender).
	 */
	public void logMessage(
		SIPMessage message,
		String from,
		String to,
		String status,
		boolean sender) {
		logMessage(message, from, to, status, sender,
		System.currentTimeMillis()
		);
	}

	

	/**
	 * Log an exception stack trace.
	 * @param ex Exception to log into the log file
	 */

	public void logException(Exception ex) {
		if (traceLevel >= TRACE_EXCEPTION) {
			checkLogFile();
			ex.printStackTrace();
			if (printWriter != null)
				ex.printStackTrace(printWriter);

		}
	}

	/**
	 * Set the name to assign for the log.
	public void setLogName(String name) {
		logRootName = name;
	}
	 */

	/**
	 * print a line to stdout if the traceLevel is TRACE_DEBUG.
	 * @param s String to print out.
	 */
	public void println(String s) {
		if (traceLevel == TRACE_DEBUG)
			System.out.println(s);
	}

	/**
	 * Set the trace level for the stack.
	 *
	 * @param level -- the trace level to set. The following trace levels are
	 * supported:
	 *<ul>
	 *<li>
	 *0 -- no tracing
	 *</li>
	 *
	 *<li>
	 *16 -- trace messages only
	 *</li>
	 *
	 *<li>
	 *32 Full tracing including debug messages.
	 *</li>
	 *
	 *</ul>
	 */
	public void setTraceLevel(int level) {
		traceLevel = level;
	}

	/**
	 * Get the trace level for the stack.
	 *
	 * @return the trace level
	 */
	public int getTraceLevel() {
		return traceLevel;
	}

	/**
	 * Set aux information. Auxiliary information may be associated
	 * with the log file. This is useful for remote logs.
	 *
	 * @param auxInfo -- auxiliary information.
	 */
	public void setAuxInfo(String auxInfo) {
		this.auxInfo = auxInfo;
	}

	/**
	 * Set the descriptive String for the log.
	 *
	 * @param desc is the descriptive string.
	public void setDescription(String desc) {
		description = desc;
	}
	 */
}
