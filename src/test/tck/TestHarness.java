package test.tck;

import gov.nist.javax.sip.address.AddressFactoryImpl;
import gov.nist.javax.sip.stack.NioMessageProcessorFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Properties;

import javax.sip.SipFactory;
import javax.sip.address.AddressFactory;
import javax.sip.header.HeaderFactory;
import javax.sip.message.MessageFactory;

import junit.framework.TestCase;
import junit.framework.TestResult;

import org.apache.log4j.Appender;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

public class TestHarness extends TestCase {

    private static final String PATH_GOV_NIST = "gov.nist";

    protected static final String IMPLEMENTATION_PATH = "javax.sip.tck.PATH";

    protected static final String ABORT_ON_FAIL = "javax.sip.tck.ABORT_ON_FAIL";

    protected static final String LOG_FILE_NAME = "javax.sip.tck.LOG_FILE";

    protected static final String LOCAL_ADDRESS = "127.0.0.1";

    protected static final int TI_PORT = 5060;

    protected static final int RI_PORT = 6050;

    // Keep these static but initialize from the constructor to allow
    // changing from the GUI
    protected static String logFileName = "tcklog.txt";

    protected static String path = null;

    protected static PrintWriter printWriter;

    protected static boolean abortOnFail  = true;

    // this flag is set to false if there is any failure throughout the test
    // cycle
    // on either side of the protocol. It helps account for failures that are
    // not triggered in the
    // main test thread. It has to be initialized before each run.
    private static boolean testPassed = true;

    protected static MessageFactory tiMessageFactory;

    protected static HeaderFactory tiHeaderFactory;

    protected static AddressFactory tiAddressFactory;

    protected static MessageFactory riMessageFactory;

    protected static HeaderFactory riHeaderFactory;

    protected static AddressFactory riAddressFactory;

    protected static int testCounter;

    protected static SipFactory riFactory;

    protected static SipFactory tiFactory;

    protected TestResult testResult;

    private static Logger logger = Logger.getLogger("test.tck");

    private static String currentMethodName;

    private static String currentClassName;

    protected static Appender console = new ConsoleAppender(new SimpleLayout());


