/******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD)       *
 ******************************************************************************/
package gov.nist.javax.sip.parser;

import gov.nist.core.*;
import gov.nist.javax.sip.message.*;
import gov.nist.javax.sip.header.*;
import java.text.ParseException;
import java.io.*;
//ifdef SIMULATION
/*
import sim.java.net.*;
//endif
*/

/**
 * This implements a pipelined message parser suitable for use
 * with a stream - oriented input such as TCP. The client uses
 * this class by instatiating with an input stream from which
 * input is read and fed to a message parser.
 * It keeps reading from the input stream and process messages in a
 * never ending interpreter loop. The message listener interface gets called
 * for processing messages or for processing errors. The payload specified
 * by the content-length header is read directly from the input stream.
 * This can be accessed from the SIPMessage using the getContent and
 * getContentBytes methods provided by the SIPMessage class. 
 *
 * @version JAIN-SIP-1.1 $Revision: 1.7 $ $Date: 2004-02-25 20:52:46 $
 *
 * @author <A href=mailto:mranga@nist.gov > M. Ranganathan  </A>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 * Lamine Brahimi and Yann Duponchel (IBM Zurich) noticed that the parser was
 * blocking so I threw out some cool pipelining which ran fast but only worked
 * when the phase of the full moon matched its mood. Now things are serialized
 * and life goes slower but more reliably.
 *
 * @see  SIPMessageListener
 */
public final class PipelinedMsgParser implements Runnable {

	/**
	 * A filter to read the input
	 */
	class MyFilterInputStream extends FilterInputStream {
		public MyFilterInputStream(InputStream in) {
			super(in);
		}
	}

	/**
	 * The message listener that is registered with this parser.
	 * (The message listener has methods that can process correct
	 * and erroneous messages.)
	 */
	protected SIPMessageListener sipMessageListener;
//ifdef SIMULATION
/*
	private SimThread mythread; // Preprocessor thread
//else
*/
	private Thread mythread; // Preprocessor thread
//endif
//
	private byte[] messageBody;
	private boolean errorFlag;
	private InputStream rawInputStream;

	/**
	 * default constructor.
	 */
	protected PipelinedMsgParser() {
		super();

	}

	private static int uid = 0;
	private static synchronized int getNewUid() {
		return uid++;
	}

	/**
	 * Constructor when we are given a message listener and an input stream
	 * (could be a TCP connection or a file)
	 * @param sipMessageListener Message listener which has 
	 * methods that  get called
	 * back from the parser when a parse is complete
	 * @param in Input stream from which to read the input.
	 * @param debug Enable/disable tracing or lexical analyser switch.
	 */
	public PipelinedMsgParser(
		SIPMessageListener sipMessageListener,
		InputStream in,
		boolean debug) {
		this();
		this.sipMessageListener = sipMessageListener;
		rawInputStream = in;
//ifndef SIMULATION
//
		mythread = new Thread(this);
		mythread.setName("PipelineThread-" + getNewUid());
//else
/*
		mythread = new SimThread(this);
//endif
*/

	}

	/**
	 * This is the constructor for the pipelined parser.
	 * @param mhandler a SIPMessageListener implementation that
	 *	provides the message handlers to
	 * 	handle correctly and incorrectly parsed messages.
	 * @param in An input stream to read messages from.
	 */

	public PipelinedMsgParser(SIPMessageListener mhandler, InputStream in) {
		this(mhandler, in, false);
	}

	/**
	 * This is the constructor for the pipelined parser.
	 * @param in - An input stream to read messages from.
	 */

	public PipelinedMsgParser(InputStream in) {
		this(null, in, false);
	}

	/**
	 * Start reading and processing input.
	 */
	public void processInput() {
		mythread.start();
	}

	/**
	 * Create a new pipelined parser from an existing one.
	 * @return A new pipelined parser that reads from the same input
	 * stream.
	 */
	protected Object clone() {
		PipelinedMsgParser p = new PipelinedMsgParser();

		p.rawInputStream = this.rawInputStream;
		p.sipMessageListener = this.sipMessageListener;
//ifdef SIMULATION
/*
		SimThread mythread = new SimThread(p);
//else
*/
		Thread mythread = new Thread(p);
//endif
//
		mythread.setName("PipelineThread");
		return p;
	}

	/**
	 * Add a class that implements a SIPMessageListener interface whose
	 * methods get called * on successful parse and error conditons.
	 * @param mlistener a SIPMessageListener
	 * implementation that can react to correct and incorrect
	 * pars.
	 */

	public void setMessageListener(SIPMessageListener mlistener) {
		sipMessageListener = mlistener;
	}

	/** 
	 * read a line of input (I cannot use buffered reader because we
	 * may need to switch encodings mid-stream!
	 */
	private String readLine(FilterInputStream inputStream) throws IOException {
		StringBuffer retval = new StringBuffer("");
		while (true) {
			try {
				char ch;
				int i = inputStream.read();
				if (i == -1) {
					throw new IOException("End of stream");
				} else
					ch = (char) i;
				if (ch != '\r')
					retval.append(ch);
				if (ch == '\n') {
					break;
				}
			} catch (IOException ex) {
				throw ex;
			}
		}
		return retval.toString();
	}

