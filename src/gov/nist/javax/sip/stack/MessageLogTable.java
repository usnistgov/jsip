/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.stack;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Enumeration;
/** A table for remote access of log records.
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/

public interface MessageLogTable extends Remote {

    /** Get the log for a given key.
    */
    public String flush() throws RemoteException;

    /** Release the logs.
     */
    public void release() throws RemoteException;

    /** Pointer to a remote repository so we can deal with just one
    * collection point.
    public void registerRemoteLog(String rmiURL) throws RemoteException;
    public void submitRemoteLog(String stackId, String log, String auxInfo) 
		throws RemoteException;
    */

    /** Get auxiliary information associated with a stack (un-interpreted)
    */
    public String getAuxInfo(String stackId) throws RemoteException ;


}
