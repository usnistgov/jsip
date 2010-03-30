package test.tck.msgflow.callflows.recroute;

import java.util.Hashtable;

import javax.sip.SipListener;
import javax.sip.SipProvider;

import org.apache.log4j.Logger;

import test.tck.msgflow.callflows.ScenarioHarness;

/**
 * @author M. Ranganathan
 * @author Jeroen van Bemmel
 *
 */
public class AbstractRecRouteTestCase extends ScenarioHarness implements
        SipListener {


    protected Shootist shootist;

    private static Logger logger = Logger.getLogger("test.tck");


    protected Shootme shootme;

    private Proxy proxy;

    static {
        if ( !logger.isAttached(console))
            logger.addAppender(console);
    }

    // private Appender appender;

    public AbstractRecRouteTestCase() {

        super("TCPRecRouteTest", true);

        try {
            providerTable = new Hashtable();

        } catch (Exception ex) {
            logger.error("unexpected exception", ex);
            fail("unexpected exception ");
        }
    }

    public void setUp() {

        try {
            super.setUp(false);
            shootist = new Shootist(5060, 5070, getTiProtocolObjects());
            SipProvider shootistProvider = shootist.createSipProvider();
            providerTable.put(shootistProvider, shootist);
            shootistProvider.addSipListener(this);

            this.shootme = new Shootme(5080, getTiProtocolObjects());
            SipProvider shootmeProvider = shootme.createProvider();
            providerTable.put(shootmeProvider, shootme);
            shootmeProvider.addSipListener(this);

            this.proxy = new Proxy(5070, getRiProtocolObjects());
            SipProvider provider = proxy.createSipProvider();
            //provider.setAutomaticDialogSupportEnabled(false);
            providerTable.put(provider, proxy);
            provider.addSipListener(this);

            getTiProtocolObjects().start();
            if (getTiProtocolObjects() != getRiProtocolObjects())
                getRiProtocolObjects().start();
        } catch (Exception ex) {
            logger.error("Unexpected exception",ex);
            fail("unexpected exception ");
        }
    }


    public void tearDown() {
        try {
            Thread.sleep(5000);
            this.shootist.checkState();
            this.shootme.checkState();
            this.proxy.checkState();
            super.tearDown();
            Thread.sleep(4000);
            this.providerTable.clear();

            super.logTestCompleted();
        } catch (Exception ex) {
            logger.error("unexpected exception", ex);
            fail("unexpected exception ");
        }
    }



}
