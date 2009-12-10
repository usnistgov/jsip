package test.unit.gov.nist.javax.sip.stack;

import test.tck.msgflow.callflows.forkedinvite.AbstractForkedInviteTestCase;
import junit.framework.TestCase;

public class NonRfc3261ForkedInviteTest extends AbstractForkedInviteTestCase {

    boolean myFlag;

    public void setUp() {
        super.testedImplFlag = !myFlag;
        myFlag = !super.testedImplFlag;
        super.transport = "udp";
        super.setUp();
    }
    /**
     * Test forking in combination with one non-RFC3261 UAS
     */
    public void testForkedInviteNonRFC3261() {
        shootme.setNonRFC3261( true );
        this.shootist.sendInvite();
    }
}
