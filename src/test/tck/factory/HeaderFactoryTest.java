
package test.tck.factory;
import gov.nist.javax.sip.header.*;
import gov.nist.javax.sip.message.*;
import gov.nist.javax.sip.parser.*;
import java.lang.reflect.*;
import javax.sip.header.*;
import gov.nist.core.*;
import java.util.*;
import java.io.*;
import java.text.*;
import test.tck.*;



/** Generate header test cases based on RI. This assumes the correctness
 * of the TCK and generates a set of tests for a reference implementation
 * based on the TCK. It does this by introspection.
 */

public class HeaderFactoryTest extends FactoryTestHarness  {
    
    // Header definitions for valid headers.
    
    private static void testParametersHeader
    ( Header referenceHeader, Header headerToTest) {
        Iterator it = ((ParametersHeader) referenceHeader).getParameterNames();
        //System.out.println(referenceHeader.getClass());
        while (it.hasNext()) {
            String name = (String) it.next();
            String value = ((Parameters) headerToTest).getParameter(name);
            String referenceValue =
            ((Parameters) referenceHeader).getParameter(name);
            NameValue nv = ((ParametersHeader) referenceHeader).
            getNameValue(name);
            Object val = nv.getValue();
            if (val != null && val.getClass().equals(String.class) ) {
                if (nv.isValueQuoted()) {
                    assertTrue ( value.equals(  referenceValue )) ;
                } else {
                    assertTrue ( value.equalsIgnoreCase
				( (String)referenceValue )); 
                }
            } else if (val != null) {
                assertTrue ( value.equals( referenceValue ) ) ;
            } else {
		assertTrue(value == null);
            }
        }
    }
    
    
    private  void testGetMethods(Header refHeader, Header headerToTest) 
	throws IllegalAccessException, 
        InvocationTargetException {
	try {
        Class implementationClass;
        String name = refHeader.getName();
        
        
        implementationClass = refHeader.getClass();
        
        
        Class[] implementedInterfaces = implementationClass.getInterfaces();
        int j = 0;
        for ( j = 0; j < implementedInterfaces.length; j++) {
            if ( javax.sip.header.Header.class.isAssignableFrom
            (implementedInterfaces[j]))
                break;
        }
        if (j == implementedInterfaces.length) {
            System.out.println("Hmm... could not find it" +
            refHeader.getClass());
	    throw new TckInternalError("Header not implemented");
        }
        
        
        String jainClassName = implementedInterfaces[j].getName();
        
        checkImplementsInterface(headerToTest.getClass(),
        implementedInterfaces[j]);
        
        // Test the get methods of the interface.
        Method methods[] = implementedInterfaces[j].getDeclaredMethods();
        for (int i = 0; i < methods.length;  i++) {
            String methodName = methods[i].getName();
            if ( (! methodName.startsWith("get") ) ||
            methodName.equals("getParameter") ) continue;
            Class returnType = methods[i].getReturnType();
            Object refType = null;
            try {
                refType = methods[i].invoke(refHeader,null);
            } catch (Exception ex) {
		throw new TckInternalError("Invocation failure " + methodName);
            }
            String ftype = returnType.toString();
            if (returnType.isPrimitive()) {
                Object testValue =
                methods[i].invoke( headerToTest,null);
		assertTrue(testValue.equals(refType));
            } else {
                // Non primitive.
                Object testValue =
                methods[i].invoke( headerToTest,null);
                if (refType != null) {
		    assertTrue(testValue != null);
                } else if (refType == null) {
		    assertTrue(testValue == null);
                }
            }
            
        }
	} catch (Exception ex) {
		throw new TiUnexpectedError(ex.getMessage());
	} finally {
		logTestCompleted("testGetMethods(refHeader,headerToTest)");
	}
        
        
        
    }
    
    
    
    
    public  void testHeaderFactory()  {
        
        try {
        for (int i = 0; i < headers.length; i++) {
            StringBuffer value = new StringBuffer(headers[i]);
            StringBuffer name= new StringBuffer();
	    StringBuffer body = new StringBuffer();
	    int j  = 0;
            for ( j = 0; j < value.length(); j++) {

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
            
             riHeader = riHeaderFactory.createHeader
			(name.toString().trim(),body.toString().trim());

	    } catch (ParseException ex) {
		 throw new TckInternalError(ex.getMessage());
	    }
	
		
            Header headerToTest = tiHeaderFactory.createHeader
            ( name.toString().trim(),body.toString().trim());
	    assertTrue(headerToTest != null);
                
            testGetMethods(riHeader,headerToTest);
            
        }
	for (int i = 0; i < multiHeaders.length; i++ ) {
            StringBuffer value = new StringBuffer(multiHeaders[i]);
            List riHeaders = null;
   	    List tiHeaders = null;
	    try {
            
             riHeaders = riHeaderFactory.createHeaders
			(multiHeaders[i]);

	    } catch (ParseException ex) {
		 throw new TckInternalError(ex.getMessage());
	    }
	
		
            tiHeaders = tiHeaderFactory.createHeaders ( multiHeaders[i] );
	    assertTrue(tiHeaders != null);
	    assertTrue(tiHeaders.size() == riHeaders.size());
	    ListIterator li = riHeaders.listIterator();
	    ListIterator li1 = tiHeaders.listIterator();
	    while (li.hasNext()) {
		Header riHeader = (Header) li.next();
		Header headerToTest = (Header) li1.next();
                testGetMethods(riHeader,headerToTest);
	    }

	}
        } catch (Exception ex) {
		throw new TiUnexpectedError(ex.getMessage());
        } finally {
	      logTestCompleted("testHeaderFactory()");
	}

        
    }
    
    public HeaderFactoryTest() {
        super("HeaderFactoryTest");
    }
    
    
    public static void main(String[] args ) {
        junit.textui.TestRunner.run(HeaderFactoryTest.class);
    }
    
    
    
    
}


