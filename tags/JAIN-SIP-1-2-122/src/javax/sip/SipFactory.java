/**
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Unpublished - rights reserved under the Copyright Laws of the United States.
 * Copyright © 2003 Sun Microsystems, Inc. All rights reserved.
 * Copyright © 2005 BEA Systems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * This distribution may include materials developed by third parties.
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 *
 * Module Name   : JSIP Specification
 * File Name     : SipFactory.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty
 *  1.2     20/12/2005  Phelim O'Doherty    Modified to enable single stack
 *                                          instance if no IP Address config
 *                                          property.
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */

package javax.sip;

import javax.sip.address.AddressFactory;
import javax.sip.message.MessageFactory;
import javax.sip.header.HeaderFactory;
import java.util.*;
import java.lang.reflect.Constructor;

/**
 * The SipFactory is a singleton class which applications can use a single
 * access point to obtain proprietary implementations of this specification. As
 * the SipFactory is a singleton class there will only ever be one instance of
 * the SipFactory. The single instance of the SipFactory can be obtained using
 * the {@link SipFactory#getInstance()} method. If an instance of the SipFactory
 * already exists it will be returned to the application, otherwise a new
 * instance will be created. A peer implementation object can be obtained from
 * the SipFactory by invoking the appropriate create method on the SipFactory
 * e.g. to create a peer SipStack, an application would invoke the
 * {@link SipFactory#createSipStack(Properties)} method.
 * <p>
 * <b>Naming Convention</b><br>
 * Note that the SipFactory utilises a naming convention defined by this
 * specification to identify the location of proprietary objects that implement
 * this specification. The naming convention is defined as follows:
 * <ul>
 * <li>The <b>upper-level package structure</b> referred to by the SipFactory
 * with the attribute <var>pathname</var> can be used to differentiate between
 * proprietary implementations from different SIP stack vendors. The
 * <var>pathname</var> used by each SIP vendor <B>must be</B> the domain name
 * assigned to that vendor in reverse order. For example, the pathname used by
 * Sun Microsystem's would be <code>com.sun</code>.
 * <li>The <b>lower-level package structure and classname</b> of a peer object
 * is also mandated by this specification. The lowel-level package must be
 * identical to the package structure defined by this specification and the
 * classname is mandated to the interface name appended with the
 * <code>Impl</code> post-fix. For example, the lower-level package structure
 * and classname of a proprietary implementation of the
 * <code>javax.sip.SipStack</code> interface <B>must</B> be
 * <code>javax.sip.SipStackImpl</code>.
 * </ul>
 *
 * Using this naming convention the SipFactory can locate a vendor's
 * implementation of this specification without requiring an application to
 * supply a user defined string to each create method in the SipFactory. Instead
 * an application merely needs to identify the vendors SIP implementation it
 * would like to use by setting the pathname of that vendors implementation.
 * <p>
 * It follows that a proprietary implementation of a peer object of this
 * specification can be located at:
 * <p>
 * <code>'pathname'.'lower-level package structure and classname'.</code>
 * <p>
 * For example an application can use the SipFactory to instantiate a NIST
 * peer SipStack object by setting the pathname to
 * <code>gov.nist</code> and calling the createSipStack method. The SipFactory
 * would return a new instance of the SipStack object at the following location:
 * <code>gov.nist.javax.sip.SipStackImpl.java</code> Because the space of
 * domain names is managed, this scheme ensures that collisions between two
 * different vendor's implementations will not happen. For example: a different
 * vendor with a domain name 'bea.com' would have their peer SipStack object
 * located at <code>com.bea.javax.sip.SipStackImpl.java</code>.
 * <p>
 * <b>Default Namespace:</b><br>
 * This specification defines a default namespace for the SipFactory, this
 * namespace is the location of the Reference Implementation. The default
 * namespace is <code>gov.nist</code> the author of the Reference
 * Implementation, therefore the <var>pathname</var> will have the initial
 * value of <code>gov.nist</code> for a new instance of the SipFactory. An
 * application must set the <var>pathname</var> of the SipFactory on retrieval
 * of a new instance of the factory in order to use a different vendors SIP
 * stack from that of the Reference Implementation. An application can not mix
 * different vendor's peer implementation objects.
 *
 * @author BEA Systems, NIST
 * @version 1.2
 */

