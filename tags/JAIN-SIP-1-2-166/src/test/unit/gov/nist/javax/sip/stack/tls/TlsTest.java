package test.unit.gov.nist.javax.sip.stack.tls;

import junit.framework.TestCase;

public class TlsTest extends TestCase {
	
	private Shootist shootist;
	private Shootme shootme;


	public void setUp() {
		  // setup TLS properties
        System.setProperty( "javax.net.ssl.keyStore",  TlsTest.class.getResource("testkeys").getPath() );
        System.setProperty( "javax.net.ssl.trustStore", TlsTest.class.getResource("testkeys").getPath() );
        System.setProperty( "javax.net.ssl.keyStorePassword", "passphrase" );
        System.setProperty( "javax.net.ssl.keyStoreType", "jks" );
        this.shootist = new Shootist();
        this.shootme = new Shootme();
        this.shootme.init();
	}
	
	public void testTls() {
		this.shootist.init();
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
