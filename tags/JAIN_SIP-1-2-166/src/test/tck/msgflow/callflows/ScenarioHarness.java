package test.tck.msgflow.callflows;

import java.util.EventObject;
import java.util.HashSet;
import java.util.Hashtable;

import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;

import test.tck.TckInternalError;
import test.tck.TestHarness;
import test.tck.TiUnexpectedError;

public abstract class ScenarioHarness extends TestHarness {
    private HashSet<ProtocolObjects> tiProtocolObjects = new HashSet<ProtocolObjects>();

    private HashSet<ProtocolObjects> riProtocolObjects = new HashSet<ProtocolObjects>();

    protected String transport;

    protected Hashtable providerTable;

    // this flag determines whether the tested SIP Stack is shootist or shootme
    protected boolean testedImplFlag;

    public void setUp() throws Exception {
        if (testedImplFlag) {
            this.tiProtocolObjects.add(new ProtocolObjects("ti" + super.getName(), "gov.nist",
                    transport, true,false, false));

            this.addRiProtocolObjects(new ProtocolObjects("ri" + super.getName(), super
                    .getImplementationPath(), transport, true,false, false));
            /*
             * if (!getImplementationPath().equals("gov.nist")) this.riProtocolObjects = new
             * ProtocolObjects( super.getName(), super.getImplementationPath(), transport, true);
             * else this.riProtocolObjects = tiProtocolObjects;
             */

        } else {
            this.tiProtocolObjects.add(new ProtocolObjects("ti" + super.getName(),
                    getImplementationPath(), transport, true,false, false));
            this.addRiProtocolObjects(new ProtocolObjects("ri" + super.getName(), "gov.nist",
                    transport, true,false, false));

            /*
             * if (!getImplementationPath().equals("gov.nist")) this.riProtocolObjects = new
             * ProtocolObjects( super.getName(), super.getImplementationPath(), transport, true);
             * else this.riProtocolObjects = tiProtocolObjects;
             */

        }
    }

    public void setUp(int nri, int nti) throws Exception {
        if (testedImplFlag) {
            for (int i = 0; i < nti; i++) {
                this.tiProtocolObjects.add(new ProtocolObjects("ti" + super.getName() + i,
                        "gov.nist", transport, true,false, false));
            }
            for (int i = 0; i < nri; i++) {

                this.addRiProtocolObjects(new ProtocolObjects("ri" + super.getName() + i, super
                        .getImplementationPath(), transport, true,false, false));
            }
            /*
             * if (!getImplementationPath().equals("gov.nist")) this.riProtocolObjects = new
             * ProtocolObjects( super.getName(), super.getImplementationPath(), transport, true);
             * else this.riProtocolObjects = tiProtocolObjects;
             */

        } else {
            for (int i = 0; i < nti; i++) {
                this.tiProtocolObjects.add(new ProtocolObjects("ti" + super.getName() + i,
                        getImplementationPath(), transport, true,false, false));
            }
            for (int i = 0; i < nri; i++) {
                this.addRiProtocolObjects(new ProtocolObjects("ri" + super.getName() + i,
                        "gov.nist", transport, true,false, false));
            }

            /*
             * if (!getImplementationPath().equals("gov.nist")) this.riProtocolObjects = new
             * ProtocolObjects( super.getName(), super.getImplementationPath(), transport, true);
             * else this.riProtocolObjects = tiProtocolObjects;
             */

        }
    }

    public void setUp(boolean riAutoDialog) throws Exception {
        if (testedImplFlag) {
            this.tiProtocolObjects.add(new ProtocolObjects("ti" + super.getName(), "gov.nist",
                    transport, true, false, false));

            this.addRiProtocolObjects(new ProtocolObjects("ri" + super.getName(), super
                    .getImplementationPath(), transport, riAutoDialog,false, false));
            /*
             * if (!getImplementationPath().equals("gov.nist")) this.riProtocolObjects = new
             * ProtocolObjects( super.getName(), super.getImplementationPath(), transport, true);
             * else this.riProtocolObjects = tiProtocolObjects;
             */

        } else {
            this.tiProtocolObjects.add(new ProtocolObjects("ti" + super.getName(),
                    getImplementationPath(), transport, true,false, false));
            this.addRiProtocolObjects(new ProtocolObjects("ri" + super.getName(), "gov.nist",
                    transport, riAutoDialog,false, false));

            /*
             * if (!getImplementationPath().equals("gov.nist")) this.riProtocolObjects = new
             * ProtocolObjects( super.getName(), super.getImplementationPath(), transport, true);
             * else this.riProtocolObjects = tiProtocolObjects;
             */

        }
    }

