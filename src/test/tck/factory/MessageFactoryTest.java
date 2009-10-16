package test.tck.factory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.TreeSet;

import javax.sip.address.Address;
import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContentLengthHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.ExtensionHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Message;
import javax.sip.message.Request;
import javax.sip.message.Response;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.tck.TckInternalError;
import test.tck.TiUnexpectedError;

public class MessageFactoryTest extends FactoryTestHarness {

    protected String method;

    protected javax.sip.address.URI tiRequestURI;
    protected CallIdHeader ticallId;
    protected FromHeader tifrom;
    protected ToHeader tito;
    protected MaxForwardsHeader timaxForwards;
    protected ContentTypeHeader ticontentType;
    protected List tivia;
    protected CSeqHeader ticSeq;
    protected ExtensionHeader tiextensionHeader;

    protected javax.sip.address.URI riRequestURI;
    protected CallIdHeader ricallId;
    protected FromHeader rifrom;
    protected ToHeader rito;
    protected MaxForwardsHeader rimaxForwards;
    protected ContentTypeHeader ricontentType;
    protected List rivia;
    protected CSeqHeader ricSeq;
    protected ExtensionHeader riextensionHeader;

    protected byte[] contentBytes;
    protected Object contentObject;
    protected int statusCode;

    public MessageFactoryTest() {
        super("MessageFactoryTest");
    }


    private void cloneTest(Request request) {
        try {
            Request newRequest = (Request) request.clone();
            assertEquals(newRequest,request);
            newRequest.setMethod("FOOBAR");
            assertFalse(newRequest.equals(request));
            newRequest = (Request) request.clone();
            // Add the extension header.
            Header extensionHeader = tiHeaderFactory.createHeader("My-Header",
                    "my header value");
            request.addHeader(extensionHeader);
            assertFalse( newRequest.equals(request) );
            request.removeHeader("My-Header");
            assertEquals(newRequest, request);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
        } finally {
            logTestCompleted("cloneTest(request)");
        }
    }

   private void cloneTest(Response response) {
        try {
            Response newResponse = (Response) response.clone();
            assertEquals(newResponse, response);
            assertNotSame(newResponse, response);
            newResponse = (Response) response.clone();
            // Add the extension header.
            Header extensionHeader = tiHeaderFactory.createHeader("My-Header",
                    "my header value");
            newResponse.addHeader(extensionHeader);
            assertFalse(newResponse.equals(response));
            newResponse.removeHeader("My-Header");
            assertEquals(newResponse, response);
        } catch (ParseException ex) {
            fail(ex.getMessage());
        } finally {
            logTestCompleted("cloneTest(response)");
        }
    }

   /**
    * Tests whether the TI is compliant with the RI for a created message
    * 'Compliant' means that the TI message must at least contain all headers
    * that the RI message contains, and each such header must be equal
    * (according to the RI definition of equals())
    *
    * Note that the ~order~ in which the headers appear is arbitrary, and that the
    * TI may contain additional headers (e.g. User-Agent)
    *
    * @author JvB
    *
    * @param tiMsg
    * @param riMsg
    */
   private void testAgainstRIMsg( Message tiMsg, Message riMsg ) {

       // Test equality using RI definition of equals
       assertEquals( riMsg.getHeader( FromHeader.NAME ), tiMsg.getHeader( FromHeader.NAME ) );
       assertEquals( riMsg.getHeader( ToHeader.NAME ), tiMsg.getHeader( ToHeader.NAME ) );
       assertEquals( riMsg.getHeader( ViaHeader.NAME ), tiMsg.getHeader( ViaHeader.NAME ) );
       assertEquals( riMsg.getHeader( CallIdHeader.NAME ), tiMsg.getHeader( CallIdHeader.NAME ) );
       assertEquals( riMsg.getHeader( CSeqHeader.NAME ), tiMsg.getHeader( CSeqHeader.NAME ) );
   }

