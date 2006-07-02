/**
 * 
 */
package test.tck.msgflow.callflows.forkedinvite;

import gov.nist.javax.sip.SipProviderImpl;

import java.util.EventObject;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.helpers.NullEnumeration;

import test.tck.msgflow.callflows.ProtocolObjects;
import test.tck.msgflow.callflows.ScenarioHarness;

import junit.framework.TestCase;

/**
 * @author M. Ranganathan
 * 
 */
public class AbstractForkedInviteTestCase extends ScenarioHarness implements
		SipListener {

	
	protected Shootist shootist;

	private static Logger logger = Logger.getLogger("test.tck");

	
	private Shootme shootme;

	private Shootme shootme2;

    
	static {
		if ( !logger.isAttached(console))
			logger.addAppender(console);
	}

	// private Appender appender;

	public AbstractForkedInviteTestCase() {

		super("forkedInviteTest", true);

		try {
			providerTable = new Hashtable();

		} catch (Exception ex) {
			logger.error("unexpected exception", ex);
			fail("unexpected exception ");
		}
	}

	public void setUp() {

		try {
			super.setUp();
			shootist = new Shootist(5060, 5070, tiProtocolObjects);
			SipProvider shootistProvider = shootist.createSipProvider();
			providerTable.put(shootistProvider, shootist);

			this.shootme = new Shootme(5080, tiProtocolObjects);
			SipProvider shootmeProvider = shootme.createProvider();
			providerTable.put(shootmeProvider, shootme);
			shootistProvider.addSipListener(this);
			shootmeProvider.addSipListener(this);

			this.shootme2 = new Shootme(5090, tiProtocolObjects);
			shootmeProvider = shootme2.createProvider();
			providerTable.put(shootmeProvider, shootme2);
			shootistProvider.addSipListener(this);
			shootmeProvider.addSipListener(this);

			Proxy proxy = new Proxy(5070, riProtocolObjects);
			SipProvider provider = proxy.createSipProvider();
			provider.setAutomaticDialogSupportEnabled(false);
			providerTable.put(provider, proxy);
			provider.addSipListener(this);

			tiProtocolObjects.start();
			if (tiProtocolObjects != riProtocolObjects)
				riProtocolObjects.start();
		} catch (Exception ex) {
			fail("unexpected exception ");
		}
	}

	
	public void tearDown() {
		try {
			Thread.sleep(2000);
			this.shootist.checkState();
			this.shootme.checkState();
			this.shootme2.checkState();
			tiProtocolObjects.destroy();
			if (riProtocolObjects != tiProtocolObjects)
				riProtocolObjects.destroy();
			Thread.sleep(2000);
			this.providerTable.clear();
			
			super.logTestCompleted();
		} catch (Exception ex) {
			logger.error("unexpected exception", ex);
			fail("unexpected exception ");
		}
	}

	

}
