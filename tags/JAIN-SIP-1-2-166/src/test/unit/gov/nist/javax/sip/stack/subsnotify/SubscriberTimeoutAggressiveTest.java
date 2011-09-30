package test.unit.gov.nist.javax.sip.stack.subsnotify;

import junit.framework.TestCase;

public class SubscriberTimeoutAggressiveTest  extends TestCase {
	Subscriber subscriber;
	Notifier   notifier;
	
	
	public void setUp() throws Exception {
		subscriber = Subscriber.createSubcriber();
		notifier = Notifier.createNotifier();
	}
	
	
	public void testSendSubscribe() {
	    notifier.setHandleSubscribe(false);
		subscriber.sendSubscribe();
	
	}
	
	public void tearDown() throws Exception {
		Thread.sleep(50000);
		subscriber.tearDown();
		notifier.tearDown();
	}

}
