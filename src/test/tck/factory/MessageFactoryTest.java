package test.tck.factory;

import java.text.ParseException;
import javax.sip.header.*;
import java.util.*;
import gov.nist.javax.sip.header.*;
import javax.sip.message.*;
import javax.sip.address.*;
import gov.nist.javax.sip.parser.*;
import junit.framework.*;
import test.tck.*;
import java.lang.reflect.*;

public class MessageFactoryTest extends FactoryTestHarness {
    
    protected javax.sip.address.URI requestURI;
    protected String method;
    protected CallIdHeader callId;
    protected FromHeader from;
    protected ToHeader to;
    protected MaxForwardsHeader maxForwards;
    protected ContentTypeHeader contentType;
    protected List via;
    protected byte[] contentBytes;
    protected Object contentObject;
    protected CSeqHeader cSeq;
    protected int statusCode;
    protected ExtensionHeader extensionHeader;
    
    public MessageFactoryTest() {
        super("MessageFactoryTest");
    }


    private void cloneTest( Request request) {
	try {
	    Request newRequest = (Request) request.clone();
	    assertTrue( newRequest.equals(request ) );
	    newRequest.setMethod("FOOBAR");
	    assertTrue( ! newRequest.equals(request));
	    newRequest = (Request)request.clone();
            // Add the extension header.
            Header extensionHeader = tiHeaderFactory.createHeader
            	("My-Header","my header value");
            request.addHeader(extensionHeader);
	    assertTrue( !newRequest.equals(request)) ;
            request.removeHeader("My-Header");
	    assertTrue( newRequest.equals(request)) ;
        } catch (Exception ex) {
              assertTrue(false);
	} finally {
              logTestCompleted("cloneTest(request)");
	}
   }

   private void cloneTest( Response response) {
	try {
	    Response newResponse = (Response) response.clone();
	    assertTrue ( newResponse.equals(response) ) ;
            assertTrue ( newResponse != response);
	    newResponse = (Response)response.clone();
            // Add the extension header.
            Header extensionHeader =
            tiHeaderFactory.createHeader
            ("My-Header","my header value");
            newResponse.addHeader(extensionHeader);
	    assertTrue(!newResponse.equals(response) ) ;
	    newResponse.removeHeader("My-Header");
	    assertTrue( newResponse.equals(response) );
        } catch (ParseException ex) {
               assertTrue(false);
	} finally {
	       logTestCompleted("cloneTest(response)");
	}
   }

   private void testAgainstRI(Response testResponse) {
	try {
	    StringMsgParser smp = new StringMsgParser();
	    Response response = (Response) 
			smp.parseSIPMessage(testResponse.toString());
	
	    assertTrue( response.getStatusCode() == this.statusCode ) ;
	
	    From   from   = (From)smp.parseSIPHeader
			(this.from.toString().trim());
	    assertTrue ( response.getHeader(FromHeader.NAME).equals(from) ) ;
	
	    To   to   = (To)smp.parseSIPHeader(this.to.toString().trim());
	    assertTrue ( response.getHeader(ToHeader.NAME).equals(to) ) ;

	    Via via = (Via) 
	    ((SIPHeaderList)smp.parseSIPHeader
			(this.via.get(0).toString().trim())).first();
	    Via requestVia = (Via) response.getHeaders(ViaHeader.NAME).next();
	    assertTrue ( requestVia.equals(via) ) ;

	     CSeq cseq = (CSeq)smp.parseSIPHeader(this.cSeq.toString().trim());

	     assertTrue ( response.getHeader(CSeqHeader.NAME).equals(cseq));

	     CallID callId = (CallID) smp.parseSIPHeader
				(this.callId.toString().trim());
	     assertTrue 
		(  response.getHeader(CallIdHeader.NAME).equals(callId));
	} catch (Exception ex) {
		assertTrue(false);
	} finally {
		logTestCompleted("testAgainstRI(testResponse)");
	}

   }

   private void testAgainstRI(Request testRequest)  {
	try {
	    StringMsgParser smp = new StringMsgParser();
	    Request request = (Request) smp.parseSIPMessage
				(testRequest.toString());
	    assertTrue( request.getMethod().equals(method)) ;

	    String uriString = this.requestURI.toString();
	    gov.nist.javax.sip.address.GenericURI requestURI = 
			smp.parseUrl(uriString);

	    assertTrue ( requestURI.equals(request.getRequestURI()) );

	    From   from   = 
			(From)smp.parseSIPHeader(this.from.toString().trim());

	    assertTrue ( request.getHeader(FromHeader.NAME).equals(from) ) ;
	
	    To   to   = (To)smp.parseSIPHeader
		(this.to.toString().trim());
	    assertTrue ( request.getHeader(ToHeader.NAME).equals(to) ) ;

	    Via via = (Via) 
	    	((SIPHeaderList)smp.parseSIPHeader(this.via.get(0).toString().
		trim())).getFirst();
	    Via requestVia = (Via) request.getHeaders(ViaHeader.NAME).next();

	    assertTrue ( requestVia.equals(via) );

	    CSeq cseq = (CSeq)smp.parseSIPHeader(this.cSeq.toString().trim());
	    
	    assertTrue (  request.getHeader(CSeqHeader.NAME).equals(cseq));

	    CallID callId = (CallID) smp.parseSIPHeader
				(this.callId.toString().trim());
	    assertTrue (  request.getHeader(CallIdHeader.NAME).equals(callId));

	} catch (Exception ex) {
	    assertTrue(false);
	} finally {
	    logTestCompleted("testAgainstRI(testRequest)");
	}
    }


