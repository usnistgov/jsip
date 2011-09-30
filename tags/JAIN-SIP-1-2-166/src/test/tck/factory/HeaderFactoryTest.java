package test.tck.factory;

// import gov.nist.javax.sip.header.*;
import java.lang.reflect.*;

import javax.sip.address.SipURI;
import javax.sip.address.TelURL;
import javax.sip.address.URI;
import javax.sip.header.*;

import org.apache.log4j.Logger;
// import gov.nist.core.*;
import java.util.*;
import java.text.*;

import test.tck.*;

/**
 * Generate header test cases based on RI. This assumes the correctness
 * of the TCK and generates a set of tests for a reference implementation
 * based on the TCK. It does this by introspection.
 */

public class HeaderFactoryTest extends FactoryTestHarness {

    private static Logger logger = Logger.getLogger(HeaderFactoryTest.class);

    // Header definitions for valid headers.

    /*
     * JvB: This is using gov.nist specific classes that are not in the RI
     * and is not being used at the moment
     *
     *
    private static void testParametersHeader(
        Header referenceHeader,
        Header headerToTest) {
        Iterator it = ((ParametersHeader) referenceHeader).getParameterNames();
        //logger.info(referenceHeader.getClass());
        while (it.hasNext()) {
            String name = (String) it.next();
            String value = ((Parameters) headerToTest).getParameter(name);
            String referenceValue =
                ((Parameters) referenceHeader).getParameter(name);
            NameValue nv =
                ((ParametersHeader) referenceHeader).getNameValue(name);
            Object val = nv.getValue();
            if (val != null && val.getClass().equals(String.class)) {
                if (nv.isValueQuoted()) {
                    assertTrue(value.equals(referenceValue));
                } else {
                    assertTrue(value.equalsIgnoreCase((String) referenceValue));
                }
            } else if (val != null) {
                assertTrue(value.equals(referenceValue));
            } else {
                assertTrue(value == null);
            }
        }
    }
    */

    private void testGetMethods(Header refHeader, Header headerToTest)
        throws IllegalAccessException, InvocationTargetException {
        try {
            Class implementationClass;
            String name = refHeader.getName();

            implementationClass = refHeader.getClass();

            Class[] implementedInterfaces = implementationClass.getInterfaces();
            int j = 0;
            for (j = 0; j < implementedInterfaces.length; j++) {
                if (Header.class.isAssignableFrom(implementedInterfaces[j]))
                    break;
            }
            if (j == implementedInterfaces.length) {
                logger.fatal(
                    "Hmm... could not find it" + refHeader.getClass());
                throw new TckInternalError("Header not implemented");
            }

            String jainClassName = implementedInterfaces[j].getName();

            checkImplementsInterface(
                headerToTest.getClass(),
                implementedInterfaces[j]);

            // Test the get methods of the interface.
            Method methods[] = implementedInterfaces[j].getDeclaredMethods();
            for (int i = 0; i < methods.length; i++) {
                String methodName = methods[i].getName();
                if ((!methodName.startsWith("get"))
                    || methodName.equals("getParameter"))
                    continue;
                Class returnType = methods[i].getReturnType();
                Object refType = null;
                try {
                    refType = methods[i].invoke(refHeader, (Object[]) null);
                } catch (InvocationTargetException ex1 ) {
                    ex1.getCause().printStackTrace();
                    throw new TckInternalError("Invocation failure " +methodName);
                }
                String ftype = returnType.toString();
                if (returnType.isPrimitive()) {
                    Object testValue = methods[i].invoke(headerToTest, (Object[])null);
                    assertTrue(testValue.equals(refType));
                } else {
                    // Non primitive.
                    Object testValue = methods[i].invoke(headerToTest, (Object[]) null);
                    if (refType != null) {

                        if (refType instanceof Calendar) {
                            // TODO: compare how? equals fails
                        } else if (refType instanceof Iterator) {
                            // TODO
                        } else {
                            assertEquals( "Method:" + methods[i], refType, testValue );
                        }
                    } else {
                        assertNull( testValue );
                    }
                }
            }
            if (refHeader instanceof Parameters) {
                Parameters p1 = (Parameters) refHeader;
                Parameters p2 = (Parameters) headerToTest;

                for ( Iterator it = ((Parameters) refHeader).getParameterNames(); it.hasNext(); ) {
                    String pname = (String) it.next();

                    // too strict: equalsIgnoreCase is better
                    assertEquals( p1.getParameter(pname), p2.getParameter(pname) );
                }
            }

            /*
            if (!refHeader.toString().trim().replaceAll( "[\\n\\t\\r ]", "" ).equalsIgnoreCase( headerToTest.toString().trim().replaceAll( "[\\n\\t\\r ]", "" ))) {
                System.err.println( "\n\n\n##### Difference #####\n" + refHeader + "!=" + headerToTest );
            }
            */

        } catch (InvocationTargetException ex ) {
            ex.getCause().printStackTrace();
            throw new TiUnexpectedError(ex.getMessage());
        } catch (Exception ex) {

            ex.printStackTrace();
            throw new TiUnexpectedError(ex.getMessage());
        } finally {
            logTestCompleted("testGetMethods(refHeader,headerToTest)");
        }

    }

