/**
 *
 */
package examples.cancel;

/**
 * @author M. Ranganathan
 *
 */
public class TestDelayedCancel extends AbstractCancelTest {

    public TestDelayedCancel() {
        super();
    }

    public void testCancelDelay() throws Exception {
        Shootist.sendDelayedCancel = true;
        shootist.sendInvite();
        Thread.sleep(2000);
        shootist.checkState();
    }
}
