package test.unit.gov.nist.javax.sip.stack.dialog.b2bua.reinvite;

import junit.framework.TestCase;

public class BackToBackUserAgentTest extends TestCase {
    
    private Shootist shootist;
    private BackToBackUserAgent b2bua;
    private Shootme shootme;

    @Override 
    public void setUp() throws Exception {
        this.shootist = new Shootist(5060,5070);
        this.b2bua  = new BackToBackUserAgent(5070,5080);
        this.shootme = new Shootme(5090,true,100);
        Thread.sleep(1000);
        
    }
    
    public void testSendInvite() {
        this.shootist.sendInvite();
    }
    
    @Override 
    public void tearDown() throws Exception {
        Thread.sleep(4000);
        this.shootist.checkState();
        this.shootme.checkState();
        this.b2bua.checkState();
    }
    

}