	/**
	 * Read to the next break (CRLFCRLF sequence)
	 */
	private String readToBreak(FilterInputStream inputStream)
		throws IOException {
		StringBuffer retval = new StringBuffer("");
		boolean flag = false;
		while (true) {
			try {
				char ch;
				int i = inputStream.read();
				if (i == -1)
					break;
				else
					ch = (char) i;
				if (ch != '\r')
					retval.append(ch);
				if (ch == '\n') {
					if (flag)
						break;
					else
						flag = true;
				}
			} catch (IOException ex) {
				throw ex;
			}

		}
		return retval.toString();
	}

	/**
	 * This is input reading thread for the pipelined parser.
	 * You feed it input through the input stream (see the constructor)
	 * and it calls back an event listener interface for message 
	 * processing or error.
	 * It cleans up the input - dealing with things like line continuation 
	 */
	public void run() {

		MyFilterInputStream inputStream = null;
		inputStream = new MyFilterInputStream(this.rawInputStream);
		// I cannot use buffered reader here because we may need to switch
		// encodings to read the message body.
		try {
			while (true) {
				StringBuffer inputBuffer = new StringBuffer();
				Debug.println("Starting parse!");
				String line1;
				String line2 = null;

				// ignore blank lines.
				while (true) {
					try {
						line1 = readLine(inputStream);
						if (line1.equals("\n")) {
							Debug.println("Discarding " + line1);
							continue;
						} else
							break;
					} catch (IOException ex) {
						Debug.printStackTrace(ex);
						return;

					}
				}
				Debug.println("line1  = " + line1);
				inputBuffer.append(line1);

				while (true) {
					try {
						line2 = readLine(inputStream);
						inputBuffer.append(line2);
						if (line2.trim().equals(""))
							break;
					} catch (IOException ex) {
						Debug.printStackTrace(ex);
						return;

					}
				}
				inputBuffer.append(line2);
				StringMsgParser smp = new StringMsgParser(sipMessageListener);
				smp.readBody = false;
				SIPMessage sipMessage = null;
				try {
					sipMessage = smp.parseSIPMessage(inputBuffer.toString());
					if (sipMessage == null)
						continue;
				} catch (ParseException ex) {
					// Just ignore the parse exception.
					continue;
				}
				Debug.println("Completed parsing message");
				ContentLength cl =
					(ContentLength) sipMessage.getContentLength();
				int contentLength = 0;
				if (cl != null) {
					contentLength = cl.getContentLength();
				} else {
					contentLength = 0;
				}

				if (contentLength == 0) {
					Debug.println("content length " + contentLength);
					sipMessage.removeContent();
				} else { // deal with the message body.
					contentLength = cl.getContentLength();
					Debug.println("content length " + contentLength);
					
					byte[] message_body = new byte[contentLength];
					int nread = 0;
					while (nread < contentLength) {
						try {
							int readlength =
								inputStream.read(
									message_body,
									nread,
									contentLength - nread);
							if (readlength >= 0) {
								nread += readlength;
								Debug.println("read " + nread);
							} else {
								break;
							}
						} catch (IOException ex) {
							ex.printStackTrace();
							break;
						}
					}
					sipMessage.setMessageContent(message_body);
				}
				if (sipMessageListener != null) {
					sipMessageListener.processMessage(sipMessage);
				}
			}
		} finally {
			try {
				inputStream.close();
			} catch (IOException ioe) {
			}
		}
	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.6  2004/01/22 18:39:41  mranga
 * Reviewed by:   M. Ranganathan
 * Moved the ifdef SIMULATION and associated tags to the first column so Prep preprocessor can deal with them.
 *
 * Revision 1.5  2004/01/22 14:23:45  mranga
 * Reviewed by:   mranga
 * Fixed some minor formatting issues.
 *
 * Revision 1.4  2004/01/22 13:26:31  sverker
 * Issue number:
 * Obtained from:
 * Submitted by:  sverker
 * Reviewed by:   mranga
 *
 * Major reformat of code to conform with style guide. Resolved compiler and javadoc warnings. Added CVS tags.
 *
 * CVS: ----------------------------------------------------------------------
 * CVS: Issue number:
 * CVS:   If this change addresses one or more issues,
 * CVS:   then enter the issue number(s) here.
 * CVS: Obtained from:
 * CVS:   If this change has been taken from another system,
 * CVS:   then name the system in this line, otherwise delete it.
 * CVS: Submitted by:
 * CVS:   If this code has been contributed to the project by someone else; i.e.,
 * CVS:   they sent us a patch or a set of diffs, then include their name/email
 * CVS:   address here. If this is your work then delete this line.
 * CVS: Reviewed by:
 * CVS:   If we are doing pre-commit code reviews and someone else has
 * CVS:   reviewed your changes, include their name(s) here.
 * CVS:   If you have not had it reviewed then delete this line.
 *
 */
