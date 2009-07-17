package test.tck.msgflow.callflows.refer;

public class TcpReferTest extends AbstractReferTestCase {
    boolean myFlag;

    public void setUp() throws Exception {
        // these flags determine
        // which SIP Stack (RI vs TI) is
        // Shootist and which one is Shootme
        // the following setup code flips the roles before each test is run
        testedImplFlag = !myFlag;
        myFlag = !testedImplFlag;
        super.transport = "tcp";
        super.setUp();
    }
    public void testRefer() {
        this.referrer.sendRefer();
    }

    public void testRefer2() {
        this.referrer.sendRefer();
    }
}
