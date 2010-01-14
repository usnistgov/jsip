package examples.tls;
import javax.sip.address.*;
import java.util.*;


public class HopImpl implements Hop {
     /** Creates new Hop
     *@param hop is a hop string in the form of host:port/Transport
     *@throws IllegalArgument exception if string is not properly formatted or
     * null.
     */
    private String host;
    private int port;
    private String transport;


    public String getHost() { return this.host; }

    public int getPort() { return this.port; }

    public String getTransport() { return this.transport; }

    public HopImpl(String hop) throws IllegalArgumentException {
        if (hop == null) throw new IllegalArgumentException("Null arg!");
        System.out.println("hop = " + hop);
        StringTokenizer stringTokenizer = new StringTokenizer(hop + "/");
        String hostPort = stringTokenizer.nextToken("/");
        transport = stringTokenizer.nextToken().trim();
        // System.out.println("Hop: transport = " + transport);
        if (transport == null) transport = "UDP";
        else if (transport == "") transport = "UDP";
        if (transport.compareToIgnoreCase("UDP") != 0 &&
        transport.compareToIgnoreCase("TLS") != 0 &&
        transport.compareToIgnoreCase("TCP") != 0)  {
            System.out.println("Bad transport string " + transport);
            throw new IllegalArgumentException(hop);
        }

        stringTokenizer = new StringTokenizer(hostPort+":");
        host = stringTokenizer.nextToken(":");
        if (host == null || host.equals( "") )
            throw new IllegalArgumentException("no host!");
        String portString = null;
        try {
            portString = stringTokenizer.nextToken(":");
        } catch (NoSuchElementException ex) {
            // Ignore.
        }
        if (portString == null || portString.equals("")) {
            port = 5060;
        } else {
            try {
                port = Integer.parseInt(portString);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Bad port spec");
            }
        }
    }

}

