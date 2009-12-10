package test.tck.msgflow.callflows.redirect;

public class TcpRedirectTest extends AbstractRedirectTestCase {
    boolean myFlag;

    public void setUp() {
        super.testedImplFlag = !myFlag;
        myFlag = !super.testedImplFlag;
        super.transport = "tcp";
        super.setUp();
    }
    public void testRedirect() {
        this.shootist.sendInvite();

    }

    public void testRedirect2() {
        this.shootist.sendInvite();
    }
}
