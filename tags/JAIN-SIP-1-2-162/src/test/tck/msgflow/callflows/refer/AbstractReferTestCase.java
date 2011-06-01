package test.tck.msgflow.callflows.refer;

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
 *
 * Implements common setup and tearDown sequence for Refer tests
 *
 * @author M. Ranganathan
 * @author Ivelin Ivanov
 *
 */
public abstract class AbstractReferTestCase extends ScenarioHarness implements
        SipListener {


    protected Referee referee;

    protected Referrer referrer;

    private static Logger logger = Logger.getLogger("test.tck");

    static {
        if (!logger.isAttached(console)) {

            logger.addAppender(console);

        }
    }

    public AbstractReferTestCase() {
        super("refer", true);
    }

    public void setUp() throws Exception {
        try {
            super.setUp();

            logger.info("ReferTest: setup()");
            referee = new Referee(getTiProtocolObjects());
            SipProvider refereeProvider = referee.createProvider();
            providerTable.put(refereeProvider, referee);

            referrer = new Referrer(getRiProtocolObjects());
            SipProvider referrerProvider = referrer.createProvider();
            providerTable.put(referrerProvider, referrer);

            refereeProvider.addSipListener(this);
            referrerProvider.addSipListener(this);

            if (getTiProtocolObjects() != getRiProtocolObjects())
                getTiProtocolObjects().start();
            getRiProtocolObjects().start();
        } catch (Exception ex) {
            logger.error("unexpected excecption ", ex);
            fail("unexpected exception");
        }
    }

    public void tearDown() throws Exception {
        try {
            Thread.sleep(4000);
            super.tearDown();
            Thread.sleep(1000);
            this.providerTable.clear();

            super.assertTrue(" Should have at least 3 NOTIFY", referrer.count >= 3);  // Should have 3 NOTIFYs

            logTestCompleted();
        } catch (Exception ex) {
            logger.error("unexpected exception", ex);
            fail("unexpected exception ");
        }
        super.tearDown();
    }




}
