package test.unit.gov.nist.javax.sip.stack.tls;

import gov.nist.javax.sip.stack.NioMessageProcessorFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;

import javax.sip.ListeningPoint;

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
	
	// Non Regression test for https://java.net/jira/browse/JSIP-492
	public void testTlsResponseContactTCP() {
        this.shootist = new Shootist();
        this.shootme = new Shootme();
        this.shootme.init();
        this.shootme.setResponseTransport(ListeningPoint.TCP);
		this.shootist.init("localhost");
	}
	
	/*
	 * Non regression test for https://java.net/jira/browse/JSIP-464
	 */
	public void testTlsUnsecure() throws Exception {
        this.shootme = new Shootme();
        Properties properties = new Properties();        
        // If you want to use UDP then uncomment this.
        //properties.setProperty(
        //  "javax.sip.ROUTER_PATH",
        //  "examples.shootistTLS.MyRouter");
        properties.setProperty("javax.sip.STACK_NAME", "shootme");
   
        // The following properties are specific to nist-sip
        // and are not necessarily part of any other jain-sip
        // implementation.
        // You can set a max message size for tcp transport to
        // guard against denial of service attack.
        properties.setProperty("gov.nist.javax.sip.MAX_MESSAGE_SIZE",
                    "1048576");
        properties.setProperty(
            "gov.nist.javax.sip.DEBUG_LOG",
            "logs/shootmetlsunsecure_debug.txt");
        properties.setProperty(
            "gov.nist.javax.sip.SERVER_LOG",
            "logs/shootmetlsunsecure_log.txt");
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
        this.shootme.init(properties);
        
        Socket socketClient = null;
        DataOutputStream os = null;
        DataInputStream is = null;
		// Initialization section:
		// Try to open a socket on port 25
		// Try to open input and output streams
        try {
            socketClient = new Socket("127.0.0.1", 5071);
            os = new DataOutputStream(socketClient.getOutputStream());
            is = new DataInputStream(socketClient.getInputStream());
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: hostname");
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: hostname");
        }
		// If everything has been initialized then we want to write some data
		// to the socket we have opened a connection to on port 25
        if (socketClient != null && os != null && is != null) {
            try {
            	os.writeBytes("garbage non TLS message nor handshake\n");    
                os.writeBytes("QUIT");
                // keep on reading from/to the socket till we receive the "Ok" from SMTP,
                // once we received that then we want to break.
                String responseLine;
                while ((responseLine = is.readUTF()) != null) {
                    System.out.println("Server: " + responseLine);
                    if (responseLine.indexOf("Ok") != -1) {
                      break;
                    }
                }
            } catch (IOException e) {
                System.err.println("IOException:  " + e);
                return;
            }
        }

        // clean up:
		// close the output stream
		// close the input stream
		// close the socket
        os.close();
        is.close();
        socketClient.close();
        
        fail("Connection not closed on non secure connection");
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
