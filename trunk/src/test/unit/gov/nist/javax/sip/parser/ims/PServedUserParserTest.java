package test.unit.gov.nist.javax.sip.parser.ims;
/*
* Conditions Of Use
*
* This software was developed by employees of the National Institute of
* Standards and Technology (NIST), an agency of the Federal Government.
* Pursuant to title 15 Untied States Code Section 105, works of NIST
* employees are not subject to copyright protection in the United States
* and are considered to be in the public domain.  As a result, a formal
* license is not needed to use the software.
*
* This software is provided by NIST as a service and is expressly
* provided "AS IS."  NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
* OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
* AND DATA ACCURACY.  NIST does not warrant or make any representations
* regarding the use of the software or the results thereof, including but
* not limited to the correctness, accuracy, reliability or usefulness of
* the software.
*
* Permission to use this software is contingent upon your acceptance
* of the terms of this agreement
*
* .
*
*/
import java.text.ParseException;
import javax.sip.address.AddressFactory;
import gov.nist.javax.sip.address.AddressFactoryImpl;
import gov.nist.javax.sip.header.HeaderFactoryImpl;
import gov.nist.javax.sip.header.ims.PServedUserHeader;
import gov.nist.javax.sip.parser.ims.PServedUserParser;
import test.unit.gov.nist.javax.sip.parser.ParserTestCase;

/**
 *
 * @author aayush.bhatnagar
 * Rancore Technologies Pvt Ltd, Mumbai India.
 *
 */
public class PServedUserParserTest extends ParserTestCase{

    @Override
    public void testParser() {

        System.out.println("*****************************************************");
        System.out.println("parsie parsie.....");
        System.out.println("*****************************************************");

        String[] p_serv_user = {"P-Served-User: <sip:aayush@rancore.com>;regstate=reg;sescase=orig\n",
                                "P-Served-User: <sip:aayush@rancore.com>;regstate=unreg\n",
                                "P-Served-User: <sip:aayush@rancore.com>;sescase=term\n",
                                "P-Served-User: <sip:aayush@rancore.com>\n",
                                "P-Served-User: <sip:aayush@rancore.com;transport=UDP>;sescase=term;regstate=unreg\n"};

        super.testParser(PServedUserParser.class, p_serv_user);

        System.out.println("******************************************************");
        System.out.println("From the perspective of the application, lets test the");
        System.out.println("encoding and usage of the P-Served-User header.");
        System.out.println("******************************************************");

        HeaderFactoryImpl himpl = new HeaderFactoryImpl();
        AddressFactory addfact = new AddressFactoryImpl();
        try {
            PServedUserHeader psuh = himpl.createPServedUserHeader(addfact.createAddress(addfact.createSipURI("aayush", "rancore.com")));
            psuh.setSessionCase("orig");
            psuh.setRegistrationState("reg");
            System.out.println("The encoded header is---> "+psuh.toString());
            System.out.println("The sescase is---> "+psuh.getSessionCase());
            System.out.println("The Regs state is--->"+psuh.getRegistrationState());

        } catch (ParseException e) {

            assertTrue(false);
        }

    }

}
