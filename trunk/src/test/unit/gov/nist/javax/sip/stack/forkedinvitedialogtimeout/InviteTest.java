/**
 * 
 */
package test.unit.gov.nist.javax.sip.stack.forkedinvitedialogtimeout;

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

    protected Shootist shootist;

    private static Logger logger = Logger.getLogger("test.tck");

    protected static final Appender console = new ConsoleAppender(new SimpleLayout());

    private static int forkCount = 2;
    
   

    protected HashSet<Shootme> shootme = new HashSet<Shootme>();

  

    private Proxy proxy;

    // private Appender appender;

    public InviteTest() {

        super("forkedInviteTest");

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

    public void testInvite() throws Exception {
        try {
            shootist = new Shootist(6050, 5070, "on", true);
            SipProvider shootistProvider = shootist.createSipProvider();
            shootistProvider.addSipListener(shootist);
            boolean sendRinging = true;
            for  (int i = 0 ; i <  forkCount ; i ++ ) {
                
                Shootme shootme = new Shootme(5080 + i,sendRinging,4000 + (500 *i), 4000 + (500 *i));
                sendRinging = true;
                SipProvider shootmeProvider = shootme.createProvider();
                shootmeProvider.addSipListener(shootme);
                this.shootme.add(shootme);
            }
    
           
    
            this.proxy = new Proxy(5070,forkCount);
            SipProvider provider = proxy.createSipProvider();
            provider.addSipListener(proxy);
            logger.debug("setup completed");
            
            this.shootist.sendInvite(forkCount);
            
            Thread.sleep(69000);
            this.shootist.checkState();
            int ackCount = 0;
            for ( Shootme shootme: this.shootme) {
                 shootme.checkState();
                 if ( shootme.isAckSeen()) {
                     ackCount ++;
                 }
            }
            assertEquals("ACK count must be exactly 1", 1,ackCount);
        } finally {
            this.shootist.stop();
            for ( Shootme shootme: this.shootme) {
                shootme.stop();
            }
            this.proxy.stop();
        }
    }

 
    
}
