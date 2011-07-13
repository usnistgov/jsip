package test.tck.msgflow.callflows.prack;


public class TcpPrackTest extends UdpPrackTest {
    boolean myFlag;

    public void setUp() throws Exception {
        super.testedImplFlag = !myFlag;
        myFlag = !super.testedImplFlag;
        super.transport = "tcp";
        super.setUp();
    }
}
