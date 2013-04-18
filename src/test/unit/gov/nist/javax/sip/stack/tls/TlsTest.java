package test.unit.gov.nist.javax.sip.stack.tls;

import gov.nist.javax.sip.stack.NioMessageProcessorFactory;

import java.util.Properties;

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
//        this.shootist = new Shootist();
//		this.shootme = new Shootme();
//		this.shootme.init();
	}

	public void testNIOTlsProtocols() {        
        this.shootme = new Shootme();
        this.shootme.init();
        this.shootist = new Shootist();
        Properties properties = new Properties();        
        // If you want to use UDP then uncomment this.
        //properties.setProperty(
        //  "javax.sip.ROUTER_PATH",
        //  "examples.shootistTLS.MyRouter");
        properties.setProperty("javax.sip.STACK_NAME", "shootist");
   
        // The following properties are specific to nist-sip
        // and are not necessarily part of any other jain-sip
        // implementation.
        // You can set a max message size for tcp transport to
        // guard against denial of service attack.
        properties.setProperty("gov.nist.javax.sip.MAX_MESSAGE_SIZE",
                    "1048576");
        properties.setProperty(
            "gov.nist.javax.sip.DEBUG_LOG",
            "logs/shootistdebug.txt");
        properties.setProperty(
            "gov.nist.javax.sip.SERVER_LOG",
            "logs/shootistlog.txt");
        properties.setProperty(
                "gov.nist.javax.sip.SSL_HANDSHAKE_TIMEOUT", "10000");
        properties.setProperty("gov.nist.javax.sip.TCP_POST_PARSING_THREAD_POOL_SIZE", "20");
        properties.setProperty("gov.nist.javax.sip.TLS_SECURITY_POLICY",
                Shootist.class.getName());

        // Drop the client connection after we are done with the transaction.
        properties.setProperty("gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS", "false");
        // Set to 0 in your production code for max speed.
        // You need  16 for logging traces. 32 for debug + traces.
        // Your code will limp at 32 but it is best for debugging.
        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
        properties.setProperty("gov.nist.javax.sip.MESSAGE_PROCESSOR_FACTORY", NioMessageProcessorFactory.class.getName());
        properties.setProperty("gov.nist.javax.sip.TLS_CLIENT_PROTOCOLS", "SSLv2Hello, TLSv1");
        this.shootist.init("localhost", properties);
    }
	
	public void testTls() {
        this.shootist = new Shootist();
        this.shootme = new Shootme();
        this.shootme.init();
		this.shootist.init("localhost");
	}
	
	@Override
	public void tearDown() throws Exception {
            Thread.sleep(2000);
            if(this.shootme != null)
            	this.shootme.stop();
            if(this.shootist != null)
            	this.shootist.stop();
           

            System.clearProperty( "javax.net.ssl.keyStore" );
            System.clearProperty( "javax.net.ssl.trustStore" );
            System.clearProperty( "javax.net.ssl.keyStorePassword" );
            System.clearProperty( "javax.net.ssl.keyStoreType" );

           
    }
}