    public void setUp(boolean riAutoDialog, int nri, int nti) {
        if (testedImplFlag) {
            for (int i = 0; i < nti; i++) {
                this.tiProtocolObjects.add(new ProtocolObjects("ti" + super.getName() + i,
                        "gov.nist", transport, true,false, false));

            }
            for (int i = 0; i < nri; i++) {
                this.addRiProtocolObjects(new ProtocolObjects("ri" + super.getName(), super
                        .getImplementationPath(), transport, riAutoDialog,false, false));
            }

        } else {
            for (int i = 0; i < nti; i++) {
                this.tiProtocolObjects.add(new ProtocolObjects("ti" + super.getName() + i,
                        getImplementationPath(), transport, true,false, false));
            }
            for (int i = 0; i < nri; i++) {
                this.addRiProtocolObjects(new ProtocolObjects("ri" + super.getName(), "gov.nist",
                        transport, riAutoDialog,false, false));
            }

        }
    }

    private SipListener getSipListener(EventObject sipEvent) {
        SipProvider source = (SipProvider) sipEvent.getSource();
        SipListener listener = (SipListener) providerTable.get(source);
        if (listener == null)
            throw new TckInternalError("Unexpected null listener");
        return listener;
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
    
    	System.out.println("IOException ");

    }

    public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
        getSipListener(transactionTerminatedEvent).processTransactionTerminated(
                transactionTerminatedEvent);

    }

    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
        getSipListener(dialogTerminatedEvent).processDialogTerminated(dialogTerminatedEvent);

    }

    protected ScenarioHarness(String name, boolean autoDialog) {

        super(name, autoDialog);
        this.providerTable = new Hashtable();

    }

    protected ScenarioHarness(String name, boolean autoDialog, int nri, int nti) {
        super(name, autoDialog);
        this.providerTable = new Hashtable();

    }

    /**
     * @param riProtocolObjects the riProtocolObjects to set
     */
    protected void addRiProtocolObjects(ProtocolObjects riProtocolObjects) {
        this.riProtocolObjects.add(riProtocolObjects);
    }

    /**
     * @return the riProtocolObjects
     */
    protected ProtocolObjects getRiProtocolObjects() {
        return riProtocolObjects.iterator().next();
    }

    /**
     * @param tiProtocolObjects the tiProtocolObjects to set
     */
    protected void addTiProtocolObjects(
            test.tck.msgflow.callflows.ProtocolObjects tiProtocolObjects) {
        this.tiProtocolObjects.add(tiProtocolObjects);
    }

    /**
     * @return the tiProtocolObjects
     */
    protected test.tck.msgflow.callflows.ProtocolObjects getTiProtocolObjects() {
        return tiProtocolObjects.iterator().next();
    }

    public ProtocolObjects getTiProtocolObjects(int index) {
        return (ProtocolObjects) tiProtocolObjects.toArray()[index];
    }

    public ProtocolObjects getRiProtocolObjects(int index) {
        return (ProtocolObjects) riProtocolObjects.toArray()[index];
    }

    public void tearDown() throws Exception {
        for (ProtocolObjects protocolObjects : this.tiProtocolObjects) {
            protocolObjects.destroy();
        }
        for (ProtocolObjects protocolObjects : this.riProtocolObjects) {
            protocolObjects.destroy();
        }
    }
    
    public void start() throws Exception {
        for (ProtocolObjects protocolObjects : this.tiProtocolObjects) {
            protocolObjects.start();
        }
        for (ProtocolObjects protocolObjects : this.riProtocolObjects) {
            protocolObjects.start();
        }
    }

}
