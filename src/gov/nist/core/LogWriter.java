/***************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).    *
***************************************************************************/

package gov.nist.core;
import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
*  Log System Errors. Also used for debugging log.
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/

public class LogWriter
{
	
/** Dont trace
 */    
	public static int TRACE_NONE = 0;
/** Trace initialization code
 */        
/** Trace message processing
 */        
	public static int TRACE_MESSAGES = 16;
/** Trace exception processing
 */        
	public static int TRACE_EXCEPTION = 17;
/** Debug trace level (all tracing enabled).
 */        
	public static int TRACE_DEBUG = 32;
/** Name of the log file in which the trace is written out
 * (default is /tmp/sipserverlog.txt)
 */        
	private  String   logFileName="debuglog.txt";
/** Print writer that is used to write out the log file.
 */        
	public  PrintWriter printWriter;
/** print stream for writing out trace
 */        
	public  PrintStream traceWriter = System.out;

/** Flag to indicate that logging is enabled. This needs to be
* static and public in order to globally turn logging on or off.
* This is static for efficiency reasons (the java compiler will not
* generate the logging code if this is set to false).
*/
	public static boolean needsLogging = false;

	public int lineCount;


	/**
	*  Debugging trace stream.
	*/
	private    static  PrintStream trace = System.out;
	/** trace level
	 */        
	 // protected     static int traceLevel = TRACE_DEBUG;
	protected     static int traceLevel = TRACE_NONE;


	/** log a stack trace..
	*/
	public  void logStackTrace() {
		if (needsLogging) {
		   checkLogFile();
		   if (printWriter != null)  {
		      println("------------ Traceback ------");
		      logException(new Exception());
		      println("----------- End Traceback ------");
		   }
		}
	}

	
	public void logException(Exception ex) {
	    if (needsLogging)  {
	      StringWriter sw = new StringWriter();
	      PrintWriter pw = new PrintWriter( sw);
	      checkLogFile();
	      if (printWriter != null) ex.printStackTrace(pw);
	      println(sw.toString());
	    }
	}
		

	/** Log an excption. 
	* 1.4x Code contributed by Brad Templeton
	*
	*@param sframe - frame to log grace.
	*/
	public void logTrace(Throwable sframe) {
	    if (needsLogging)  {
	     checkLogFile();
             logException(new Exception(sframe.getMessage()) );
	   }
	}


	/** Set the log file name 
	*@param name is the name of the log file to set. 
	*/
	public void setLogFileName(String name) {
		logFileName = name;
	}

	public synchronized void 
	logMessage(String message, String logFileName) {
		try {
			File  logFile = new File(logFileName);
			if (! logFile.exists()) {
				logFile.createNewFile();
				printWriter = null;
			}
			// Append buffer to the end of the file.
   			FileWriter fw = new FileWriter(logFileName,true);
			PrintWriter printWriter = 
					new PrintWriter(fw,true);
			printWriter.println
			(" ---------------------------------------------- ");
			printWriter.println(message);
			printWriter.close();
			fw.close();
				
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	private void checkLogFile() {
		
		if (printWriter != null) return;
		if (logFileName == null) return;
		try {
			File  logFile = new File(logFileName);
			if (! logFile.exists()) {
				logFile.createNewFile();
				printWriter = null;
			}
			// Append buffer to the end of the file.
			if (printWriter == null) {
	   			FileWriter fw = new FileWriter(logFileName,true);
				printWriter = new PrintWriter(fw,true);
				printWriter.println("<debug>");
				printWriter.println("<![CDATA[ ");
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
 
	private void println(String message) {
	   for (int i = 0; i < message.length(); i++) {
		if (message.charAt(i) == '\n') 
			lineCount ++;
	    }
	    checkLogFile();
	    // String tname = Thread.currentThread().getName();
	    if (printWriter != null) {
		   printWriter.println( message);	
	    }
	    lineCount++;
	}

	/** Log a message into the log file.
         * @param message message to log into the log file.
         */
	public  void logMessage(String message) {
                if (! needsLogging) return;
		checkLogFile();
	        println(message);	
	}
	
    
	
        /** Set the trace level for the stack.
         */
        public void setTraceLevel(int level) {
            traceLevel = level;
        }
        
        /** Get the trace level for the stack.
         */
        public int getTraceLevel() { return traceLevel; }
	

}