public class SipFactory {

    /**
     * Returns an instance of a SipFactory. This is a singleton class so this
     * method is the global access point for the SipFactory.
     *
     * @return the single instance of this singleton SipFactory
     */
    public synchronized static SipFactory getInstance() {
        if (myFactory == null) {
            myFactory = new SipFactory();
        }
        return myFactory;
    }

      /**
       * Creates an instance of a SipStack implementation based on the
       * configuration properties object passed to this method. The
       * recommended behaviour is to not specify an
       * "javax.sip.IP_ADDRESS" property in the Properties argument, in this
       * case the "javax.sip.STACK_NAME" uniquely identifies the stack.
       * A new stack instance will be returned for each different stack name
       * associated with a specific vendor implementation. The
       * ListeningPoint is used to configure the IP Address argument.
       * For backwards compatability, if a "javax.sip.IP_ADDRESS" is supplied,
       * this method ensures that only one instance of a SipStack is returned
       * to the application for that IP Address, independent of the number of
       * times this method is called. Different SipStack instances are
       * returned for each different IP address.
       * <p>
       * See {@link SipStack} for the expected format of the
       * <code>properties</code> argument.
       *
       * @throws PeerUnavailableException
       *             if the peer class could not be found
       */
    public synchronized SipStack createSipStack(Properties properties)
            throws PeerUnavailableException {

        String ipAddress = properties.getProperty("javax.sip.IP_ADDRESS");
        String name = properties.getProperty("javax.sip.STACK_NAME");
        if (name == null ) throw new PeerUnavailableException("Missing javax.sip.STACK_NAME property");
        // IP address was not specified in the properties.
        // This means that the architecture supports a single sip stack
        // instance per stack name
        // and each listening point is assinged its own IP address.
        if ( ipAddress == null) {
            SipStack mySipStack = (SipStack) this.sipStackByName.get(name);
            if (mySipStack == null) {
                mySipStack = createStack(properties);
            }
            return mySipStack;
        } else {
            // Check to see if a stack with that IP Address is already
            // created, if so select it to be returned. In this case the
            // the name is not used.
            int i = 0;
            for (i = 0; i < sipStackList.size(); i++) {
                if (((SipStack) sipStackList.get(i)).getIPAddress().equals( ipAddress )) {
                    return (SipStack) sipStackList.get(i);
                }
            }
            return createStack(properties);
        }

    }

    /**
     * Creates an instance of the MessageFactory implementation. This method
     * ensures that only one instance of a MessageFactory is returned to the
     * application, no matter how often this method is called.
     *
     * @throws PeerUnavailableException
     *             if peer class could not be found
     */

    public MessageFactory createMessageFactory()
            throws PeerUnavailableException {
        if (messageFactory == null) {
            messageFactory = (MessageFactory) createSipFactory("javax.sip.message.MessageFactoryImpl");
        }
        return messageFactory;
    }

    /**
     * Creates an instance of the HeaderFactory implementation. This method
     * ensures that only one instance of a HeaderFactory is returned to the
     * application, no matter how often this method is called.
     *
     * @throws PeerUnavailableException
     *             if peer class could not be found
     */
    public HeaderFactory createHeaderFactory() throws PeerUnavailableException {
        if (headerFactory == null) {
            headerFactory = (HeaderFactory) createSipFactory("javax.sip.header.HeaderFactoryImpl");
        }
        return headerFactory;
    }

    /**
     * Creates an instance of the AddressFactory implementation. This method
     * ensures that only one instance of an AddressFactory is returned to the
     * application, no matter how often this method is called.
     *
     * @throws PeerUnavailableException
     *             if peer class could not be found
     */
    public AddressFactory createAddressFactory()
            throws PeerUnavailableException {
        if (addressFactory == null) {
            addressFactory = (AddressFactory) createSipFactory("javax.sip.address.AddressFactoryImpl");
        }
        return addressFactory;
    }

