package test.unit.gov.nist.javax.sip.stack.subsnotify;

import junit.framework.TestCase;

public class NotifyBefore202Test  extends TestCase {
	Subscriber subscriber;
	Notifier   notifier;
	
	
	public void setUp() throws Exception {
		subscriber = Subscriber.createSubcriber();
		notifier = Notifier.createNotifier();
	}
	
	
	public void testSendSubscribe() {
		subscriber.sendSubscribe();
	}
	
	/*
	 * Non Regression test for issue http://java.net/jira/browse/JSIP-374
	 */
	public void testInDialogSubscribe() throws InterruptedException {
		subscriber.setInDialogSubcribe(true);
		subscriber.sendSubscribe();
		Thread.sleep(15000);
		assertTrue(subscriber.checkState());
	}
	
	public void tearDown() throws Exception {		
		subscriber.tearDown();
		notifier.tearDown();
	}

}
