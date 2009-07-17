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
/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sdp.fields;
import gov.nist.core.*;
import javax.sdp.*;
/**
* Connectin Field of the SDP request.
*
*@version  JSR141-PUBLIC-REVIEW (subject to change).
*
*@author Olivier Deruelle <deruelle@antd.nist.gov>
*@author M. Ranganathan   <br/>
*
*
*
*/
public class ConnectionField extends SDPField implements javax.sdp.Connection {
    protected String nettype;
    protected String addrtype;
    protected ConnectionAddress address;

    public ConnectionField() {
        super(SDPFieldNames.CONNECTION_FIELD);
    }
    public String getNettype() {
        return nettype;
    }
    public String getAddrtype() {
        return addrtype;
    }
    public ConnectionAddress getConnectionAddress() {
        return address;
    }
    /**
    * Set the nettype member
    */
    public void setNettype(String n) {
        nettype = n;
    }
    /**
    * Set the addrtype member
    */
    public void setAddrType(String a) {
        addrtype = a;
    }
    /**
    * Set the address member
    */
    public void setAddress(ConnectionAddress a) {
        address = a;
    }
    /**
    * Get the string encoded version of this object
    * @since v1.0
    */
    public String encode() {
        String encoded_string = CONNECTION_FIELD;
        if (nettype != null)
            encoded_string += nettype;
        if (addrtype != null)
            encoded_string += Separators.SP + addrtype;
        if (address != null)
            encoded_string += Separators.SP + address.encode();
        return encoded_string += Separators.NEWLINE;
    }

    public String toString() {
        return this.encode();
    }
    /** Returns the type of the network for this Connection.
     * @throws SdpParseException
     * @return the type of the network
     */
    public String getAddress() throws SdpParseException {
        ConnectionAddress connectionAddress = getConnectionAddress();
        if (connectionAddress == null)
            return null;
        else {
            Host host = connectionAddress.getAddress();
            if (host == null)
                return null;
            else
                return host.getAddress();
        }
    }

    /** Returns the type of the address for this Connection.
     * @throws SdpParseException
     * @return the type of the address
     */
    public String getAddressType() throws SdpParseException {
        return getAddrtype();
    }

    /** Returns the type of the network for this Connection.
     * @throws SdpParseException
     * @return the type of the network
     */
    public String getNetworkType() throws SdpParseException {
        return getNettype();
    }

    /** Sets the type of the address for this Connection.
     * @param addr to set
     * @throws SdpException if the type is null
     */
    public void setAddress(String addr) throws SdpException {
        if (addr == null)
            throw new SdpException("the addr is null");
        else {
            if (address == null) {
                address = new ConnectionAddress();
                Host host = new Host(addr);
                address.setAddress(host);
            } else {
                Host host = address.getAddress();
                if (host == null) {
                    host = new Host(addr);
                    address.setAddress(host);
                } else
                    host.setAddress(addr);
            }
            setAddress(address);
        }
    }

    /** Returns the type of the network for this Connection.
     * @param type to set
     * @throws SdpException if the type is null
     */
    public void setAddressType(String type) throws SdpException {
        if (type == null)
            throw new SdpException("the type is null");
        this.addrtype = type;
    }

    /** Sets the type of the network for this Connection.
     * @param type to set
     * @throws SdpException if the type is null
     */
    public void setNetworkType(String type) throws SdpException {
        if (type == null)
            throw new SdpException("the type is null");
        else
            setNettype(type);
    }
    public Object clone() {
        ConnectionField retval = (ConnectionField) super.clone();
        if (this.address != null)
            retval.address = (ConnectionAddress) this.address.clone();
        return retval;
    }

}
