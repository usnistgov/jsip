package test.tck.msgflow;

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

public class MessageFlowTestSuite
    extends TestSuite
{

    public static void main(String[] args)
    {
         junit.swingui.TestRunner.run(MessageFlowTestSuite.class);
    }

    public MessageFlowTestSuite(String name)
    {
        super(name);
        addTestSuite(test.tck.msgflow.SipProviderTest.class);
        addTestSuite(test.tck.msgflow.ClientTransactionTest.class);
        addTestSuite(test.tck.msgflow.ServerTransactionTest.class);
        addTestSuite(test.tck.msgflow.DialogTest.class);

        addTestSuite(test.tck.msgflow.InviteClientTransactionsStateMachineTest.class);
        addTestSuite(test.tck.msgflow.NonInviteClientTransactionsStateMachineTest.class);
        addTestSuite(test.tck.msgflow.InviteServerTransactionsStateMachineTest.class);
        addTestSuite(test.tck.msgflow.NonInviteServerTransactionsStateMachineTest.class);

        addTestSuite(test.tck.msgflow.DialogStateMachineTest.class);
    }

    public static Test suite()
    {
        return new MessageFlowTestSuite("MessageFlowTestSuite");
    }

}
