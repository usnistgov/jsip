/**
 *
 */
package examples.cancel;

/**
 * @author M. Ranganathan
 *
 */
public class TestCancel extends AbstractCancelTest {

    public TestCancel() {
        super();
    }


    public void testCancelNoDelay() throws Exception {
        Shootist.sendDelayedCancel = false;
        shootist.sendInvite();
        Thread.sleep(5000);
        shootist.checkState();

    }
}
