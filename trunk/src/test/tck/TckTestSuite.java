package test.tck;

import junit.framework.*;

/**
 * <p>Title: TCK</p>
 * <p>Description: JAIN SIP 1.1 Technology Compatibility Kit</p>
 *
 * This class represents a test suite that contains all TCK tests.
 *
 * @company NIST
 * @author Emil Ivov
 * This  code is in the public domain.
 * @version 1.0
 */

public class TckTestSuite
    extends TestSuite
{

    public static void main(String[] args)
    {
         junit.textui.TestRunner.run(new test.tck.TckTestSuite("TckTestSuite"));
	 System.exit(0);
    }

    public TckTestSuite(String name)
    {
        super(name);
        addTest(test.tck.factory.FactoryTestSuite.suite());
        addTest(test.tck.msgflow.MessageFlowTestSuite.suite());
    }

    public static Test suite()
    {
        return new TckTestSuite("TckTestSuite");
    }

}
