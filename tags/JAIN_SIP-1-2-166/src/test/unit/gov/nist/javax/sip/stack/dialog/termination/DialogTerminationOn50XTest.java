package test.unit.gov.nist.javax.sip.stack.dialog.termination;

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

public class DialogTerminationOn50XTest extends ScenarioHarness implements SipListener {

    protected Shootist shootist;

    private Shootme shootme;

    private static Logger logger = Logger.getLogger("test.tck");

    static {
        if (!logger.isAttached(console))
            logger.addAppender(console);
    }

    private SipListener getSipListener(EventObject sipEvent) {
        SipProvider source = (SipProvider) sipEvent.getSource();
        SipListener listener = (SipListener) providerTable.get(source);
        assertTrue(listener != null);
        return listener;
    }

    public DialogTerminationOn50XTest() {
        super("DialogTerminationOn500Test", true);

    }

    public void setUp() {
        doSetUp();

    }

    private void doSetUp() {
        try {
            this.transport = "udp";

            super.setUp();
            shootist = new Shootist(getRiProtocolObjects());
            SipProvider shootistProvider = shootist.createSipProvider();
            shootist.init();
            providerTable.put(shootistProvider, shootist);

            shootme = new Shootme(getTiProtocolObjects());
            SipProvider shootmeProvider = shootme.createSipProvider();
            shootme.init();
            providerTable.put(shootmeProvider, shootme);
            shootistProvider.addSipListener(this);
            shootmeProvider.addSipListener(this);

            getRiProtocolObjects().start();
            if (getTiProtocolObjects() != getRiProtocolObjects())
                getTiProtocolObjects().start();
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("unexpected exception ");
        }
    }

    public void testSendInviteShouldNotTerminatedOnINFO() {
        for (int i = 300; i < 601; i += 100 ) {
            try {

                this.shootme.setResponseCodeToINFO(i);
                this.shootist.setResponseCodeToINFO(i);
                this.shootist.sendInviteRequest();
                Thread.currentThread().sleep(25000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if (!this.shootme.checkState() || !this.shootist.checkState()) {
                fail("Test Failed - Didnt receive DTE for response: "+i);
                return;
            }
            if (i != 600) {
//                doTearDown(false);
//                doSetUp();
            }

        }

    }



    public void tearDown() {
        doTearDown(true);

    }

    private void doTearDown(boolean definetly) {
        try {
            Thread.sleep(2000);
            // this.shootist.checkState();
            // this.shootme.checkState();
            getTiProtocolObjects().destroy();
            if (getTiProtocolObjects() != getRiProtocolObjects())
                getRiProtocolObjects().destroy();
            Thread.sleep(1000);
            this.providerTable.clear();
            if (definetly)
                logTestCompleted();
        } catch (Exception ex) {
            ex.printStackTrace();
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

    public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
        getSipListener(transactionTerminatedEvent).processTransactionTerminated(transactionTerminatedEvent);

    }

    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
        getSipListener(dialogTerminatedEvent).processDialogTerminated(dialogTerminatedEvent);

    }

}
