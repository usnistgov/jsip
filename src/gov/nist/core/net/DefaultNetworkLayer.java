package gov.nist.core.net;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * default implementation which passes straight through to java platform
 *
 * @author m.andrews
 *
 */
public class DefaultNetworkLayer implements NetworkLayer {

    /**
     * single default network layer; for flexibility, it may be better not to make it a singleton,
     * but singleton seems to make sense currently.
     */
    public static final DefaultNetworkLayer SINGLETON = new DefaultNetworkLayer();

    private DefaultNetworkLayer() {

    }

    public ServerSocket createServerSocket(int port, int backlog, InetAddress bindAddress)
            throws IOException {
        return new ServerSocket(port, backlog, bindAddress);
    }

    public Socket createSocket(InetAddress address, int port) throws IOException {
        return new Socket(address, port);
    }

    public DatagramSocket createDatagramSocket() throws SocketException {
        return new DatagramSocket();
    }

    public DatagramSocket createDatagramSocket(int port, InetAddress laddr) throws SocketException {
        return new DatagramSocket(port, laddr);
    }

}