package test.tck;
import junit.framework.*;
import javax.sip.header.*;
import javax.sip.*;
import javax.sip.address.*;
import javax.sip.message.*;
import java.io.*;

public class TestHarness extends TestCase {

	protected static final String IMPLEMENTATION_PATH = "javax.sip.tck.PATH";
	protected static final String ABORT_ON_FAIL = "javax.sip.tck.ABORT_ON_FAIL";
	protected static final String LOG_FILE_NAME = "javax.sip.tck.LOG_FILE";

	// Keep these static but initialize from the constructor to allow
	// changing from the GUI
	protected static String logFileName = "tcklog.txt";
	protected static String path = null;
	protected static PrintWriter printWriter;
	protected static boolean abortOnFail;

	protected SipFactory tiSipFactory;
	protected MessageFactory tiMessageFactory;
	protected HeaderFactory tiHeaderFactory;
	protected AddressFactory tiAddressFactory;

	protected SipFactory riSipFactory;
	protected MessageFactory riMessageFactory;
	protected HeaderFactory riHeaderFactory;
	protected AddressFactory riAddressFactory;

	protected static int testCounter;
	protected TestResult testResult;

	private static void println(String messageToPrint) {
		System.out.println(messageToPrint);
		printWriter.println(messageToPrint);
	}

	public TestHarness(String name) {
		super(name);
		this.testResult = new TestResult();
		if (System.getProperties().getProperty(IMPLEMENTATION_PATH) != null)
			path = System.getProperties().getProperty(IMPLEMENTATION_PATH);
		String flag = System.getProperties().getProperty(ABORT_ON_FAIL);
		String lf = System.getProperties().getProperty(LOG_FILE_NAME);
		if (lf != null)
			logFileName = lf;
		abortOnFail = (flag != null && flag.equalsIgnoreCase("true"));

		// If already created a print writer then just use it.
		if (printWriter == null) {
			try {
				File logFile = new File(logFileName);
				if (!logFile.exists()) {
					logFile.createNewFile();
					printWriter = null;
				} else {
					System.out.println(
						"please remove the log file " + logFileName);
					System.exit(0);
				}
				// Append buffer to the end of the file.
				if (printWriter == null) {
					FileWriter fw = new FileWriter(logFileName, true);
					printWriter = new PrintWriter(fw, true);
				}
			} catch (IOException ex) {
				ex.printStackTrace();
				System.out.println("Could not create/write log file");
				System.exit(0);
			}
		}
		getFactories();
		getRIFactories();
	}

	private static void logSuccess(String message) {
		testCounter++;
		println("testPoint " + testCounter + " : " + message + " passed ! ");
	}

