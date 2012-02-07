/**
 * 
 */
package test.unit.gov.nist.javax.sip.stack.acktransport;

import java.util.HashSet;

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

    private static Logger logger = Logger.getLogger("test.tck");

    protected static final Appender console = new ConsoleAppender(new SimpleLayout());    
   

    protected HashSet<Shootme> shootme = new HashSet<Shootme>();

  

    private Proxy proxy;

    // private Appender appender;

    public InviteTest() {

        super("");

    }

    public void setUp() {

        try {
            super.setUp();
            

        } catch (Exception ex) {
            fail("unexpected exception ");
        }
    }

    public void tearDown() {
        try {
            
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("unexpected exception", ex);
            fail("unexpected exception ");
        }
    }

    public void testSendInvite() throws Exception {
        try {
           
            Shootme shootmeUa = new Shootme(5080, true, 4000, 4000);
            SipProvider shootmeProvider = shootmeUa.createProvider();
            shootmeProvider.addSipListener(shootmeUa);

        
            this.proxy = new Proxy(5070);
            SipProvider provider = proxy.createSipProvider("tcp");
            provider.addSipListener(proxy);
            provider = proxy.createSipProvider("udp");
            provider.addSipListener(proxy);
            
            
            Shootist shootist = new Shootist(6050, 5070);
            SipProvider shootistProvider = shootist.createSipProvider();
            shootistProvider.addSipListener(shootist);
          //  shootistProvider = shootist.createSipProvider("udp");
          //  shootistProvider.addSipListener(shootist);

            shootist.sendInvite(1);

            Thread.sleep(30000);
            
            shootmeUa.checkState();
            shootist.checkState();
            shootist.stop();
            shootmeUa.stop();
            proxy.stop();
        } finally {
           
        }
    }

}