   private void testAgainstRIMsgContent( Message tiMsg, Message riMsg ) {

       // Test equality using RI definition of equals
       assertEquals( riMsg.getHeader( ContentLengthHeader.NAME ), tiMsg.getHeader( ContentLengthHeader.NAME ) );
       assertEquals( riMsg.getHeader( ContentTypeHeader.NAME ), tiMsg.getHeader( ContentTypeHeader.NAME ) );
       assertEquals( riMsg.getContent(), tiMsg.getContent() );
   }

   private void testAgainstRI(Response tiResponse, Response riResponse) {
        try {
            assertEquals( riResponse.getStatusCode(), tiResponse.getStatusCode() );
            testAgainstRIMsg( tiResponse, riResponse );
        } catch (Throwable ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
        } finally {
            logTestCompleted("testAgainstRI(testResponse)");
        }
    }

   private void testAgainstRI(Request tiRequest, Request riRequest) {
        try {
            assertEquals(riRequest.getMethod(), tiRequest.getMethod());
            assertEquals(riRequest.getRequestURI(), tiRequest.getRequestURI() );
            assertEquals(riRequest.getHeader( MaxForwardsHeader.NAME ), tiRequest.getHeader( MaxForwardsHeader.NAME ) );
            testAgainstRIMsg(tiRequest, riRequest);
        } catch (Throwable ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
        } finally {
            logTestCompleted("testAgainstRI(testRequest)");
        }
    }

    public void testCreateRequest() {
    try {
        Request tiRequest = tiMessageFactory.createRequest
           (tiRequestURI, method, ticallId,
            ticSeq,tifrom,tito,tivia,timaxForwards,
                    ticontentType,
            contentObject);
        cloneTest(tiRequest);

        Request riRequest = riMessageFactory.createRequest
           (riRequestURI, method, ricallId,
            ricSeq,rifrom,rito,rivia,rimaxForwards,
                 ricontentType,
            contentObject);

        testAgainstRI( tiRequest, riRequest );
        testAgainstRIMsgContent( tiRequest, riRequest );

        tiRequest = tiMessageFactory.createRequest
           (tiRequestURI, method, ticallId,ticSeq,tifrom,tito,tivia,timaxForwards,
                   ticontentType, contentBytes);
        cloneTest(tiRequest);
        riRequest = riMessageFactory.createRequest
           (riRequestURI, method, ricallId,ricSeq,rifrom,rito,rivia,rimaxForwards,
                ricontentType, contentBytes);
        cloneTest(tiRequest);
        testAgainstRI(tiRequest,riRequest);
        testAgainstRIMsgContent( tiRequest, riRequest );


        Response tiResponse = tiMessageFactory.createResponse(statusCode,
                    ticallId, ticSeq, tifrom, tito, tivia, timaxForwards, ticontentType,
                    contentObject);

        cloneTest(tiResponse);
        Response riResponse = riMessageFactory.createResponse(statusCode,
                ricallId, ricSeq, rifrom, rito, rivia, rimaxForwards, ricontentType,
                contentObject);
        testAgainstRI(tiResponse,riResponse);
        testAgainstRIMsgContent( tiResponse, riResponse );

         tiResponse = tiMessageFactory.createResponse(statusCode, ticallId,
                    ticSeq, tifrom, tito, tivia, timaxForwards, ticontentType,
                    contentObject);
        cloneTest(tiResponse);
         riResponse = riMessageFactory.createResponse(statusCode, ricallId,
                ricSeq, rifrom, rito, rivia, rimaxForwards, ricontentType,
                contentObject);
        testAgainstRI(tiResponse,riResponse);
        testAgainstRIMsgContent( tiResponse, riResponse );

        tiResponse = tiMessageFactory.createResponse(statusCode, ticallId,
                    ticSeq, tifrom, tito, tivia, timaxForwards);
        cloneTest(tiResponse);
        riResponse = riMessageFactory.createResponse(statusCode, ricallId,
                ricSeq, rifrom, rito, rivia, rimaxForwards);

        testAgainstRI(tiResponse,riResponse);

        tiResponse = tiMessageFactory.createResponse(statusCode,  tiRequest,
                     ticontentType,  contentBytes) ;
        cloneTest(tiResponse);
        riResponse = riMessageFactory.createResponse(statusCode,  riRequest,
                 ricontentType,  contentBytes) ;
        testAgainstRI(tiResponse,riResponse);
        testAgainstRIMsgContent( tiResponse, riResponse );

        tiResponse = tiMessageFactory.createResponse(statusCode, tiRequest,
                 ticontentType,  contentObject) ;
        cloneTest(tiResponse);
        riResponse = riMessageFactory.createResponse(statusCode, riRequest,
                 ricontentType,  contentObject) ;
        testAgainstRI(tiResponse,riResponse);
        testAgainstRIMsgContent( tiResponse, riResponse );

        tiResponse = tiMessageFactory.createResponse(statusCode,  tiRequest) ;
        cloneTest(tiResponse);
        riResponse = riMessageFactory.createResponse(statusCode,  riRequest) ;
        testAgainstRI(tiResponse,riResponse);
    } catch (Throwable ex) {
        ex.printStackTrace();
        fail( ex.getMessage() );
    } finally {
        logTestCompleted("testCreateRequest()");
    }

    }