    static {
        try {
            Properties tckProperties = new Properties();


//            tckProperties.load(TestHarness.class.getClassLoader()
//                    .getResourceAsStream("tck.properties"));
            tckProperties.load(new FileInputStream("tck.properties"));
            Enumeration props = tckProperties.propertyNames();
            while (props.hasMoreElements()) {
                String propname = (String) props.nextElement();
                System.setProperty(propname, tckProperties
                        .getProperty(propname));
            }

            path = System.getProperties().getProperty(IMPLEMENTATION_PATH);
            String flag = System.getProperties().getProperty(ABORT_ON_FAIL);

            String lf = System.getProperties().getProperty(LOG_FILE_NAME);
            if (lf != null)
                logFileName = lf;
            abortOnFail = (flag != null && flag.equalsIgnoreCase("true"));

            // JvB: init log4j
            //PropertyConfigurator.configure("log4j.properties");

            BasicConfigurator.configure();

            // If already created a print writer then just use it.
            if (lf != null)
                logger.addAppender(new FileAppender(new SimpleLayout(),
                        logFileName));
            else
                logger.addAppender(new FileAppender(new SimpleLayout(),
                        "tckoutput.txt"));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

    private static void println(String messageToPrint) {
        logger.info(messageToPrint);
    }

    /**
     * Default constructor
     */
    protected TestHarness() {

    }

    protected String getImplementationPath() {
        return System.getProperties().getProperty( IMPLEMENTATION_PATH, "gov.nist" );
    }

    public TestHarness(String name) {
        this(name, false); // default: disable auto-dialog
    }

    protected TestHarness(String name, boolean autoDialog) {
        super(name);
        this.testResult = new TestResult();

        getRIFactories(autoDialog);
        getTIFactories();

    }

    private static void logSuccess(String message) {
        testCounter++;
        Throwable throwable = new Throwable();
        StackTraceElement frameset[] = throwable.getStackTrace();
        StackTraceElement frame = frameset[2];
        String className = frame.getClassName();
        //int ind = className.lastIndexOf(".");
        //if (ind != -1) {
        //  className = className.substring(ind + 1);
        //}

        logger.info(className + ":" + frame.getMethodName() + "(" +
                        frame.getFileName() + ":"+ frame.getLineNumber() + ")" +  " : Status =  passed ! ");
        String methodName = frame.getMethodName();

        if (!(currentMethodName != null && methodName.equals(currentMethodName) && currentClassName
                .equals(className))) {
            currentClassName = className;
            currentMethodName = methodName;
            System.out.println("\n");
            System.out.print(currentClassName + ":" + currentMethodName);
        }
    }



    private static void logSuccess() {
        Throwable throwable = new Throwable();
        StackTraceElement frameset[] = throwable.getStackTrace();
        StackTraceElement frame = frameset[2];
        String className = frame.getClassName();
        //int ind = className.lastIndexOf(".");
        //if (ind != -1) {
        //  className = className.substring(ind + 1);
        //}
        logger.info(className + ":" + frame.getMethodName() + ": Status =  passed ! ");
        String methodName = frame.getMethodName();

        if (!(currentMethodName != null && methodName.equals(currentMethodName) && currentClassName
                .equals(className))) {
            currentClassName = className;
            currentMethodName = methodName;
            System.out.println("\n");
            System.out.print(currentClassName + ":" + currentMethodName);
        }
    }



    private static void logFailureDetails(String reason) {
        Throwable throwable = new Throwable();
        StackTraceElement frameset[] = throwable.getStackTrace();
        StackTraceElement frame = frameset[2];
        String className = frame.getClassName();
        logFailure(className, frame.getMethodName(), reason);

    }



    private static void logFailure(String className, String methodName,
            String reason) {
        println(" Test in function " + className
                + ":" + methodName + " failed because of " + reason);

        StringWriter stringWriter = new StringWriter();
        new Exception().printStackTrace(new PrintWriter(stringWriter));
        println(stringWriter.getBuffer().toString());

        testPassed = false;
        if (abortOnFail) {
            new Exception().printStackTrace();
            System.exit(0);
        }
    }

    private static void logFailure(String reason) {
        logFailureDetails(reason);
    }

    public static void assertTrue(boolean cond) {
        if (cond) {
            logSuccess();
        } else {
            logFailure("assertTrue failed");
        }
        if (!cond) {
            new Exception().printStackTrace();
            fail("assertion failure");
        }

        TestCase.assertTrue(cond);
    }

    public static void assertTrue(String diagnostic, boolean cond) {
        if (cond) {
            logSuccess("assertTrue " + diagnostic);
        } else {
            logFailure(diagnostic);
        }
        if (!cond) {
            new Exception(diagnostic).printStackTrace();
            fail(diagnostic + " : Assertion Failure ");

        }

        TestCase.assertTrue(diagnostic, cond);
    }

    public static void assertEquals(Object me, Object him) {
        if (me == him) {
            logSuccess();
        } else if (me == null && him != null) {
            logFailure("assertEquals failed");

        } else if (me != null && him == null) {
            logFailure("assertEquals failed");

        } else if (!me.equals(him)) {
            logFailure("assertEquals failed");

        }
        TestCase.assertEquals(me, him);
    }

    public static void assertEquals(String me, String him) {
        if (me == him) {
            logSuccess();
        } else if (me == null && him != null) {
            logFailure("assertEquals failed");

        } else if (me != null && him == null) {
            logFailure("assertEquals failed");

        } else if (!me.equals(him)) {
            logFailure("assertEquals failed");

        }
        TestCase.assertEquals(me, him);
    }

    public static void assertEquals(String reason, Object me, Object him) {
        if (me == him) {
            logSuccess("assertEquals : " + reason);
        } else if (me == null && him != null) {
            logFailure("assertEquals failed:" + reason );
        } else if (me != null && him == null) {
            logFailure("assertEquals failed:" + reason );
        } else if (!me.equals(him)) {
            logFailure(reason);
        }
        TestCase.assertEquals(reason, me, him);
    }

    public static void assertEquals(String reason, String me, String him) {
        if (me == him) {
            logSuccess("assertEquals " + reason);
        } else if (me == null && him != null) {
            logFailure("assertEquals failed");
        } else if (me != null && him == null) {
            logFailure("assertEquals failed");
        } else if (!me.equals(him)) {
            logFailure("assertEquals failed");
        }
        TestCase.assertEquals(reason, me, him);

    }

    public static void assertNotNull(String reason, Object thing) {
        if (thing != null) {
            logSuccess("assertNotNull " + reason);
        } else {
            logFailure(reason);
        }
        TestCase.assertNotNull(reason, thing);
    }

    public static void assertNull(String reason, Object thing) {
        if (thing == null) {
            logSuccess("assertNull " + reason);
        } else {
            logFailure(reason);
        }
        TestCase.assertNull(reason, thing);
    }

    public static void assertSame(String diagnostic, Object thing,
            Object thingie) {
        if (thing == thingie) {
            logSuccess("assertSame " + diagnostic);
        } else {
            logFailure(diagnostic);
        }
        TestCase.assertSame(diagnostic, thing, thingie);
    }

    public static void fail(String message) {
        logFailure(message);

        TestCase.fail(message);
    }

    public static void fail(String message, Exception ex) {
        logFailure(message);
        logger.error(message,ex);
        TestCase.fail( message );
    }

    public static void fail() {
        logFailure("Unknown reason for failure. Check logs for more info.");
        new Exception().printStackTrace();
        TestCase.fail();
    }

    public static void checkImplementsInterface(Class implementationClass,
            Class jainInterface) {

        assertTrue(jainInterface.toString() + " is_assignable_from "
                + implementationClass.toString(), jainInterface
                .isAssignableFrom(implementationClass));
    }

    public static boolean implementsInterface(Class implementationClass,
            Class jainInterface) {
        return jainInterface.isAssignableFrom(implementationClass);
    }

    static void getTIFactories() {
        try {
            tiFactory = SipFactory.getInstance();

            // JvB: need this! but before setting path
            tiFactory.resetFactory();

            // if no TI path is specified on the command line, then assume
            // RI self-test mode

            //String tiPathName = System.getProperty( IMPLEMENTATION_PATH, PATH_RI_HELPER );
            String tiPathName = System.getProperty( IMPLEMENTATION_PATH, "gov.nist" );

            // Yes this does access implementation classes but we have to do
            // things
            // this way for self test. v1.2 only assumes one instance of
            // factories per vendor
            // per jvm.
            tiFactory.setPathName(tiPathName);

            tiAddressFactory = tiFactory.createAddressFactory();
            tiHeaderFactory = tiFactory.createHeaderFactory();
            tiMessageFactory = tiFactory.createMessageFactory();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out
                    .println("Cannot get TI factories -- cannot proceed! Bailing");
            System.exit(0);
        }
        // Cannot sensibly proceed so bail out.
        if (tiAddressFactory == null || tiMessageFactory == null
                || tiHeaderFactory == null) {
            System.out
                    .println("Cannot get TI factories --  cannot proceed! Bailing!!");
            System.exit(0);
        }
    }

    static void getRIFactories(boolean autoDialog) {
        try {
            riFactory = SipFactory.getInstance();
            if (riFactory == null) {
                throw new TckInternalError("could not get SipFactory");
            }
            riFactory.resetFactory();

            // Testing against the RI.
            riFactory.setPathName(PATH_GOV_NIST);

            riAddressFactory = riFactory.createAddressFactory();

            assertTrue( "RI must be gov.nist implementation", riAddressFactory instanceof AddressFactoryImpl );

            riHeaderFactory = riFactory.createHeaderFactory();
            riMessageFactory = riFactory.createMessageFactory();
        } catch (Exception ex) {
            throw new TckInternalError("Could not get factories");
        }
    }

    public void logTestCompleted() {
    	TestCase.assertTrue( testPassed );
        logger.info(this.getName() + " Completed");
    }

    public void logTestCompleted(String info) {
      TestCase.assertTrue( testPassed );
        logger.info(this.getName() + ":" + info +" Completed");
    }


    /**
     * Returns a properties object containing all RI settings. The result from
     * this method is passed to the SipFactory when creating the RI Stack
     */
    public static Properties getRiProperties(boolean autoDialog) {
        // TODO collect all system properties
        // prefixed javax.sip.tck.ri and add them to the local
        // properties object

        Properties properties = new Properties();

        // IP_ADDRESS is deprecated as of jsip 1.2.
        // Each listening point associated with a stack has its own IP address.
        properties.setProperty("javax.sip.STACK_NAME", "RiStack");
//        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "0");

        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
        properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
                "logs/riDebugLog.txt");
        properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
                "logs/riMessageLog.txt");

        // JvB: Most TCK tests dont work well with automatic dialog support
        // enabled
        // Disable it for the moment
        properties.setProperty("javax.sip.AUTOMATIC_DIALOG_SUPPORT", autoDialog ? "ON" : "OFF");

        // JvB: for testing of ACK to non-2xx
        properties.setProperty(
                "gov.nist.javax.sip.PASS_INVITE_NON_2XX_ACK_TO_LISTENER",
                "true");
        // For testing sending of stateless null keepalive messages.
        //@see test.tck.msgflow.SipProviderTest.testSendNullRequest
        properties.setProperty("javax.sip.OUTBOUND_PROXY", LOCAL_ADDRESS + ":" + TI_PORT + "/udp");
        if(System.getProperty("enableNIO") != null && System.getProperty("enableNIO").equalsIgnoreCase("true")) {
        	logger.info("\nNIO Enabled\n");
        	properties.setProperty("gov.nist.javax.sip.MESSAGE_PROCESSOR_FACTORY", NioMessageProcessorFactory.class.getName());
        }

        return properties;
    }