	private static void logFailure(String message, Object[] args) {
		StringBuffer stringBuffer = new StringBuffer(message);
		stringBuffer.append("(");
		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				stringBuffer.append(
					args[i] != null ? args[i].toString() : null);
				if (i < args.length)
					stringBuffer.append(",");
			}
		}
		stringBuffer.append(")");
		logFailure(stringBuffer.toString());
	}

	private static void logSuccess() {
		Throwable throwable = new Throwable();
		StackTraceElement frameset[] = throwable.getStackTrace();
		StackTraceElement frame = frameset[2];
		logSuccess(frame.getMethodName());
	}

	private static void logFailure() {
		Throwable throwable = new Throwable();
		StackTraceElement frameset[] = throwable.getStackTrace();
		StackTraceElement frame = frameset[2];
		logFailure(frame.getMethodName());
	}

	private static void logFailure(Throwable sframe, Object[] args) {
		StackTraceElement frameset[] = sframe.getStackTrace();
		StackTraceElement frame = frameset[0];
		sframe.printStackTrace();
		logFailure(frame.getMethodName(), args);
	}

	private static void logFailure(String reason) {
		println(
			" Test point "
				+ testCounter
				+ " in function "
				+ reason
				+ " failed ");
		if (abortOnFail) {
			new Exception().printStackTrace();
			System.exit(0);
		}
	}

	public static void assertTrue(boolean cond) {
		if (cond) {
			logSuccess();
		} else {
			logFailure();
		}
		if (!cond) {
			new Exception().printStackTrace();
		}

		TestCase.assertTrue(cond);
	}

	public static void assertTrue(String diagnostic, boolean cond) {
		if (cond) {
			logSuccess();
		} else {
			logFailure();
		}
		if (!cond) {
			new Exception().printStackTrace();
			System.exit(0);
		}

		TestCase.assertTrue(diagnostic, cond);
	}

	public static void assertEquals(Object me, Object him) {
		if (me == him) {
			logSuccess();
		} else if (me == null && him != null) {
			logFailure();
		} else if (me != null && him == null) {
			logFailure();
		} else if (!me.equals(him)) {
			logFailure();
		}
		TestCase.assertEquals(me, him);
	}

	public static void assertEquals(String me, String him) {
		if (me == him) {
			logSuccess();
		} else if (me == null && him != null) {
			logFailure();
		} else if (me != null && him == null) {
			logFailure();
		} else if (!me.equals(him)) {
			logFailure();
		}
		TestCase.assertEquals(me, him);
	}

	public static void assertEquals(String reason, Object me, Object him) {
		if (me == him) {
			logSuccess();
		} else if (me == null && him != null) {
			logFailure();
		} else if (me != null && him == null) {
			logFailure();
		} else if (!me.equals(him)) {
			logFailure();
		}
		TestCase.assertEquals(reason, me, him);
	}

	public static void assertEquals(String reason, String me, String him) {
		if (me == him) {
			logSuccess();
		} else if (me == null && him != null) {
			logFailure();
		} else if (me != null && him == null) {
			logFailure();
		} else if (!me.equals(him)) {
			logFailure();
		}
		TestCase.assertEquals(reason, me, him);

	}

	public static void assertNotNull(String reason, Object thing) {
		if (thing != null) {
			logSuccess();
		} else {
			logFailure();
		}
		TestCase.assertNotNull(reason, thing);
	}

	public static void assertNull(String reason, Object thing) {
		if (thing == null) {
			logSuccess();
		} else {
			logFailure();
		}
		TestCase.assertNull(reason, thing);
	}

	public static void assertSame(
		String diagnostic,
		Object thing,
		Object thingie) {
		if (thing == thingie) {
			logSuccess();
		} else {
			logFailure();
		}
		TestCase.assertSame(diagnostic, thing, thingie);
	}

	public static void checkImplementsInterface(
		Class implementationClass,
		Class jainInterface) {
		assertTrue(jainInterface.isAssignableFrom(implementationClass));
	}

	public static boolean implementsInterface(
		Class implementationClass,
		Class jainInterface) {
		return jainInterface.isAssignableFrom(implementationClass);
	}

	void getFactories() {
		try {
			tiSipFactory = SipFactory.getInstance();
			if (path == null)
				path = "gov.nist";
			tiSipFactory.setPathName(path.trim());
			tiAddressFactory = tiSipFactory.createAddressFactory();
			tiHeaderFactory = tiSipFactory.createHeaderFactory();
			tiMessageFactory = tiSipFactory.createMessageFactory();
		} catch (Exception ex) {
			assertTrue(false);
			System.out.println(
				"Cannot get TI factories -- cannot proceed! Bailing");
			System.exit(0);
		}
		// Cannot sensibly proceed so bail out.
		if (tiAddressFactory == null
			|| tiMessageFactory == null
			|| tiHeaderFactory == null) {
			System.out.println(
				"Cannot get TI factories --  cannot proceed! Bailing!!");
			System.exit(0);
		}
	}

	void getRIFactories() {
		try {
			riSipFactory = SipFactory.getInstance();
			if (riSipFactory == null) {
				throw new TckInternalError("could not get SipFactory");
			}
			String path = "gov.nist";
			riSipFactory.setPathName(path);
			riAddressFactory = riSipFactory.createAddressFactory();
			if (riAddressFactory == null) {
				throw new TckInternalError("could not create RI Address Factory -- check class path");
			}
			riHeaderFactory = riSipFactory.createHeaderFactory();
			if (riHeaderFactory == null) {
				throw new TckInternalError("could not create RI Header Factory -- check class path");
			}
			riMessageFactory = riSipFactory.createMessageFactory();
			if (riMessageFactory == null) {
				throw new TckInternalError("Cold not create RI Message Factory -- check class path");
			}
		} catch (Exception ex) {
			throw new TckInternalError("Could not get factories");
		}
	}

	public void logTestCompleted() {
		Throwable throwable = new Throwable();
		StackTraceElement frameset[] = throwable.getStackTrace();
		StackTraceElement frame = frameset[2];
		// println( "Completed " + frame.getMethodName() );
	}

	public void logTestCompleted(String name) {
		// println( "Completed " + name );
	}

}
