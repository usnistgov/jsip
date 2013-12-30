/*
* Conditions Of Use
*
* This software was developed by employees of the National Institute of
* Standards and Technology (NIST), and others.
* This software is has been contributed to the public domain.
* As a result, a formal license is not needed to use the software.
*
* This software is provided "AS IS."
* NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
* OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
* AND DATA ACCURACY.  NIST does not warrant or make any representations
* regarding the use of the software or the results thereof, including but
* not limited to the correctness, accuracy, reliability or usefulness of
* the software.
*
*
*/
/**
 *
 */
package test.unit.gov.nist.javax.sip.stack.reInvite;

import gov.nist.javax.sip.SipStackImpl;

import java.util.EventObject;

import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;

import org.apache.log4j.Logger;

import test.tck.msgflow.callflows.ScenarioHarness;

/**
 * @author M. Ranganathan
 *
 */
public class ReInviteTest extends ScenarioHarness implements SipListener {


    protected Shootist shootist;

    private Shootme shootme;

    private static Logger logger = Logger.getLogger("test.tck");

    static {
        //if (!logger.isAttached(console))
        //    logger.addAppender(console);
    }

    private SipListener getSipListener(EventObject sipEvent) {
        SipProvider source = (SipProvider) sipEvent.getSource();
        SipListener listener = (SipListener) providerTable.get(source);
        return listener;
    }

    public ReInviteTest() {
        super("reinvitetest", true);
    }

    public void setUp() {

        try {
            this.transport = "udp";

            super.setUp();
            
            shootist = new Shootist(getRiProtocolObjects());
            
            SipProvider shootistProvider = shootist.createSipProvider();
            providerTable.put(shootistProvider, shootist);

            shootme = new Shootme(getTiProtocolObjects());
            SipProvider shootmeProvider = shootme.createSipProvider();
            providerTable.put(shootmeProvider, shootme);
            shootistProvider.addSipListener(this);
            shootmeProvider.addSipListener(this);
            
            ((SipStackImpl)getTiProtocolObjects().sipStack).setIsBackToBackUserAgent(false);
            ((SipStackImpl)getRiProtocolObjects().sipStack).setIsBackToBackUserAgent(false);

            getRiProtocolObjects().start();
            if (getTiProtocolObjects() != getRiProtocolObjects())
                getTiProtocolObjects().start();
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("unexpected exception ");
        }
    }

    public void testSendInvite() {
        this.shootist.sendInvite();
    }
    
	// http://java.net/jira/browse/JSIP-444 Check Honor Target Refresh on Response
    public void testTargetRefresh() {
    	this.shootme.isTargetRefresh = true;
    	this.shootist.isTargetRefresh = true;
        this.shootist.sendInvite();
    }

    public void tearDown() {
        try {
            Thread.sleep(4000);
            this.shootist.checkState();
            this.shootme.checkState();   
            this.providerTable.clear();
            logTestCompleted();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
        	try {
        		super.tearDown();
        		Thread.sleep(1000);
        	} catch (Exception ex) { ex.printStackTrace(); }
        }

    }

    public void processRequest(RequestEvent requestEvent) {
        getSipListener(requestEvent).processRequest(requestEvent);

    }

    public void processResponse(ResponseEvent responseEvent) {
        getSipListener(responseEvent).processResponse(responseEvent);

    }

    public void processTimeout(TimeoutEvent timeoutEvent) {
        getSipListener(timeoutEvent).processTimeout(timeoutEvent);
    }

    public void processIOException(IOExceptionEvent exceptionEvent) {
        fail("unexpected exception");

    }

    public void processTransactionTerminated(
            TransactionTerminatedEvent transactionTerminatedEvent) {
        if ( getSipListener(transactionTerminatedEvent) != null ) {
         	getSipListener(transactionTerminatedEvent).processTransactionTerminated(transactionTerminatedEvent);
        }

    }

    public void processDialogTerminated(
            DialogTerminatedEvent dialogTerminatedEvent) {
    	if (getSipListener(dialogTerminatedEvent) != null ) {
    		getSipListener(dialogTerminatedEvent).processDialogTerminated(
                dialogTerminatedEvent);
    	}

    }

}
