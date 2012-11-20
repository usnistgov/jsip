package test.unit.gov.nist.javax.sip.stack.tls;

import junit.framework.TestCase;

/**
 * Non regression test for http://java.net/jira/browse/JSIP-417
 *
 */
public class TlsTestDomainDash extends TestCase {
	
	private Shootist shootist;
	private Shootme shootme;


	public void setUp() {
		 // setup TLS properties
	       System.setProperty( "javax.net.ssl.keyStore",  TlsTestDomainDash.class.getResource("fookeys").getPath() );
	       System.setProperty( "javax.net.ssl.trustStore", TlsTestDomainDash.class.getResource("fookeys").getPath() );
	       System.setProperty( "javax.net.ssl.keyStorePassword", "passphrase" );
	       System.setProperty( "javax.net.ssl.keyStoreType", "jks" );
	}
	
	public void testTls() {
		
        this.shootist = new Shootist();
        this.shootme = new Shootme();
        this.shootme.init();
		this.shootist.init("foo-bar.com");
	}
	
	
	public void tearDown() {
		try {
            Thread.sleep(2000);
            this.shootme.stop();
            this.shootist.stop();
           

            System.clearProperty( "javax.net.ssl.keyStore" );
            System.clearProperty( "javax.net.ssl.trustStore" );
            System.clearProperty( "javax.net.ssl.keyStorePassword" );
            System.clearProperty( "javax.net.ssl.keyStoreType" );

           
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