    public void testCreateRequest() {
	try {
	    Request request = tiMessageFactory.createRequest
		   (requestURI, method, callId,
		    cSeq,from,to,via,maxForwards,
                    contentType,
		    contentObject);
	    cloneTest(request);
	    testAgainstRI(request);
	    
	    request = tiMessageFactory.createRequest
		   (requestURI, method, callId,cSeq,from,to,via,maxForwards,
                   contentType, contentBytes);
	    cloneTest(request);
	    testAgainstRI(request);
    	    
    	    Response response = tiMessageFactory.createResponse
		(	statusCode,  callId, 
             		cSeq,  from,  to,  via, 
			maxForwards,  contentType, contentObject);

	    cloneTest(response);
	    testAgainstRI(response);

     	    response = 
		tiMessageFactory.createResponse
		(statusCode, callId, 
             	cSeq, from, to, via, maxForwards, contentType, contentObject) ;
	    cloneTest(response);
	    testAgainstRI(response);

            response = tiMessageFactory.createResponse
		(statusCode,  callId, cSeq, from, to, via, maxForwards);
	    cloneTest(response);
	    testAgainstRI(response);

    	 
     	    response = tiMessageFactory.createResponse(statusCode,  request, 
             		contentType,  contentBytes) ;
	    cloneTest(response);
	    testAgainstRI(response);
    
    	    response = tiMessageFactory.createResponse(statusCode, request, 
             	contentType,  contentObject) ;
	    cloneTest(response);
	    testAgainstRI(response);

    	    response = tiMessageFactory.createResponse(statusCode,  request) ;
	    cloneTest(response);
	    testAgainstRI(response);


	} catch (Exception ex) {
		assertTrue (false);
	} finally {
		logTestCompleted("testCreateRequest()");
	}
	
    }