    public void testCreateRequest1() {
        try {
            Request request = tiMessageFactory.createRequest(tiRequestURI,
                    method, ticallId, ticSeq, tifrom, tito, tivia, timaxForwards);
            String msgString = request.toString();
            assertEquals(request, tiMessageFactory.createRequest(msgString));
        } catch (Exception ex) {
            ex.printStackTrace();
            fail( ex.getMessage() );
        } finally {
            logTestCompleted("testCreateRequest1()");
        }
    }

    private void testGetMethods(Request refRequest, Request requestToTest)
    throws IllegalAccessException,
        InvocationTargetException {
    try {
        Class implementationClass;

        implementationClass = refRequest.getClass();

        Class[] implementedInterfaces = implementationClass.getInterfaces();
        int j = 0;

        // Do a TCK consistency check.
        for ( j = 0; j < implementedInterfaces.length; j++) {
            if ( javax.sip.message.Request.class.isAssignableFrom
            (implementedInterfaces[j]))
                break;
        }
        if (j == implementedInterfaces.length) {
            System.out.println("Hmm... could not find it" +
            refRequest.getClass());
        throw new TckInternalError("Request not implemented");
        }

        String jainClassName = implementedInterfaces[j].getName();

        // Make sure that all the methods of the interface are implemented
        checkImplementsInterface(requestToTest.getClass(),
        Request.class);

        // Test the get methods of the interface.
        Method methods[] = implementedInterfaces[j].getDeclaredMethods();
        for (int i = 0; i < methods.length;  i++) {
            String methodName = methods[i].getName();
            if  (! methodName.startsWith("get") ) continue;
            // Testing only null argument gets
            if ( methods[i].getParameterTypes().length != 0) continue;
            Class returnType = methods[i].getReturnType();
            Object refType = null;
            try {
                refType = methods[i].invoke(refRequest,(Object[])null);
            } catch (Exception ex) {
                throw new TckInternalError("Invocation failure " + methodName);
            }
            String ftype = returnType.toString();
            if (returnType.isPrimitive()) {
                Object testValue = methods[i].invoke( requestToTest, (Object[])null);
                assertEquals( refType, testValue );
            } else {
                // Non primitive.
                Object testValue = methods[i].invoke( requestToTest,(Object[])null);
                if (refType != null) {
                    assertTrue(testValue != null);
                    // Find the jain type implemented by the interface.
                    Class fclass = refType.getClass();
                    Class[] fInterfaces = fclass.getInterfaces();
                    // Find what JAIN interface this is implementing.
                    int k = 0;
                    for ( k = 0; k < fInterfaces.length; k++) {
                        if ( javax.sip.header.Header.class.isAssignableFrom(fInterfaces[k]))
                            break;
                    }
                    // If this implements a header interface
                    // check that the same header is returned in both
                    // ti and ri
                    if ( k < fInterfaces.length) {
                        // Make sure that the return type matches.
                        assertTrue(fInterfaces[k].isAssignableFrom(testValue.getClass()));
                        String refhdrString = refType.toString();
                        String testhdrString = testValue.toString();
                        // Use the factory to test for equivalence
                        Header riHeader = createRiHeaderFromString(refhdrString);
                        if (riHeader == null)
                            throw new TckInternalError( "could not parse "  + refhdrString );

                        // Create a RI header from the string to test
                        // for equivalence. Note that we cannot compare
                        // ti header to RI header otherwise.
                        Header tiHeader = createRiHeaderFromString(testhdrString);
                        assertNotNull(tiHeader);
                        assertEquals("Retrieved header did not match RI",riHeader,tiHeader);
                    }
                } else if (refType == null) {
                    assertTrue(testValue == null);
                }
            }

        }
    } catch (Exception ex) {
        throw new TiUnexpectedError(ex.getMessage());
    }

    }

