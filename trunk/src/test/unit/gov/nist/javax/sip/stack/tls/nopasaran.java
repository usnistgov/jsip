package test.unit.gov.nist.javax.sip.stack.tls;

import java.net.Socket;

/**
 * To test http://java.net/jira/browse/JSIP-415
 * Run Shootme, run this class do a thread dump to make sure there is no thread leaks
 */
public class nopasaran {

	public static void main(String args[]) throws Exception {
		Socket s = new Socket("127.0.0.1", 5071);
		s.getInputStream();
		System.out.println("you shall not pass! :)");
		Thread.sleep(99999999);
	}
}