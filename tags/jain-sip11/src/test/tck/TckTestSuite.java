package test.tck;

import junit.framework.*;

import javax.sip.*;
import javax.sip.address.*;
import javax.sip.message.*;
import javax.sip.header.*;
import java.util.Properties;
import java.util.List;
import java.util.*;
import java.text.*;


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
        addTest(new test.tck.factory.FactoryTestSuite("FactoryTestSuite").suite());
        addTest(new test.tck.msgflow.MessageFlowTestSuite("MessageFlowTestSuite").suite());
    }

    public static Test suite()
    {
        return new TckTestSuite("TckTestSuite");
    }

}
