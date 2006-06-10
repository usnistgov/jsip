package gov.nist.core.net;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/* Added by Daniel J. Martinez Manzano <dani@dif.um.es> */
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;


/**
 * default implementation which passes straight through to java platform
 *
 * @author m.andrews
 *
 */
public class DefaultNetworkLayer implements NetworkLayer {

    private SSLSocketFactory       sslSocketFactory;
    private SSLServerSocketFactory sslServerSocketFactory;

    /**
     * single default network layer; for flexibility, it may be better not to make it a singleton,
     * but singleton seems to make sense currently.
     */
    public static final DefaultNetworkLayer SINGLETON = new DefaultNetworkLayer();

    private DefaultNetworkLayer() {
        sslServerSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
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

    /* Added by Daniel J. Martinez Manzano <dani@dif.um.es> */
    public SSLServerSocket createSSLServerSocket(int port, int backlog, InetAddress bindAddress) throws IOException
    {
        return (SSLServerSocket) sslServerSocketFactory.createServerSocket(port, backlog, bindAddress);
    }

    /* Added by Daniel J. Martinez Manzano <dani@dif.um.es> */
    public SSLSocket createSSLSocket(InetAddress address, int port) throws IOException
    {
        return (SSLSocket) sslSocketFactory.createSocket(address, port);
    }

    /* Added by Daniel J. Martinez Manzano <dani@dif.um.es> */
    public SSLSocket createSSLSocket(InetAddress address, int port, InetAddress myAddress) throws IOException
    {
        return (SSLSocket) sslSocketFactory.createSocket(address, port, myAddress,0);
    }

   public Socket createSocket(InetAddress address, int port, InetAddress myAddress )  throws IOException {
	return new Socket(address, port, myAddress, 0 );
   }

   
}