    public void testMessageGetMethods() {
        try {
            Request tiRequest = tiMessageFactory.createRequest(msgString);
            Request riRequest = riMessageFactory.createRequest(msgString);

            testGetMethods(riRequest,tiRequest);

        } catch (Exception ex) {
            assertTrue(false);
        } finally {
            logTestCompleted("testMessageGetMethods()");
        }
    }


    public void testGetHeaderNames() {
        // BUG report by Ben Evans (opencloud)
        //  This test assumed implementations will store header names
        // using mixed case, eg. "Content-Type" not "content-type".
        // Header names are not case sensitive in RFC3261 so this is not
        // a real requirement.
        try {
            Request request = tiMessageFactory.createRequest
            (tiRequestURI, method, ticallId,
                    ticSeq,tifrom,tito,tivia,timaxForwards,
                    ticontentType,
                    contentObject);
            ListIterator li = request.getHeaderNames();
            // Use a tree set with case insensitive ordering & comparison
            TreeSet set = new TreeSet(String.CASE_INSENSITIVE_ORDER);
            while (li.hasNext()) {
                String hdrName = (String) li.next();
                set.add(hdrName);
            }
            assertTrue (  set.contains(CSeqHeader.NAME) ) ;
            assertTrue (  set.contains(FromHeader.NAME) ) ;
            assertTrue (  set.contains(ToHeader.NAME)   ) ;
            assertTrue ( set.contains(ViaHeader.NAME)   );
            assertTrue (  set.contains(MaxForwardsHeader.NAME) ) ;
            assertTrue ( set.contains(ContentTypeHeader.NAME) ) ;
            assertTrue (  set.contains(CallIdHeader.NAME) ) ;
            Response response = tiMessageFactory.createResponse
            (   statusCode,  ticallId,
                ticSeq,  tifrom,  tito,  tivia,
                timaxForwards,  ticontentType, contentObject);
            li = request.getHeaderNames();
            set.clear();
            while (li.hasNext()) {
                String hdrName = (String)li.next();
                set.add(hdrName);
            }

            //
            // JvB: Headers may use short names too
            //
            assertTrue (  set.contains(CSeqHeader.NAME) ) ;
            assertTrue (  set.contains(FromHeader.NAME) || set.contains("f") ) ;
            assertTrue (  set.contains(ToHeader.NAME) || set.contains("t")  ) ;
            assertTrue (  set.contains(ViaHeader.NAME) || set.contains("v") ) ;
            assertTrue (  set.contains(MaxForwardsHeader.NAME) ) ;
            assertTrue (  set.contains(ContentTypeHeader.NAME) || set.contains("c") ) ;
            assertTrue (  set.contains(CallIdHeader.NAME) || set.contains("i") ) ;

        } catch (Exception ex) {
            ex.printStackTrace();
            fail( ex.getMessage() );
        } finally {
            logTestCompleted("testGetHeaderNames()");
        }

    }