    /**
     * Returns a properties object containing all TI settings. The result from
     * this method is passed to the SipFactory when creating the TI Stack
     *
     *
     */
    public static Properties getTiProperties() {
        // TODO collect all system properties
        // prefixed javax.sip.tck.ti and add them to the local
        // properties object

        Properties properties = new Properties();
        // IP_ADDRESS is deprecated as of jsip 1.2.
        // Each listening point associated with a stack has its own IP address.
        // properties.setProperty("javax.sip.IP_ADDRESS", LOCAL_ADDRESS);
        properties.setProperty("javax.sip.STACK_NAME", "TiStack");

        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
        properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
                "logs/tiDebugLog.txt");
        properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
                "logs/tiMessageLog.txt");
        // For testing sending of stateless null keepalive messages.
        //@see test.tck.msgflow.SipProviderTest.testSendNullRequest
        properties.setProperty("javax.sip.OUTBOUND_PROXY", LOCAL_ADDRESS + ":" + RI_PORT + "/udp");
        if(System.getProperty("enableNIO") != null && System.getProperty("enableNIO").equalsIgnoreCase("true")) {
        	logger.info("\nNIO Enabled\n");
        	properties.setProperty("gov.nist.javax.sip.MESSAGE_PROCESSOR_FACTORY", NioMessageProcessorFactory.class.getName());
        }
        return properties;
    }

    public void setUp() throws Exception {
        testPassed = true;
    }

    public void tearDown() throws Exception {
        assertTrue("Test failed. See log for details.", testPassed);
    }
}
