package gov.nist.core.net;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;

/**
 * 
 *
 * @author m.andrews
 *
 */
public interface NetworkLayer {

    /**
     * equivalent to new "ServerSocket(port,backlog,bindAddress);"
     * 
     * @param port
     * @param backlog
     * @param bindAddress
     * @return
     */
    public ServerSocket createServerSocket(int port, int backlog,
            InetAddress bindAddress) throws IOException;

    /**
     * equivalent to "new java.net.DatagramSocket();"
     * 
     * @return
     */
    public DatagramSocket createDatagramSocket() throws SocketException;

    /**
     * equivalent to "new java.net.DatagramSocket(port,laddr);"
     * 
     * @return
     */
    public DatagramSocket createDatagramSocket(int port, InetAddress laddr)
            throws SocketException;

}