package gov.nist.javax.sip.stack;

import java.io.IOException;
import java.net.InetAddress;

import javax.sip.ListeningPoint;

public class NioMessageProcessorFactory implements MessageProcessorFactory {

    @Override  
    public MessageProcessor createMessageProcessor(
                SIPTransactionStack sipStack, InetAddress ipAddress, int port,
                String transport) throws IOException {
            if (transport.equalsIgnoreCase(ListeningPoint.UDP)) {
                UDPMessageProcessor udpMessageProcessor = new UDPMessageProcessor(
                        ipAddress, sipStack, port);         
                sipStack.udpFlag = true;
                return udpMessageProcessor;
            } else if (transport.equalsIgnoreCase(ListeningPoint.TCP)) {
                NioTcpMessageProcessor nioTcpMessageProcessor = new NioTcpMessageProcessor(
                        ipAddress, sipStack, port);         
                // this.tcpFlag = true;
                return nioTcpMessageProcessor;
            } else if (transport.equalsIgnoreCase(ListeningPoint.TLS)) {
                NioTlsMessageProcessor tlsMessageProcessor = new NioTlsMessageProcessor(
                        ipAddress, sipStack, port);         
                // this.tlsFlag = true;
                return tlsMessageProcessor;
            } else if (transport.equalsIgnoreCase(ListeningPoint.SCTP)) {

                // Need Java 7 for this, so these classes are packaged in a separate
                // jar
                // Try to load it indirectly, if fails report an error
                try {
                    Class<?> mpc = ClassLoader.getSystemClassLoader().loadClass(
                            "gov.nist.javax.sip.stack.sctp.SCTPMessageProcessor");
                    MessageProcessor mp = (MessageProcessor) mpc.newInstance();
                    mp.initialize(ipAddress, port, sipStack);               
                    return mp;
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException(
                            "SCTP not supported (needs Java 7 and SCTP jar in classpath)");
                } catch (InstantiationException ie) {
                    throw new IllegalArgumentException("Error initializing SCTP",
                            ie);
                } catch (IllegalAccessException ie) {
                    throw new IllegalArgumentException("Error initializing SCTP",
                            ie);
                }
            } else {
                throw new IllegalArgumentException("bad transport");
            }
     }

  

}
