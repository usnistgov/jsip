package test.tck.factory;

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
 * @author Emil Ivov
 *         Network Research Team, Louis Pasteur University, Strasbourg, France
 * This  code is in the public domain.
 * @version 1.0
 */

public class FactoryTestSuite
    extends TestSuite
{

    public static void main(String[] args)
    {
         junit.swingui.TestRunner.run(FactoryTestSuite.class);
    }

    public FactoryTestSuite(String name)
    {
        super(name);
        addTestSuite(AddressFactoryTest.class);
        addTestSuite(HeaderFactoryTest.class);
        addTestSuite(MessageFactoryTest.class);
    }

    public static Test suite()
    {
        return new FactoryTestSuite("FactoryTestSuite");
    }

}
