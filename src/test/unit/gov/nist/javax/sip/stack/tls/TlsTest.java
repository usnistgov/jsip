package test.unit.gov.nist.javax.sip.stack.tls;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import test.unit.gov.nist.javax.sip.stack.tls.DeadSocketTlsTest.BadShootist;

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
        this.shootist = new Shootist();
        this.shootme = new Shootme();
        this.shootme.init();
		this.shootist.init("localhost");
	}
	
	public void tearDown() {
		try {
            Thread.sleep(2000);
            if(this.shootme != null)
            	this.shootme.stop();
            if(this.shootist != null)
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
