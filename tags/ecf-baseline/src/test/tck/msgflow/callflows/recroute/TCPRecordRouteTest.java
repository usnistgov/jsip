package test.tck.msgflow.callflows.recroute;

public class TCPRecordRouteTest extends AbstractRecRouteTestCase {
    boolean myFlag;

    public void setUp() {
        super.testedImplFlag = !myFlag;
        myFlag = !super.testedImplFlag;
        super.transport = "tcp";
        super.setUp();
    }

    public void testRecordRoute() {
        this.shootist.sendInvite();
    }



}