    public void testHeaderFactory() {

        try {
            for (int i = 0; i < headers.length; i++) {
                StringBuffer value = new StringBuffer(headers[i]);
                StringBuffer name = new StringBuffer();
                StringBuffer body = new StringBuffer();
                int j = 0;
                for (j = 0; j < value.length(); j++) {

                    if (value.charAt(j) != ':') {
                        name.append(value.charAt(j));
                    } else {
                        break;
                    }
                }

                j++;

                for (; j < value.length(); j++) {
                    body.append(value.charAt(j));
                }

                Header riHeader = null;
                Header tiHeader = null;
                try {

                    riHeader =
                        riHeaderFactory.createHeader(
                            name.toString().trim(),
                            body.toString().trim());

                } catch (ParseException ex) {
                    ex.printStackTrace();
                    throw new TckInternalError(ex.getMessage());
                }

                Header headerToTest =
                    tiHeaderFactory.createHeader(
                        name.toString().trim(),
                        body.toString().trim());
                assertTrue(headerToTest != null);

                logger.info( "Testing header: " + name + " = " + body );

                testGetMethods(riHeader, headerToTest);
            }
            for (int i = 0; i < multiHeaders.length; i++) {
                StringBuffer value = new StringBuffer(multiHeaders[i]);
                List riHeaders = null;
                List tiHeaders = null;
                try {

                    riHeaders = riHeaderFactory.createHeaders(multiHeaders[i]);

                } catch (ParseException ex) {
                    throw new TckInternalError(ex.getMessage());
                }

                tiHeaders = tiHeaderFactory.createHeaders(multiHeaders[i]);
                assertTrue(tiHeaders != null);
                assertTrue(tiHeaders.size() == riHeaders.size());
                ListIterator li = riHeaders.listIterator();
                ListIterator li1 = tiHeaders.listIterator();
                while (li.hasNext()) {
                    Header riHeader = (Header) li.next();
                    Header headerToTest = (Header) li1.next();
                    testGetMethods(riHeader, headerToTest);
                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TiUnexpectedError(ex.getMessage());
        } finally {
            logTestCompleted("testHeaderFactory()");
        }

    }

    /**
     * This test checks the Date format, which is quite strict for SIP
     */
    public void testDate() {
        try {
            Calendar date = Calendar.getInstance( TimeZone.getTimeZone("GMT") );
            date.setTimeInMillis( 0 );
            DateHeader dh = tiHeaderFactory.createDateHeader( date );
            assertEquals( "Date: Thu, 01 Jan 1970 00:00:00 GMT", dh.toString().trim() );
        } finally {
            logTestCompleted("testDate()");
        }
    }

    /**
     * This tests that header parameters are properly assigned to the header, not the URI,
     * when there are no angle brackets
     */
    public void testHeaderParams() {
        try {
            Header h = tiHeaderFactory.createHeader( "m", "sip:User1@127.0.0.1:1234;param1" );
            System.err.println( h );
            assertTrue( h instanceof ContactHeader );
            ContactHeader c = (ContactHeader) h;
            URI u = c.getAddress().getURI();
            assertTrue( u.isSipURI() );
            assertNull( "URI must have no params", ((SipURI)u).getParameter("param1") );
            assertNotNull( "Parameter 'param1' must be assigned to the header", c.getParameter("param1") );
        } catch (ParseException e) {
            e.printStackTrace();
            fail( e.getMessage() );
        } finally {
            logTestCompleted("testHeaderParams()");
        }
    }

    /**
     * This tests that header parameters are properly assigned to the header, not the URI,
     * when there are no angle brackets - in particular for 'tag'
     */
    public void testHeaderParams2() {
        try {
            Header h = tiHeaderFactory.createHeader( "From", "sip:07077004201@x.com;tag=gc2zbu" );
            System.err.println( h );
            assertTrue( h instanceof FromHeader );
            FromHeader c = (FromHeader) h;
            URI u = c.getAddress().getURI();
            assertTrue( u.isSipURI() );
            assertFalse( "URI must have no params", ((SipURI)u).getParameterNames().hasNext() );
            assertNotNull( "Parameter 'tag' must be assigned to the header", c.getTag() );
        } catch (ParseException e) {
            e.printStackTrace();
            fail( e.getMessage() );
        } finally {
            logTestCompleted("testHeaderParams2()");
        }
    }
    
    /**
     * This tests that header parameters are properly assigned to the header, not the URI,
     * when there are no angle brackets - in particular for 'tag'
     */
    public void testHeaderParams3() {
        try {
            Header h = tiHeaderFactory.createHeader( "From", "tel:07077004201;tag=gc2zbu" );
            System.err.println( h );
            assertTrue( h instanceof FromHeader );
            FromHeader c = (FromHeader) h;
            URI u = c.getAddress().getURI();
            assertTrue( u instanceof TelURL );
            assertFalse( "URI must have no params", ((TelURL)u).getParameterNames().hasNext() );
            assertNotNull( "Parameter 'tag' must be assigned to the header", c.getTag() );
        } catch (ParseException e) {
            e.printStackTrace();
            fail( e.getMessage() );
        } finally {
            logTestCompleted("testHeaderParams3()");
        }
    }
    
    public HeaderFactoryTest() {
        super("HeaderFactoryTest");
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(HeaderFactoryTest.class);
    }

}
