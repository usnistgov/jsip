package examples.nistgoodies.leakaudit;

import gov.nist.javax.sip.stack.SIPTransactionStack;

import javax.sip.*;
import java.util.Timer;
import java.util.Properties;
import java.util.TimerTask;
import java.util.Set;

/**
 * This example demonstrates how an application can monitor the SIP Stack
 * for leaked dialogs and transactions.
 * <p/>
 * This code is in the public domain.
 *
 * @author R. Borba (Natural Convergence)
 */
public class LeakAudit {

    /// SIP Stack objects
    protected static SipStack sipStack;
    protected static ListeningPoint listeningPoint;
    protected static SipProvider sipProvider;

    /// Test timer
    private static Timer timer = new Timer();

    /// Interval between audits
    private static long auditIntervalInMillis = 5000;

    /// The leaking application
    private static LeakingApp leakingApp;

    /// Initializes the stack and starts the periodic audit
    public static void init() {
        // Create an application that leaks resources
        leakingApp = new LeakingApp();

        // Create and initialize the SIP Stack
        Properties properties = new Properties();
        properties.setProperty("javax.sip.STACK_NAME", "Leak Audit Sample");
        properties.setProperty("javax.sip.IP_ADDRESS", "127.0.0.1");
        initSipStack(properties, leakingApp);

        // Start a few dialogs
        leakingApp.sendRequest();
        leakingApp.sendRequest();
        leakingApp.sendRequest();

        // Start monitoring the health of the internal threads
        startAudit();

        // Sleep for a while so we can see some good audit reports being generated
        sleep(2 * auditIntervalInMillis);

        // Leak a dialog into the stack
        leakingApp.leakDialog();

        // Sleep again to see if we're able to detect the listening point thread going away
        sleep(20 * auditIntervalInMillis);

        System.out.println("Done!");
        System.exit(0);
    }

    /// Creates and initializes the SIP Stack
    private static void initSipStack(Properties properties, SipListener sipListener) {
        // Create the SIP Stack
        SipFactory l_oSipFactory = SipFactory.getInstance();
        l_oSipFactory.resetFactory();
        l_oSipFactory.setPathName("gov.nist");
        try {
            sipStack = l_oSipFactory.createSipStack(properties);
        } catch (PeerUnavailableException e) {
            System.err.println("could not find \"gov.nist.jain.protocol.ip.sip.SipStackImpl\" in the classpath");
            e.printStackTrace();
            System.err.println(e.getMessage());
            System.exit(0);
        }

        // Create a UDP listening point
        try {
            listeningPoint = sipStack.createListeningPoint("127.0.0.1", 5060, "UDP");
        } catch (Exception e) {
            System.err.println("Failed to create UDP listening point");
            e.printStackTrace();
            System.exit(0);
        }

        // Create a SIP Provider
        try {
            sipProvider = sipStack.createSipProvider(listeningPoint);
            sipProvider.addSipListener(sipListener);
        } catch (Exception e) {
            System.err.println("Failed to create sip provider");
            e.printStackTrace();
            System.exit(0);
        }

    }

    /// Sleeps for a while
    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            System.err.println("Can't sleep");
            e.printStackTrace();
            System.exit(0);
        }
    }

    // Kicks off the periodic audit
    private static void startAudit() {
        /// Timer class used to periodically audit the stack
        class AuditTimer extends TimerTask {
            /// Action to be performed by this timer task
            public final void run() {
                // That's all we need to do in order to check if there's any dialog or transaction leak
                Set activeCallIDs = leakingApp.getActiveCallIDs();
                String auditReport = ((SIPTransactionStack) sipStack).auditStack(activeCallIDs,
                        30 * 1000, // Note: We're using an unrealistically short value just for this test.
                                   // For real applications, see the suggested values in the README.txt.
                        60 * 1000);
                if (auditReport != null) {
                    System.out.println("--> RED ALERT!!! " + auditReport);
                } else {
                    System.out.println("--> No leaks detected.");
                }

                // Schedule the next audit
                timer.schedule(new AuditTimer(), auditIntervalInMillis);
            }
        }

        // Kick off the audit timer
        timer.schedule(new AuditTimer(), auditIntervalInMillis);
    }

    /// Entry point
    public static void main(String[] args) throws Exception {
        init();
    }
}
