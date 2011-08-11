package test.tck.msgflow.callflows.recroute;

public class UDPRecordRouteTest extends AbstractRecRouteTestCase{
    boolean myFlag;

    public void setUp() {
        super.testedImplFlag = !myFlag;
        myFlag = !super.testedImplFlag;
        super.transport = "udp";
        super.setUp();
    }

    public void testRecordRoute() {
        this.shootist.sendInvite();
    }
}