    public void testAddRemoveHeader() {
        try{
            Request request = tiMessageFactory.createRequest
            (tiRequestURI, method, ticallId, ticSeq,tifrom,tito,tivia,timaxForwards,
                    ticontentType, contentObject);
            request.addHeader(tiextensionHeader);
            ListIterator  viaHeaders = request.getHeaders(ViaHeader.NAME);
            ViaHeader viaHeader = (ViaHeader) viaHeaders.next();
            assertTrue ( viaHeader.equals(tivia.iterator().next())) ;
            Header header  = request.getHeader("My-Header");
            assertTrue (header != null && header.equals(tiextensionHeader));
            request.removeHeader("My-Header");
            assertNull( request.getHeader("My-Header") ) ;
            Response response = tiMessageFactory.createResponse
            (statusCode,  ticallId, ticSeq,  tifrom,  tito,  tivia,
                    timaxForwards,  ticontentType, contentObject);
            response.addHeader(tiextensionHeader);
            viaHeaders = request.getHeaders(ViaHeader.NAME);
            viaHeader = (ViaHeader)viaHeaders.next();
            assertEquals( viaHeader, tivia.iterator().next() ) ;
            header  = response.getHeader("My-Header");
            assertEquals(header, tiextensionHeader);
            response.removeHeader("My-Header");
            assertNull(response.getHeader("My-Header")) ;
            ContentLengthHeader cl = response.getContentLength();
            assertNotNull(cl) ;
        } catch (Exception ex) {
            ex.printStackTrace();
            fail( ex.getMessage() );
        } finally {
            logTestCompleted("testAddRemoveHeader()");
        }
    }

 
	public void testCharset() {
	try{
		Request request = tiMessageFactory.createRequest( 
		  "MESSAGE sip:127.0.0.1 SIP/2.0\r\n"+
		  "Via: SIP/2.0/TCP 127.0.0.1:5060;rport=5060;branch=z9hG4bKd2c87858eb0a7a09becc7a115c608d27\r\n"+
		  "CSeq: 2 BYE\r\n"+
		  "Call-ID: 84a5c57fd263bcce6fec05edf20c5aba@127.0.0.1\r\n"+
		  "From: \"The Master Blaster\" <sip:BigGuy@here.com>;tag=12345\r\n"+
		  "To: \"The Little Blister\" <sip:LittleGuy@there.com>;tag=2955\r\n"+
		  "Max-Forwards: 70\r\n"+
		  "Content-Type: text/plain;charset=ISO-8859-1\r\n" +
		  "Content-Length: 0\r\n"+
		  "\r\n"
		);

		// JvB: in UTF-8 these character would be encoded as multiple bytes
		byte[] content = "öê".getBytes( "ISO-8859-1" );
		request.setContent( new String(content,"ISO-8859-1"), 
				(ContentTypeHeader) request.getHeader(ContentTypeHeader.NAME) );
		
		assertEquals( 2, request.getRawContent().length );		
	} catch (Exception t) {
	  t.printStackTrace();
	  fail( "ParseException", t );
	} finally {
		logTestCompleted("testCharset()");
	}
}

