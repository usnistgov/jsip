/**
 * 
 */
package test.unit.gov.nist.javax.sip.stack.forkedinvite;

import javax.sip.SipProvider;

import junit.framework.TestCase;

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.SimpleLayout;

/**
 * @author M. Ranganathan
 * 
 */
public class InviteTest extends TestCase {

    protected Shootist shootist;

    private static Logger logger = Logger.getLogger("test.tck");

    protected static final Appender console = new ConsoleAppender(new SimpleLayout());

    static {

        if (!logger.isAttached(console))
            logger.addAppender(console);
        new PropertyConfigurator().configure("log4j.properties");
    }

    protected Shootme shootme;

    private Shootme shootme2;

    private Proxy proxy;

    // private Appender appender;

    public InviteTest() {

        super("forkedInviteTest");

    }

    public void setUp() {

        try {
            super.setUp();
            shootist = new Shootist(5060, 5070);
            SipProvider shootistProvider = shootist.createSipProvider();
            shootistProvider.addSipListener(shootist);
            
            this.shootme = new Shootme(5080,500);
            SipProvider shootmeProvider1 = shootme.createProvider();
            shootmeProvider1.addSipListener(shootme);

            this.shootme2 = new Shootme(5090,550);
            SipProvider shootmeProvider2 = shootme2.createProvider();

            shootmeProvider2.addSipListener(shootme2);

            this.proxy = new Proxy(5070);
            SipProvider provider = proxy.createSipProvider();
            provider.addSipListener(proxy);
            logger.debug("setup completed");

        } catch (Exception ex) {
            fail("unexpected exception ");
        }
    }

    public void tearDown() {
        try {
            Thread.sleep(4000);
            this.shootist.checkState();
            this.shootme.checkState();
            this.shootme2.checkState();
            this.shootme2.checkBye();
            this.shootme.checkNoBye();
            this.shootist.stop();
            this.shootme.stop();
            this.shootme2.stop();
            this.proxy.stop();
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("unexpected exception", ex);
            fail("unexpected exception ");
        }
    }

    public void testInvite() throws Exception {
        this.shootist.sendInvite();

    }

}