    /**
     * Sets the <var>pathname</var> that identifies the location of a
     * particular vendor's implementation of this specification. The
     * <var>pathname</var> must be the reverse domain name assigned to the
     * vendor providing the implementation. An application must call
     * {@link SipFactory#resetFactory()} before changing between different
     * implementations of this specification.
     *
     * @param pathName -
     *            the reverse domain name of the vendor, e.g. Sun Microsystem's
     *            would be 'com.sun'
     */
    public void setPathName(String pathName) {
        this.pathName = pathName;
    }

    /**
     * Returns the current <var>pathname</var> of the SipFactory. The
     * <var>pathname</var> identifies the location of a particular vendor's
     * implementation of this specification as defined the naming convention.
     * The pathname must be the reverse domain name assigned to the vendor
     * providing this implementation. This value is defaulted to
     * <code>gov.nist</code> the location of the Reference Implementation.
     *
     * @return the string identifying the current vendor implementation.
     */
    public String getPathName() {
        return pathName;
    }

    /**
     * This method reset's the SipFactory's references to the object's it has
     * created. It allows these objects to be garbage collected assuming the
     * application no longer holds references to them. This method must be
     * called to reset the factories references to a specific vendors
     * implementation of this specification before it creates another vendors
     * implementation of this specification by changing the <var>pathname</var>
     * of the SipFactory.
     */
    public void resetFactory() {
        sipStackList.clear();
        messageFactory = null;
        headerFactory = null;
        addressFactory = null;
        sipStackByName = new Hashtable();
        pathName = "gov.nist";
    }

    /**
     * Private Utility method used by all create methods to return an instance
     * of the supplied object.
     */
    private Object createSipFactory(String objectClassName)
            throws PeerUnavailableException {

        // If the stackClassName is null, then throw an exception
        if (objectClassName == null) {
            throw new NullPointerException();
        }
        try {
            Class peerObjectClass = Class.forName(getPathName() + "."
                    + objectClassName);

            // Creates a new instance of the class represented by this Class
            // object.
            Object newPeerObject = peerObjectClass.newInstance();
            return (newPeerObject);
        } catch (Exception e) {
            String errmsg = "The Peer Factory: "
                    + getPathName()
                    + "."
                    + objectClassName
                    + " could not be instantiated. Ensure the Path Name has been set.";
            throw new PeerUnavailableException(errmsg, e);
        }
    }

    /**
     * Private Utility method used to create a new SIP Stack instance.
     */
    private SipStack createStack(Properties properties)
            throws PeerUnavailableException {
        try {
            // create parameters argument to identify constructor
            Class[] paramTypes = new Class[1];
            paramTypes[0] = Class.forName("java.util.Properties");
            // get constructor of SipStack in order to instantiate
            Constructor sipStackConstructor = Class.forName(
                    getPathName() + ".javax.sip.SipStackImpl").getConstructor(
                    paramTypes);
            // Wrap properties object in order to pass to constructor of
            // SipSatck
            Object[] conArgs = new Object[1];
            conArgs[0] = properties;
            // Creates a new instance of SipStack Class with the supplied
            // properties.
            SipStack  sipStack = (SipStack) sipStackConstructor.newInstance(conArgs);
            sipStackList.add(sipStack);
            String name = properties.getProperty("javax.sip.STACK_NAME");
            this.sipStackByName.put(name, sipStack);
                        return sipStack;
        } catch (Exception e) {
            String errmsg = "The Peer SIP Stack: "
                    + getPathName()
                    + ".javax.sip.SipStackImpl"
                    + " could not be instantiated. Ensure the Path Name has been set.";
            throw new PeerUnavailableException(errmsg, e);
        }
    }

    /**
     * Constructor for SipFactory class. This is private because applications
     * are not permitted to create an instance of the SipFactory using "new".
     */
    private SipFactory() {
        this.sipStackByName = new Hashtable();
    }

    // default domain to locate Reference Implementation
    private String pathName = "gov.nist";

    // My sip stack. The implementation will allow only a single
    // sip stack in future versions of this specification.

    private Hashtable sipStackByName;

    // intrenal variable to ensure SipFactory only returns a single instance
    // of the other Factories and SipStack
    private MessageFactory messageFactory = null;

    private HeaderFactory headerFactory = null;

    private AddressFactory addressFactory = null;

    private static SipFactory myFactory = null;

    // This is for backwards compatibility with
    // version 1.1

    private final LinkedList sipStackList = new LinkedList();

}
