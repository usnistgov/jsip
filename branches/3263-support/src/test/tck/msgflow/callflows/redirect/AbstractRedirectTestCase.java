package test.tck.msgflow.callflows.redirect;

import javax.sip.SipListener;
import javax.sip.SipProvider;

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

import test.tck.msgflow.callflows.ScenarioHarness;

/**
 * @author M. Ranganathan
 *
 */
public abstract class AbstractRedirectTestCase extends ScenarioHarness implements
        SipListener {


    protected Shootist shootist;

    protected Shootme shootme;

    private static Logger logger = Logger.getLogger("test.tck");

    static {
        if (!logger.isAttached(console)) {

            logger.addAppender(console);

        }
    }

    // private Appender appender;

    public AbstractRedirectTestCase() {

        super("redirect", true);


    }

    public void setUp() {
        try {
            super.setUp();

            logger.info("RedirectTest: setup()");
            shootist = new Shootist(getTiProtocolObjects());
            SipProvider shootistProvider = shootist.createProvider();
            providerTable.put(shootistProvider, shootist);

            shootme = new Shootme(getRiProtocolObjects());
            SipProvider shootmeProvider = shootme.createProvider();
            providerTable.put(shootmeProvider, shootme);
            shootistProvider.addSipListener(this);
            shootmeProvider.addSipListener(this);
            if (getTiProtocolObjects() != getRiProtocolObjects())
                getTiProtocolObjects().start();
            getRiProtocolObjects().start();
        } catch (Exception ex) {
            logger.error("unexpected excecption ", ex);
            fail("unexpected exception");
        }

    }

    public void tearDown() {
        try {
            Thread.sleep(4000);
            this.shootist.checkState();
            this.shootme.checkState();
            super.tearDown();
            Thread.sleep(1000);
            this.providerTable.clear();

            logTestCompleted();
        } catch (Exception ex) {
            logger.error("unexpected exception", ex);
            fail("unexpected exception ");
        }

    }


}
