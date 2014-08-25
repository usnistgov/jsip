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

    private static int forkCount = 2;
    
    public static final String PREFERRED_SERVICE_VALUE="urn:urn-7:3gpp-service.ims.icsi.mmtel.gsma.ipcall"; 
   

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
            
            Thread.sleep(35000);
            this.shootist.checkState();
            int ackCount = 0;
            for ( Shootme shootme: this.shootme) {
                 shootme.checkState();
                 if ( shootme.isAckSeen()) {
                     ackCount ++;
                 }
            }
            assertEquals("ACK count must be exactly 2", 2,ackCount);
        } finally {
            this.shootist.stop();
            for ( Shootme shootme: this.shootme) {
                shootme.stop();
            }
            this.proxy.stop();
        }
    }

    public void testInviteAutomaticDialogNonEnabled() throws Exception {
        try {
            shootist = new Shootist(6050, 5070, "off", true);        
            SipProvider shootistProvider = shootist.createSipProvider();
            shootistProvider.addSipListener(shootist);
            boolean sendRinging = true;
            for  (int i = 0 ; i <  forkCount ; i ++ ) {
                Shootme shootme = new Shootme(5080 + i,sendRinging, 4000 + (100 *i), 4000 + (100 *i));
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
            Thread.sleep(35000);
            this.shootist.checkState();
            int ackCount = 0;
            for ( Shootme shootme: this.shootme) {
                 shootme.checkState();
                 if ( shootme.isAckSeen()) {
                     ackCount ++;
                 }
            }
            assertEquals("ACK count must be exactly 2", 2,ackCount);
        } finally {
            this.shootist.stop();
            for ( Shootme shootme: this.shootme) {
                shootme.stop();
            }
            this.proxy.stop();
        }
    }
    
    public void testInviteAutomaticDialogNonEnabledForkSecond() throws Exception {
        try {
            shootist = new Shootist(6050, 5070, "off", false);        
            SipProvider shootistProvider = shootist.createSipProvider();
            shootistProvider.addSipListener(shootist);
            boolean sendRinging = true;
            for  (int i = 0 ; i <  forkCount ; i ++ ) {
                Shootme shootme = new Shootme(5080 + i,sendRinging, 4000 - (500 *i), 4000 - (500 *i));
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
            Thread.sleep(35000);
            this.shootist.checkState();
            int ackCount = 0;
            for ( Shootme shootme: this.shootme) {
                 shootme.checkState();
                 if ( shootme.isAckSeen()) {
                     ackCount ++;
                 }
            }
            assertEquals("ACK count must be exactly 2", 2,ackCount);
        } finally {
            this.shootist.stop();
            for ( Shootme shootme: this.shootme) {
                shootme.stop();
            }
            this.proxy.stop();
        }
    }
    
    public void testInviteAutomaticDialogNonEnabledOKFromSecondForkFirst() throws Exception {
        try {
            shootist = new Shootist(6050, 5070, "off", true);        
            SipProvider shootistProvider = shootist.createSipProvider();
            shootistProvider.addSipListener(shootist);
            boolean sendRinging = true;
            for  (int i = 0 ; i <  forkCount ; i ++ ) {
                Shootme shootme = new Shootme(5080 + i,sendRinging, 4000 + (100 *i), 4000 - (100 *i));
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
            Thread.sleep(35000);
            this.shootist.checkState();
            int ackCount = 0;
            for ( Shootme shootme: this.shootme) {
                 shootme.checkState();
                 if ( shootme.isAckSeen()) {
                     ackCount ++;
                 }
            }
            assertEquals("ACK count must be exactly 2", 2,ackCount);
        } finally {
            this.shootist.stop();
            for ( Shootme shootme: this.shootme) {
                shootme.stop();
            }
            this.proxy.stop();
        }
    }
    
    /**
     * Checking if when the flag is not enabled and a 200 ok response comes before
     * the app code has called createNewDialog doesn't create a dialog 
     */
    public void testAutomaticDialogNonEnabledRaceCondition() throws Exception {
        try {
            shootist = new Shootist(6050, 5070, "off", false); 
            shootist.setCreateDialogAfterRequest(true);
            SipProvider shootistProvider = shootist.createSipProvider();
            shootistProvider.addSipListener(shootist);
            boolean sendRinging = true;
            forkCount = 1;
            for  (int i = 0 ; i <  forkCount ; i ++ ) {
                Shootme shootme = new Shootme(5080 + i,sendRinging, 4000 + (500 *i), 4000 + (500 *i));
                sendRinging = true;
                SipProvider shootmeProvider = shootme.createProvider();
                shootmeProvider.addSipListener(shootme);
                this.shootme.add(shootme);
            }
            this.proxy = new Proxy(5070,forkCount);
            SipProvider provider = proxy.createSipProvider();
            provider.addSipListener(proxy);
            logger.debug("setup completed");
            
            this.shootist.sendInvite(0);
            Thread.sleep(35000);
            this.shootist.checkState();
            int ackCount = 0;
            for ( Shootme shootme: this.shootme) {
                 if ( shootme.isAckSeen()) {
                     ackCount ++;
                 }
            }
            assertEquals("ACK count must be exactly 0", 0,ackCount);
        } finally {
            this.shootist.stop();
            for ( Shootme shootme: this.shootme) {
                shootme.stop();
            }
            this.proxy.stop();
        }
    }

    
}
