/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/

package gov.nist.javax.sip;
import gov.nist.javax.sip.stack.*;
import gov.nist.javax.sip.message.*;
import gov.nist.core.*;
import javax.sip.*;

/**
 * Implements all the support classes that are necessary for the nist-sip
 * stack on which the jain-sip stack has been based.
 * This is a mapping class to map from the NIST-SIP abstractions to
 * the JAIN abstractions. (i.e. It is the glue code that ties
 * the NIST-SIP event model and the JAIN-SIP event model together.
 * When a SIP Request or SIP Response is read from the corresponding
 * messageChannel, the NIST-SIP stack calls the SIPStackMessageFactory 
 * implementation that has been registered with it to process the request.)
 *
 * @version JAIN-SIP-1.1 $Revision: 1.8 $ $Date: 2004-06-15 09:54:39 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class NistSipMessageFactoryImpl implements SIPStackMessageFactory {

	SipStackImpl sipStackImpl;

	/**
	 *Construct a new SIP Server Request.
	 *@param sipRequest is the SIPRequest from which the SIPServerRequest
	 * is to be constructed.
	 *@param messageChannel is the MessageChannel abstraction for this
	 * 	SIPServerRequest.
	 */
	public SIPServerRequestInterface newSIPServerRequest(
		SIPRequest sipRequest,
		MessageChannel messageChannel) {

		if (messageChannel == null || sipRequest == null) {
			throw new IllegalArgumentException("Null Arg!");
		}

		NistSipMessageHandlerImpl retval = new NistSipMessageHandlerImpl();
		if (messageChannel instanceof SIPTransaction) {
			// If the transaction has already been created
			// then set the transaction channel.
			retval.transactionChannel = (SIPTransaction) messageChannel;
		}
		SIPTransactionStack theStack =
			(SIPTransactionStack) messageChannel.getSIPStack();
		retval.sipStackImpl = (SipStackImpl)theStack;
		retval.listeningPoint =
			messageChannel.getMessageProcessor().getListeningPoint();
		if (retval.listeningPoint == null)
			return null;
		if (LogWriter.needsLogging)
			sipStackImpl.getLogWriter().logMessage(
				"Returning request interface for "
					+ sipRequest.getFirstLine()
					+ " "
					+ retval
					+ " messageChannel = "
					+ messageChannel);
		return retval;
	}

	/**
	 * Generate a new server response for the stack.
	 *@param sipResponse is the SIPRequest from which the SIPServerRequest
	 * is to be constructed.
	 *@param messageChannel is the MessageChannel abstraction for this
	 * 	SIPServerResponse
	 */
	public SIPServerResponseInterface newSIPServerResponse(
		SIPResponse sipResponse,
		MessageChannel messageChannel) {
		SIPTransactionStack theStack =
			(SIPTransactionStack) messageChannel.getSIPStack();
		// Tr is null if a transaction is not mapped.
		SIPTransaction tr =
			(SIPTransaction) ((SIPTransactionStack) theStack).findTransaction(
				sipResponse,
				false);
		if (LogWriter.needsLogging)
			sipStackImpl.getLogWriter().logMessage(
				"Found Transaction " + tr + " for " + sipResponse);

		if ( tr != null ) {
		    // Prune unhealthy responses early if handling statefully.
		    // If the state has not yet been assigned then this is a
		    // spurious response. This was moved up from the transaction
		    // layer for efficiency.
		    if (tr.getState() == null)  {
			if (LogWriter.needsLogging)
			   sipStackImpl.logMessage( "Dropping response - null transaction state" );
			return null;
		        // Ignore 1xx 
		    }  else if (TransactionState.COMPLETED == tr.getState()
			&& sipResponse.getStatusCode() / 100 == 1) {
			if (LogWriter.needsLogging) 
			    sipStackImpl.logMessage ( "Dropping response - late arriving "  
				+ sipResponse.getStatusCode());
			return null;
		    } 
		}


		NistSipMessageHandlerImpl retval = new NistSipMessageHandlerImpl();
		retval.sipStackImpl = (SipStackImpl) theStack;
		retval.transactionChannel = tr;

		retval.listeningPoint =
			messageChannel.getMessageProcessor().getListeningPoint();
		return retval;
	}

	public NistSipMessageFactoryImpl(SipStackImpl sipStackImpl) {
		this.sipStackImpl = sipStackImpl;
	}

}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.7  2004/04/06 12:28:22  mranga
 * Reviewed by:   mranga
 * changed locale to Locale.getDefault().getCountry()
 * moved check for valid transaction state up in the stack so unfruitful responses
 * are pruned early.
 *
 * Revision 1.6  2004/01/22 13:26:28  sverker
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
