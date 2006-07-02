package test.tck.msgflow.callflows;


import java.util.EventObject;
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
	protected test.tck.msgflow.callflows.ProtocolObjects tiProtocolObjects;
	protected ProtocolObjects riProtocolObjects;
	
	protected String transport;
	
	protected Hashtable providerTable;
	
	// this flag determines whether the tested SIP Stack is shootist or shootme
	protected boolean testedImplFlag;
	
	

	public void setUp() throws Exception {
		if (testedImplFlag) {
			this.tiProtocolObjects = new ProtocolObjects(super.getName(),
					"gov.nist", transport, true);

			if (!getImplementationPath().equals("gov.nist"))
				this.riProtocolObjects = new ProtocolObjects(
						super.getName(), super.getImplementationPath(),
						transport, true);
			else
				this.riProtocolObjects = tiProtocolObjects;

		} else {
			this.tiProtocolObjects = new ProtocolObjects(super.getName(),
					getImplementationPath(), transport, true);
			if (!getImplementationPath().equals("gov.nist"))
				this.riProtocolObjects = new ProtocolObjects(
						super.getName(), super.getImplementationPath(),
						transport, true);
			else
				this.riProtocolObjects = tiProtocolObjects;

		}
	}

	private SipListener getSipListener(EventObject sipEvent) {
		SipProvider source = (SipProvider) sipEvent.getSource();
		SipListener listener = (SipListener) providerTable.get(source);
		if ( listener == null) throw new TckInternalError("Unexpected null listener");
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
		fail("unexpected exception");

	}

	public void processTransactionTerminated(
			TransactionTerminatedEvent transactionTerminatedEvent) {
		getSipListener(transactionTerminatedEvent)
				.processTransactionTerminated(transactionTerminatedEvent);

	}

	public void processDialogTerminated(
			DialogTerminatedEvent dialogTerminatedEvent) {
		getSipListener(dialogTerminatedEvent).processDialogTerminated(
				dialogTerminatedEvent);

	}



	protected ScenarioHarness (String name, boolean autoDialog ) {
		
		super(name,autoDialog);
		this.providerTable = new Hashtable();
		
		
		
	}


}