    public void testCreateRequest1() {
        try {
            Request request = tiMessageFactory.createRequest
		   (requestURI, method, callId,
		    cSeq,from,to,via,maxForwards);
            String msgString = request.toString();
            assertEquals(request,tiMessageFactory.createRequest(msgString));
        }catch (Exception ex) {
            assertTrue(false);
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
                refType = methods[i].invoke(refRequest,null);
            } catch (Exception ex) {
		throw new TckInternalError("Invocation failure " + methodName);
            }
            String ftype = returnType.toString();
            if (returnType.isPrimitive()) {
                Object testValue =
                methods[i].invoke( requestToTest,null);
		assertTrue(testValue.equals(refType));
            } else {
                // Non primitive.
                Object testValue = methods[i].invoke( requestToTest,null);
                if (refType != null) {
		    assertTrue(testValue != null);
		    // Find the jain type implemented by the interface.
		     Class fclass = refType.getClass();
                     Class[] fInterfaces = fclass.getInterfaces();
		     // Find what JAIN interface this is implementing.
		     int k = 0;
                     for ( k = 0; k < fInterfaces.length; k++) {
                        if ( javax.sip.header.Header.class.isAssignableFrom
            		(fInterfaces[k]))
                	break;
        	     }
		     // If this implements a header interface
		     // check that the same header is returned in both
		     // ti and ri
		     if ( k < fInterfaces.length) {
			// Make sure that the return type matches.
			assertTrue
                        (fInterfaces[k].isAssignableFrom(testValue.getClass()));
			String refhdrString = refType.toString();
			String testhdrString = testValue.toString();
			// Use the factory to test for equivalence
			Header riHeader = 
				createRiHeaderFromString(refhdrString);
			if (riHeader == null) 
				throw new TckInternalError
				( "could not parse "  + refhdrString );
                        // Create a RI header from the string to test
                        // for equivalence. Note that we cannot compare
                        // ti header to RI header otherwise.
			Header tiHeader = 
				createRiHeaderFromString(testhdrString);
			assertTrue(tiHeader != null);
			assertEquals("Retrieved header did not match RI",
				tiHeader,riHeader);
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
		   (requestURI, method, callId,
		    cSeq,from,to,via,maxForwards,
                    contentType,
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
		(	statusCode,  callId, 
             		cSeq,  from,  to,  via, 
			maxForwards,  contentType, contentObject);
	  li = request.getHeaderNames();
        set.clear();
	  while (li.hasNext()) {
		String hdrName = (String)li.next();
		set.add(hdrName);
	  }
	  assertTrue (  set.contains(CSeqHeader.NAME) ) ;
	  assertTrue (  set.contains(FromHeader.NAME) ) ;
	  assertTrue (  set.contains(ToHeader.NAME)   ) ;
	  assertTrue (  set.contains(ViaHeader.NAME) ) ;
	  assertTrue (  set.contains(MaxForwardsHeader.NAME) ) ;
	  assertTrue (  set.contains(ContentTypeHeader.NAME) ) ;
	  assertTrue (  set.contains(CallIdHeader.NAME) ) ;
	  
	} catch (Exception ex) {
		assertTrue(false);
	} finally {
		logTestCompleted("testGetHeaderNames()");
	}

    }


    public void testAddRemoveHeader() {
        try{
         Request request = tiMessageFactory.createRequest
		   (requestURI, method, callId, cSeq,from,to,via,maxForwards,
                    contentType, contentObject);
	request.addHeader(extensionHeader);
	ListIterator  viaHeaders = request.getHeaders(ViaHeader.NAME);
	ViaHeader viaHeader = (ViaHeader) viaHeaders.next();
	assertTrue ( viaHeader.equals(via.iterator().next())) ;
	Header header  = request.getHeader("My-Header");
	assertTrue (header != null && header.equals(extensionHeader));
	request.removeHeader("My-Header");
	assertTrue (request.getHeader("My-Header") == null) ;
        Response response = tiMessageFactory.createResponse
		(statusCode,  callId, cSeq,  from,  to,  via, 
		maxForwards,  contentType, contentObject);
	response.addHeader(extensionHeader);
	viaHeaders = request.getHeaders(ViaHeader.NAME);
	viaHeader = (ViaHeader)viaHeaders.next();
	assertTrue ( viaHeader.equals(via.iterator().next())) ;
	header  = response.getHeader("My-Header");
	assertTrue (header != null && header.equals(extensionHeader));
	response.removeHeader("My-Header");
	assertTrue (response.getHeader("My-Header") == null) ;
	ContentLengthHeader cl = response.getContentLength();
	assertTrue (cl != null ) ;
        } catch (Exception ex) {
            assertTrue(false);
        } finally {
            logTestCompleted("testAddRemoveHeader()");
        }
    }





    public void setUp() {
	try {
        	String fromName       = "BigGuy";
        	String fromSipAddress = "here.com";
        	String fromDisplayName = "The Master Blaster";
            
        	String toSipAddress   = "there.com";
        	String toUser 	 = "LittleGuy";
        	String toDisplayName  = "The Little Blister";

        	// create >From Header
        	SipURI  fromAddress =
        		tiAddressFactory.createSipURI 
			(fromName, fromSipAddress);

       		Address fromNameAddress =
       			tiAddressFactory.createAddress ( fromAddress);
       		fromNameAddress.setDisplayName(fromDisplayName);
       		from = tiHeaderFactory.createFromHeader
        		(fromNameAddress,"12345");

                // create To Header
            	SipURI toAddress = tiAddressFactory.createSipURI
            	(toUser, toSipAddress);
            	Address toNameAddress = tiAddressFactory.createAddress
            	(toAddress);
            	toNameAddress.setDisplayName(toDisplayName);
             	to = tiHeaderFactory.createToHeader (toNameAddress,null);
                // create Request URI
                requestURI = tiAddressFactory.createSipURI
            	(toUser,toSipAddress);

		maxForwards = tiHeaderFactory.createMaxForwardsHeader(70);

		contentType = tiHeaderFactory.createContentTypeHeader
				("application","sdp");

		via = new LinkedList();
		
		ViaHeader viaHeader  = 
			tiHeaderFactory.createViaHeader("127.0.0.1",5060,"udp",
			null);
		via.add(viaHeader);
		callId =  tiHeaderFactory.createCallIdHeader("12345@127.0.0.1");
	        contentObject = "v=0\r\n" +
            	"o=4855 13760799956958020 13760799956958020"+
            	" IN IP4  129.6.55.78\r\n" +
            	"s=mysession session\r\n" +
            	"p=+46 8 52018010\r\n" +
            	"c=IN IP4  129.6.55.78\r\n" +
            	"t=0 0\r\n" +
            	"m=audio 6022 RTP/AVP 0 4 18\r\n" +
            	"a=rtpmap:0 PCMU/8000\r\n" +
            	"a=rtpmap:4 G723/8000\r\n" +
            	"a=rtpmap:18 G729A/8000\r\n" +
            	"a=ptime:20\r\n";
		contentBytes = ((String)contentObject).getBytes();

		method = Request.INVITE;

		cSeq = tiHeaderFactory.createCSeqHeader(1,method);

		statusCode = 200;
		
		extensionHeader = (ExtensionHeader) 
                    tiHeaderFactory.createHeader("My-Header",
				"My Header Value");
		

	} catch (Exception ex ) {
		ex.printStackTrace();
		assertTrue(false);
	}  // finally { assertTrue(true); }

    }

    public static Test suite()  {
	return new TestSuite(MessageFactoryTest.class);
    }

	
    public static void main(String[] args) {
	junit.textui.TestRunner.run(MessageFactoryTest.class);
    }
	   
}

