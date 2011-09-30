package test.tck.msgflow.callflows.redirect;

public class UdpRedirectTest extends AbstractRedirectTestCase {
    boolean myFlag;
    public void setUp() {
        testedImplFlag = !myFlag;
        myFlag = !testedImplFlag;
        super.transport = "udp";
        super.setUp();
    }
    public void testRedirect() {
            this.shootist.sendInvite();

    }

    public void testRedirect2() {
        this.shootist.sendInvite();
    }
}
