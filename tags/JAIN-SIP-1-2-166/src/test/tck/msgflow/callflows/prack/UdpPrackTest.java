package test.tck.msgflow.callflows.prack;

import test.tck.msgflow.callflows.prack.AbstractPrackTestCase;

public class UdpPrackTest extends AbstractPrackTestCase {
    boolean myFlag;
    public void setUp() throws Exception {
        testedImplFlag = !myFlag;
        myFlag = !testedImplFlag;
        super.transport = "udp";
        super.setUp();
    }

    public void testPrack() {
        this.shootist.sendInvite();

    }

    public void testPrack2() {
        this.shootist.sendInvite();
    }
}