    public void setUp() {
        try {
            String fromName = "BigGuy";
            String fromSipAddress = "here.com";
            String fromDisplayName = "The Master Blaster";

            String toSipAddress = "there.com";
            String toUser = "LittleGuy";
            String toDisplayName = "The Little Blister";

            // create >From Header
            SipURI tifromAddress = tiAddressFactory.createSipURI(fromName,fromSipAddress);
            SipURI rifromAddress = riAddressFactory.createSipURI(fromName,fromSipAddress);

            Address tifromNameAddress = tiAddressFactory.createAddress(tifromAddress);
            Address rifromNameAddress = riAddressFactory.createAddress(rifromAddress);

            tifromNameAddress.setDisplayName(fromDisplayName);
            rifromNameAddress.setDisplayName(fromDisplayName);

            tifrom = tiHeaderFactory.createFromHeader(tifromNameAddress, "12345");
            rifrom = riHeaderFactory.createFromHeader(rifromNameAddress, "12345");

            // create To Header
            SipURI titoAddress = tiAddressFactory.createSipURI(toUser,toSipAddress);
            SipURI ritoAddress = riAddressFactory.createSipURI(toUser,toSipAddress);

            Address titoNameAddress = tiAddressFactory.createAddress(titoAddress);
            Address ritoNameAddress = riAddressFactory.createAddress(ritoAddress);

            titoNameAddress.setDisplayName(toDisplayName);
            ritoNameAddress.setDisplayName(toDisplayName);

            tito = tiHeaderFactory.createToHeader(titoNameAddress, null);
            rito = riHeaderFactory.createToHeader(ritoNameAddress, null);

            // create Request URIs
            tiRequestURI = tiAddressFactory.createSipURI(toUser, toSipAddress);
            riRequestURI = riAddressFactory.createSipURI(toUser, toSipAddress);

            timaxForwards = tiHeaderFactory.createMaxForwardsHeader(70);
            rimaxForwards = riHeaderFactory.createMaxForwardsHeader(70);

            ticontentType = tiHeaderFactory.createContentTypeHeader("application", "sdp");
            ricontentType = riHeaderFactory.createContentTypeHeader("application", "sdp");

            tivia = new LinkedList();
            rivia = new LinkedList();

            ViaHeader tiviaHeader = tiHeaderFactory.createViaHeader("127.0.0.1",5060, "udp", null);
            tivia.add(tiviaHeader);

            ViaHeader riviaHeader = riHeaderFactory.createViaHeader("127.0.0.1",5060, "udp", null);
            rivia.add(riviaHeader);

            ticallId = tiHeaderFactory.createCallIdHeader("12345@127.0.0.1");
            ricallId = riHeaderFactory.createCallIdHeader("12345@127.0.0.1");

            contentObject = "v=0\r\n"
                    + "o=4855 13760799956958020 13760799956958020"
                    + " IN IP4  129.6.55.78\r\n" + "s=mysession session\r\n"
                    + "p=+46 8 52018010\r\n" + "c=IN IP4  129.6.55.78\r\n"
                    + "t=0 0\r\n" + "m=audio 6022 RTP/AVP 0 4 18\r\n"
                    + "a=rtpmap:0 PCMU/8000\r\n" + "a=rtpmap:4 G723/8000\r\n"
                    + "a=rtpmap:18 G729A/8000\r\n" + "a=ptime:20\r\n";
            contentBytes = ((String) contentObject).getBytes();

            method = Request.INVITE;

            ticSeq = tiHeaderFactory.createCSeqHeader(1L, method);
            ricSeq = riHeaderFactory.createCSeqHeader(1L, method);

            statusCode = 200;

            tiextensionHeader = (ExtensionHeader) tiHeaderFactory.createHeader(
                    "My-Header", "My Header Value");

            riextensionHeader = (ExtensionHeader) riHeaderFactory.createHeader(
                    "My-Header", "My Header Value");

        } catch (Throwable ex) {
            ex.printStackTrace();
            fail( ex.getMessage() );
        } // finally { assertTrue(true); }

    }

    public void tearDown() {
        riFactory.resetFactory();
        tiFactory.resetFactory();
    }

    public static Test suite()  {
    return new TestSuite(MessageFactoryTest.class);
    }


    public static void main(String[] args) {
    junit.textui.TestRunner.run(MessageFactoryTest.class);
    }

}

