package gov.nist.core.net;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;

/**
 * passes straight through to java platform implementations
 *
 * @author m.andrews
 *
 */
public class DefaultNetworkLayer implements NetworkLayer {

    public static final DefaultNetworkLayer SINGLETON = new DefaultNetworkLayer();

    private DefaultNetworkLayer() {

    }

    public ServerSocket createServerSocket(int port, int backlog,
            InetAddress bindAddress) throws IOException {
	    /**
            System.out.println
		("DefaultNetworkLayer.createServerSocket (" 
			+ port + ", " + backlog + ", " + bindAddress + ")");
	    **/
        return new ServerSocket(port, backlog, bindAddress);
    }

    public DatagramSocket createDatagramSocket() throws SocketException {
        // System.out.println("DefaultNetworkLayer.createDatagramSocket()");
        return new DatagramSocket();
    }

    public DatagramSocket createDatagramSocket(int port, InetAddress laddr)
            throws SocketException {
	/**
        System.out.println("DefaultNetworkLayer.createDatagramSocket(" 
		+ port + ", " + laddr + ")");
	**/
        return new DatagramSocket(port, laddr);
    }

}
