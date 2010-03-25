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
	
	public void tearDown() throws Exception {
		Thread.sleep(4000);
		subscriber.tearDown();
		notifier.tearDown();
	}

}
