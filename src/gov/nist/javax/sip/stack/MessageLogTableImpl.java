/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/

package gov.nist.javax.sip.stack;
import java.rmi.*;
import java.rmi.server.*;
import java.util.*;
import java.io.*;

/**
* This class stores all the message logs accessible via RMI
* Here is the format of the XML file:
*<pre>
* <message from=identifier for the machine where the message originated
* to= identifier for the destination
* time= time the message was logged
* statusMessage= un-interpreted status information
* transactionId= Transaction identifier (for coloring the arrows).
* firstLine= first line of the message
* <![CDATA[ Message content ]]>
* ...
*<messages>
*
* For example:
* <message from="127.0.0.1:2000"
* to="127.0.0.1:5070"
* time="1013714883983"
* statusMessage="FSM Nodes Fired by Incoming Msg:  node3"
* transactionId="127.0.0.1:1:INVITE:127.0.0.1:2000"
* firstLine="INVITE sip:127.0.0.1:5070;transport=UDP SIP/2.0">
* <![CDATA[INVITE sip:127.0.0.1:5070;transport=UDP SIP/2.0
* Via: SIP/2.0/UDP 127.0.0.1:2000;branch=fcab1368e30f422f3d9bbdcd5293398c.1
* Via: SIP/2.0/UDP 127.0.0.1:5071
* Record-Route: <sip:127.0.0.1:2000;transport=UDP;maddr=127.0.0.1>
* CSeq: 1 INVITE
* Call-Id: 679a7c8a02d479d0187ca51087d5fe3e@127.0.0.1
* From: <sip:BigGuy@here.com>
*To: <sip:LittleGuy@there.com>
* Content-Length: 0
* ]]>
* ...
* </messages>
*</pre>
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov> 
*@author Marc Bednarek 
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/

class MessageLogTableImpl extends UnicastRemoteObject 
    implements MessageLogTable {

    // A table that 
    private Hashtable localLogTable;
    private Hashtable auxInfoTable;
    private final int defaultLifeTime = 3600; /* 1 hour */
    private int lifeTime;
    private Hashtable timeOut;
    private String logRootName;
    private String rmiHostName;


    /**
     * Constructor
     */
 
    public MessageLogTableImpl() throws RemoteException {
        super();
	this.auxInfoTable = new Hashtable();
	this.localLogTable = new Hashtable();
	this.timeOut = new Hashtable();
    }

    public MessageLogTableImpl(int port) throws RemoteException {
        super(port);
	this.auxInfoTable = new Hashtable();
	this.localLogTable = new Hashtable();
	this.timeOut = new Hashtable();
    }

    public void init(String rmiHostName, int port, String logRootName)  
		throws RemoteException {
	init(rmiHostName, port, logRootName, defaultLifeTime);
    }

    public void init(String rmiHostName, int port,  
	String logRootName, int lifeTime) 
	throws RemoteException {
	try {
	  //this.logRootName = URLEncoder.encode(logRootName,"UTF-8");
            this.logRootName = logRootName;
	  this.rmiHostName = rmiHostName;
	  if (lifeTime == 0) {
	      this.lifeTime = defaultLifeTime;
	  } else {
	      this.lifeTime = lifeTime;
	  }
       /*
	try { 
	  UnicastRemoteObject.exportObject(this);
	} catch (Exception ex) {}
	*/
           if (System.getSecurityManager() == null) {
	      System.setSecurityManager(new RMISecurityManager());
	   }
	   String name = "//" + rmiHostName + ":" + port + "/" + 
	     	logRootName +"/" + getClass().getName();
	    System.out.println("rebind : "  + name);
	    Naming.rebind(name, this);
	    System.out.println(getClass() + " bound");
	} catch (Exception e) {
	    System.err.println(getClass() + " exception: " + 
			       e.getMessage());
	    e.printStackTrace();
	    throw new RemoteException("problem binding " + e.getMessage());
	}
    }





    /** Return all our information to a remote caller.
    *@param key is the log id -- could refer to a 
    * locally generated log or a remotely submitted log.
    */
    public synchronized String flush() throws RemoteException {
	try {
	  File file = new File (ServerLog.getLogFileName());
	  long length = file.length();
	  char[] cbuf = new char[(int)length];
	  FileReader fr = new FileReader( file );
	  fr.read(cbuf);
	  return new String(cbuf);
	} catch (IOException ex) {
		return "";
	}
    }

    /** Gets any auxiliary information that may be stored here. (For example,
     * in our test system we are storing the XML for the responder).
     *@param logName is the name for which we are storing the 
     * remote information.
     */
    public String getAuxInfo(String logName) throws RemoteException {
	
	// System.out.println("!!! for logname : "
	//		+logName+" -- "+auxInfoTable.get(logName)+" !!!") ;
	return (String) auxInfoTable.get(logName);
	
    }

    /** Clean the logs. Call this to delete the parsed logs and save space 
    * in the memory of the server.
    */
    public synchronized void release() throws RemoteException {
	this.localLogTable = null;
    }

}
