package test.tck.msgflow.callflows.forkedinvite;

public class ForkedInviteTest extends AbstractForkedInviteTestCase {
    boolean myFlag;

    public void setUp() {
        super.testedImplFlag = !myFlag;
        myFlag = !super.testedImplFlag;
        super.transport = "udp";
        super.setUp();
    }

    public void testForkedInvite() {
        this.shootist.sendInvite();
    }



}
