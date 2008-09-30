/**
 * 
 */
package test.unit.gov.nist.javax.sip.stack.forkedinvite;

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

    private static final int forkCount = 40;
    
   

    protected HashSet<Shootme> shootme = new HashSet<Shootme>();

  

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
            
            for  (int i = 0 ; i <  forkCount ; i ++ ) {
                Shootme shootme = new Shootme(5080 + i,500);
                SipProvider shootmeProvider = shootme.createProvider();
                shootmeProvider.addSipListener(shootme);
                this.shootme.add(shootme);
            }

           

            this.proxy = new Proxy(5070,forkCount);
            SipProvider provider = proxy.createSipProvider();
            provider.addSipListener(proxy);
            logger.debug("setup completed");

        } catch (Exception ex) {
            fail("unexpected exception ");
        }
    }

    public void tearDown() {
        try {
            Thread.sleep(12000);
           
            
            this.shootist.checkState();
            
            for ( Shootme shootme: this.shootme) {
                 shootme.checkState();
                 shootme.checkBye();
            }
           
           
            this.shootist.stop();
            
            for ( Shootme shootme: this.shootme) {
              
                shootme.stop();
           }

        
            this.proxy.stop();
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("unexpected exception", ex);
            fail("unexpected exception ");
        }
    }

    public void testInvite() throws Exception {
        this.shootist.sendInvite(forkCount);

    }

}
