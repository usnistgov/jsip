package test.tck;

/**
 * <p>Title: TCK</p>
 * <p>Description: JAIN SIP 1.1 Technology Compatibility Kit</p>
 *
 * Executes the TCK.
 *
 * @company NIST
 * @author  Emil Ivov (ULP Strasbourg)
 * 			This  code is in the public domain.
 * @version 1.0
 */

public class Tck
{
    public static String ABORT_ON_FAIL = test.tck.TestHarness.ABORT_ON_FAIL;
    public static String IMPLEMENTATION_PATH = test.tck.TestHarness.IMPLEMENTATION_PATH;
    public static String LOG_FILE_NAME = test.tck.TestHarness.LOG_FILE_NAME;

    private static final String TEXT_MODE = "text";
    private static final String GRAPHICAL_MODE = "graphical";

    private static String INTERFACE_MODE = "javax.sip.tck.INTERFACE_MODE";

    public static void printUsage()
    {
        System.out.println("");
        System.out.println("Usage: java test.tck.Tck [<options>]");
        System.out.println("");
        System.out.println("Where options include:");
        System.out.println("        -path <package>  to set the path name of the tested implementation (e.g. gov.nist)");
        System.out.println("        -mode gui|text   to run the TCK in graphical or text mode ");
        System.out.println("        -abortonfail     to tell the TCK to exit after the first failure");
        System.out.println("        -logfile         to tell the TCK where to log results (should be a write-able)");
        System.out.println("        -help            to tell the TCK where to log results (should be a write-able)");
    }

    private static void parseArgs(String[] args)
    {
        try
        {
            // Default mode is text mode.
            System.setProperty(INTERFACE_MODE, TEXT_MODE);
            int i = 0;
            while (i < args.length)
            {
                if (args[i].equals("-mode"))
                {
                    if(args[i+1].equals("gui"))
                        System.setProperty(INTERFACE_MODE, GRAPHICAL_MODE);
                    else if (args[i+1].equals("text"))
                        System.setProperty(INTERFACE_MODE, TEXT_MODE);
                    else
                        throw new Exception("Invalid mode: " + args[i+1]);
                    i+=2;
                } else if (args[i].equals("-logfile")) {
                    // tell the tck where to log results.
                    String logFileName = args[i+1];
                    System.setProperty(test.tck.TestHarness.LOG_FILE_NAME,
                logFileName );
                    i +=2;
                } else if (args[i].equals("-path")) {
                    System.setProperty
            (test.tck.TestHarness.IMPLEMENTATION_PATH, args[i + 1]);
                    i+=2;
                }
                else if (args[i].equals("-abortonfail"))
                {
                    System.setProperty
            (test.tck.TestHarness.ABORT_ON_FAIL, "true");
                    i++;
                } else if (args[i].equals("-help")) {
           printUsage();
           System.exit(0);
        }
                else
                    throw new Exception("Invalid option: " + args[i]);
            }
        }catch(Throwable exc)
        {
            System.out.println(exc.getMessage());
            printUsage();
            System.exit(0);
        }
    }

    public static void main(String[] args)
    {
        parseArgs(args);

        String mode = System.getProperty(INTERFACE_MODE);
        if(mode == null || mode.trim().length() == 0)
            printUsage();
        else if (mode.equals(GRAPHICAL_MODE))
            test.tck.gui.TckFrame.main(new String[]{});
        else if (mode.equals(TEXT_MODE))
            test.tck.TckTestSuite.main(new String[]{});
        else
            printUsage();

    }

}
