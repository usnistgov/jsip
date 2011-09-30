/*
* Conditions Of Use
*
* This software was developed by employees of the National Institute of
* Standards and Technology (NIST), and others.
* This software is has been contributed to the public domain.
* As a result, a formal license is not needed to use the software.
*
* This software is provided "AS IS."
* NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
* OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
* AND DATA ACCURACY.  NIST does not warrant or make any representations
* regarding the use of the software or the results thereof, including but
* not limited to the correctness, accuracy, reliability or usefulness of
* the software.
*
*
*/
package test.tck.msgflow.callflows.subsnotify;

import junit.textui.TestRunner;

public class UdpSubsnotifyTest extends AbstractSubsnotifyTestCase {
    boolean myFlag;
    public void setUp() throws Exception {
        testedImplFlag = !myFlag;
        myFlag = !testedImplFlag;
        super.transport = "udp";
        super.setUp();
    }


    /**
     * tests notifier and subscriber in TI (test impl) provider
     *
     */
    public void testSubsnotify() {
        this.subscriber.sendSubscribe(5065);
    }
    /**
     * tests provider in TI (test impl) provider. Subscriber and Notifier
     * are the RI.
     *
     */
    public void testSubsnotify2() {
        this.subscriber.sendSubscribe(5065);
    }
    public static void main(String[] args) {
        String[] nargs = {UdpSubsnotifyTest.class.getName()};
        TestRunner.main(nargs);
    }
}
