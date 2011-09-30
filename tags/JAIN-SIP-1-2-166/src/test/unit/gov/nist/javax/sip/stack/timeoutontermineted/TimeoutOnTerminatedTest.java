/**
 *
 */
package test.unit.gov.nist.javax.sip.stack.timeoutontermineted;

import javax.sip.SipProvider;

import junit.framework.TestCase;

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

/**
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 *
 */
public class TimeoutOnTerminatedTest extends TestCase {

    protected Shootist shootist;

    protected Shootme shootme;

    private static Logger logger = Logger.getLogger("test.tck");

    protected static final Appender console = new ConsoleAppender(new SimpleLayout());

    // private Appender appender;

    public TimeoutOnTerminatedTest() {

        super("timeoutontermineted");

    }

    @Override
    public void setUp() {

        try {
            super.setUp();
            shootist = new Shootist(5060, 5080);
            SipProvider shootistProvider = shootist.createSipProvider();
            shootistProvider.addSipListener(shootist);

            shootme = new Shootme(5080, 1000);

            SipProvider shootmeProvider = shootme.createProvider();
            shootmeProvider.addSipListener(shootme);

            logger.debug("setup completed");

        } catch (Exception ex) {
            fail("unexpected exception ");
        }
    }

    @Override
    public void tearDown() {
        try {
            Thread.sleep(60000);

            this.shootist.checkState();

            this.shootme.checkState();

            this.shootist.stop();

            this.shootme.stop();

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
