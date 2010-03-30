package examples.nistgoodies.threadaudit;

import gov.nist.javax.sip.stack.SIPTransactionStack;

import javax.sip.*;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This example demonstrates how an application can monitor
 * the health of the internal threads of the SIP Stack.
 *
 * This code is in the public domain.
 *
 * @author R. Borba (Natural Convergence)
 *
 */
public class ThreadAudit {

    /// SIP Stack objects
    private static SipStack sipStack;
    private static ListeningPoint listeningPoint;

    /// Test timer
    private static Timer timer = new Timer();

    /// Interval between thread audits
    private static long auditIntervalInMillis;

    /// Initializes the stack and starts the periodic audit
    public static void init(boolean enableThreadAudit, long auditInterval) {
        // Save the audit interval for future use
        auditIntervalInMillis = auditInterval;

        /// Initialize the stack properties
        Properties properties = new Properties();
        properties.setProperty("javax.sip.STACK_NAME", "Thread Audit Sample");
        if (enableThreadAudit) {
            // That's all we need to do in order to enable the thread auditor
            properties.setProperty("gov.nist.javax.sip.THREAD_AUDIT_INTERVAL_IN_MILLISECS",
                                    String.valueOf(auditInterval));
        }
        System.out.println("Thread Audit is " + (enableThreadAudit ? "enabled" : "disabled"));

        // Create and initialize the SIP Stack
        initSipStack(properties);

        // Start monitoring the health of the internal threads
        startThreadAudit();

        // Sleep for a while so we can see some good audit reports being generated
        sleep(4 * auditIntervalInMillis);

        // Kill one of the internal threads of the SIP stack so we can detect it in the next audit
        System.out.println("Killing one of the internal threads on purpose to see if the thread auditor detects it");
        try {
            sipStack.deleteListeningPoint(listeningPoint);
        } catch (ObjectInUseException e) {
            System.err.println("Failed to delete UDP listening point");
            e.printStackTrace();
            System.exit(0);
        }

        // Sleep again to see if we're able to detect the listening point thread going away
        sleep(4 * auditIntervalInMillis);

        System.out.println("Done!");
        System.exit(0);
    }

    /// Creates and initializes the SIP Stack
    private static void initSipStack(Properties properties)
    {
        // Create the SIP Stack
        SipFactory l_oSipFactory = SipFactory.getInstance();
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
    private static void startThreadAudit()
    {
        /// Timer class used to periodically audit the stack
        class AuditTimer extends TimerTask {
            /// Action to be performed by this timer task
            public final void run() {
                // That's all we need to do in order to check if the internal threads of the stack are healthy
                String auditReport = ((SIPTransactionStack) sipStack).getThreadAuditor().auditThreads();
                if (auditReport != null) {
                    System.out.println("--> RED ALERT!!! " + auditReport);
                } else {
                    System.out.println("--> Internal threads of the stack appear to be healthy...");
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
        ThreadAudit.init(true, 5000);
    }
}
