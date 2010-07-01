package test.tck.msgflow.callflows.refer;

public class UdpReferTest extends AbstractReferTestCase {
    boolean myFlag;
    public void setUp() throws Exception {
        // switch tested stack between the two tests
        testedImplFlag = !myFlag;
        myFlag = !testedImplFlag;
        super.transport = "udp";
        super.setUp();
    }
    public void testRefer() {
        super.transport = "udp";
        this.referrer.sendRefer();
    }

    public void testRefer2() {
        this.referrer.sendRefer();
    }
}
