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
/*****************************************************************************
 * PRODUCT OF PT INOVACAO - EST DEPARTMENT and Aveiro University - Portugal)   *
 *****************************************************************************/


package gov.nist.javax.sdp.fields;


import java.io.Serializable;
import java.util.Vector;
import javax.sdp.SdpException;
import javax.sdp.SdpParseException;
import gov.nist.core.NameValue;




/**
 * Precondition fields (segmented / end-to-end).
 *
 * <p>For one media description, precondition attributes should
 * be only of one type (segmented or end-to-end) </p>
 * <p>3GPP TS 24.299, </p>
 * IETF RFC3312 + RFC4032 (Precondition Mechanism).
 *
 *  Serialization fix contributed by aregr@dev.java.net
 *
 * @author Miguel Freitas (IT) PT-Inovacao
 */
public class PreconditionFields implements Serializable
{

    // Media Description attributes for precondition
    protected Vector preconditionAttributes;


    /**
     * Constructor
     *
     */
    public PreconditionFields()
    {
        preconditionAttributes = new Vector();
    }


    /**
     * Get the number of Precondition attributes
     *
     * @return int size of segmented precondition vector of attribute fields
     */
    public int getPreconditionSize()
    {
        if (preconditionAttributes != null)
            return preconditionAttributes.size();

        else    // TODO treat this exception well
            return -1;
    }

    /**
     * Get Precondition
     *
     * @return Vector of attribute fields (segmented precondition)
     */
    public Vector getPreconditions()
    {
        return preconditionAttributes;
    }


    /**
     * Set Preconditions
     *
     * @param preconditions - vector with precondition attributes
     * @throws SdpException -- if precondition attributes is null
     */
    public void setPreconditions(Vector preconditions) throws SdpException
    {
        if (preconditions == null)
            throw new SdpException("Precondition attributes are null");
        else
            preconditionAttributes = preconditions;
    }



    /**
     * <p>Set attribute line for current precondition state, given
     * a string value encoded like the "curr" attribute value</p>
     *
     * @param precondCurrValue - a string with the value for a "curr" attribute
     * @throws SdpException
     */
    public void setPreconditionCurr(String precondCurrValue) throws SdpException
    {
        if (precondCurrValue == null)
            throw new SdpException("The Precondition \"curr\" attribute value is null");
        else if (preconditionAttributes == null)
            throw new SdpException("The Precondition Attributes is null");
        else
        {
            try
            {
                /* split the "curr" attribute value into words
                 *      attributes[0] = "qos"
                 *      attributes[1] = status-type   (e2e/local/remote)
                 *      attributes[2] = direction-tag (none/send/recv/sendrecv)
                 *
                 * split() - regular expression:
                 *      \s -> a whitespace character [ \t\n\x0B\f\r]
                 *      \b -> a word boundary
                 */
                String[] attributes = precondCurrValue.split(" ");


                // which is this length?! of the string or the []?
                /*
                if (attributes.length < 3)
                {
                    throw new SdpException
                        ("The Precondition \"curr\" attribute value mal-formed (<3words)");
                }*/

                setPreconditionCurr(attributes[1],      // status-type
                                    attributes[2]       // direction-tag
                                );
            }
            catch (ArrayIndexOutOfBoundsException ex)
            {
                throw new SdpException
                    ("Error spliting the \"curr\" attribute into words", ex);
            }
        }
    }





    /**
     * <p>Set the value of the attributes with the name "curr"
     * for current precondition.</p>
     * <p>- eg: a=curr:qos local none</p>
     *
     * <p>If there is an attribute "curr" for the same status-type,
     * the direction-tag is updated with the one supplied.</p>
     *
     * @param status - (local, remote, e2e)
     * @param directionTag - (none, send, recv, sendrecv)
     * @throws SdpParseException
     */
    public void setPreconditionCurr(String status, String directionTag)
        throws SdpException
    {
        if (status == null)
            throw new SdpException("The status-type is null");
        if (directionTag == null)
            throw new SdpException("The direction-tag is null");

        if (preconditionAttributes == null)
            throw new SdpException("Precondition Attributes is null");

        int i = 0;
        // search for attributes "curr"
        for (i = 0; i < preconditionAttributes.size(); i++)
        {
            AttributeField af =
                (AttributeField) this.preconditionAttributes.elementAt(i);

            // go to next attribute if this is not "curr"
            if (!af.getAttribute().getName().equals("curr"))
                continue;

            // if it is "curr" attribute, check the status-type
            // if it matches the value supplied, update the direction-tag
            // with the value supplied
            if (af.getValue().indexOf(status) != -1)
            {
                if (af.getValue().indexOf(directionTag) == -1)
                {
                    // attribute exists with same status, directionTag updated
                    af.setValue("qos " + status + " " + directionTag);
                    preconditionAttributes.setElementAt(af,i);
                }
                // else, exit and do nothing (attribute already exists)
                else
                    break;
            }
        }

        // attribute "curr" not found
        if (i == preconditionAttributes.size())
        {
            // create attribute for the status-type supplied
            NameValue nv = new NameValue("curr", "qos " + status + " " + directionTag);

            AttributeField newAF = new AttributeField();
            newAF.setAttribute(nv);
            preconditionAttributes.add(newAF);
        }
    }







    /**
     * <p>Set attribute line for desired precondition state, given
     * a string value encoded like the "des" attribute value</p>
     *
     * @param precondDesValue - a string with the value for a "des" attribute
     * @throws SdpException
     */
    public void setPreconditionDes(String precondDesValue) throws SdpException
    {
        if (precondDesValue == null)
            throw new SdpException("The Precondition \"des\" attribute value is null");
        else if (preconditionAttributes == null)
            throw new SdpException("The Precondition Attributes is null");
        else
        {
            /* split the "des" attribute value into words
             *      attributes[0] = "qos"
             *      attributes[1] = strength-tag  (unknown/failure/none/optional/mandatory)
             *      attributes[2] = status-type   (e2e/local/remote)
             *      attributes[3] = direction-tag (none/send/recv/sendrecv)
             *
             * split() - regular expression:
             *      \s -> a whitespace character [ \t\n\x0B\f\r]
             *      \b -> a word boundary
             */
            try
            {
                String[] attributes = precondDesValue.split(" ");

                // which is this length?! of the string or the []?
                /*
                if (attributes.length < 4)
                {
                    throw new SdpException
                        ("The Precondition \"des\" attribute value mal-formed (<4words)");
                }*/

                setPreconditionDes( attributes[1],      // strength-tag
                                    attributes[2],      // status-type
                                    attributes[3]       // direction-tag
                                );
            }
            catch (ArrayIndexOutOfBoundsException ex)
            {
                throw new SdpException
                    ("Error spliting the \"des\" attribute into words", ex);
            }
        }
    }






    /**
     * <p>Set the value of the attributes with the name "des"
     * for desired precondition. </p>
     * <p>- eg: a=des:qos mandatory remote sendrecv</p>
     *
     * <p>There can be more than one desired precondition line
     * for a status-type, specially if strength-tag is different.</p>
     *
     * <p>If there is an attribute "des" for the same status-type,
     * the strength-tag and direction-tag are updated with the
     * ones supplied.<p>
     *
     * <p>IETF RFC4032: strength should only be downgraded within SDP offers</p>
     *
     *
     * @param strength - (none, optional,
     * @param status - (local, remote, e2e)
     * @param direction - (none, send, recv, sendrecv)
     * @throws SdpParseException
     */
    public void setPreconditionDes(String strength, String status, String direction)
        throws SdpException
    {
        if (strength == null)
            throw new SdpException("The strength-tag is null");
        if (status == null)
            throw new SdpException("The status-type is null");
        if (direction == null)
            throw new SdpException("The direction-tag is null");

        if (preconditionAttributes == null)
            throw new SdpException("Precondition Attributes is null");

        int i = 0;
        // search for attributes "des"
        for (i = 0; i < preconditionAttributes.size(); i++)
        {
            AttributeField af =
                (AttributeField) this.preconditionAttributes.elementAt(i);

            // go to next attribute if this is not "des"
            if (!af.getAttribute().getName().equals("des"))
                continue;

            // if it is "des" attribute, check the status-type
            // if it match, update the strength-tag and direction-tag
            if (af.getValue().indexOf(status) != -1)
            {
                // attribute exists with same status-type,
                // strength-tag and direction-tag updated
                af.setValue("qos " + strength + " " + status + " " + direction);
                preconditionAttributes.setElementAt(af,i);

            }
        }

        // attribute "des" not found
        if (i == preconditionAttributes.size())
        {
            // create attribute for the status-type supplied
            // with the values strength-tag and direction-tag
            NameValue nv =
                new NameValue("des", "qos " + strength + " " + status + " " + direction);

            AttributeField newAF = new AttributeField();
            newAF.setAttribute(nv);
            preconditionAttributes.add(newAF);
        }
    }







    /**
     * <p>Set attribute line for confirmation precondition request, given
     * a string value encoded like the "conf" attribute value</p>
     *
     * @param precondConfValue - a string with the value for a "conf" attribute
     * @throws SdpException
     */
    public void setPreconditionConfirmStatus(String precondConfValue)
        throws SdpException
    {
        if (precondConfValue == null || precondConfValue.length()==0)
            throw new SdpException("The Precondition \"conf\" attribute value is null");
        else if (preconditionAttributes == null)
            throw new SdpException("The Precondition Attributes is null");
        else
        {
            /* split the "conf" attribute value into words
             *      attributes[0] = "qos"
             *      attributes[1] = status-type   (e2e/local/remote)
             *      attributes[2] = direction-tag (none/send/recv/sendrecv)
             */
            try
            {
                String[] attributes = precondConfValue.split(" ");

                setPreconditionConfirmStatus(
                                    attributes[1],      // status-type
                                    attributes[2]       // direction-tag
                                );
            }
            catch (ArrayIndexOutOfBoundsException ex)
            {
                throw new SdpException
                    ("Error spliting the \"conf\" attribute into words", ex);
            }
        }
    }





    /**
     * <p>IETF RFC3312</p>
     * <p>"The confirmation status attribute carries threshold conditions
     * for a media stream. When the status of network resources reach
     * these conditions, the peer UA will send an update of the
     * session description".</p>
     *
     * <p>- eg: a=conf:qos remote sendrecv</p>
     *
     * @param status - (e2e, local, remote)
     * @param direction - (none, send, recv, sendrecv)
     * @throws SdpException -- if param are null
     */
    public void setPreconditionConfirmStatus(String status, String direction)
        throws SdpException
    {
        if (status == null || direction.length()==0)
            throw new SdpException("The status-type is null");
        if (direction == null || direction.length()==0)
            throw new SdpException("The direction-tag is null");

        if (preconditionAttributes == null)
            throw new SdpException("Precondition Attributes is null");

        int i = 0;
        // search for attributes "conf"
        for (i = 0; i < preconditionAttributes.size(); i++)
        {
            AttributeField af =
                (AttributeField) this.preconditionAttributes.elementAt(i);


            //System.out.println("--> PreconditionField -> (i="+i+") > attribute field: " + af.toString());


            // go to next attribute if this is not "conf"
            if (!af.getAttribute().getName().equals("conf"))
                continue;

            // if it is "conf" attribute, check the status-type
            // if it matches, update the direction-tag
            // with the value supplied
            if (af.getValue().indexOf(status) != -1)
            {
                if (af.getValue().indexOf(direction) == -1)
                {
                    // attribute exists with same status, directionTag updated
                    af.setValue("qos " + status + " " + direction);
                    preconditionAttributes.setElementAt(af,i);
                }
                // else, exit and do nothing (attribute already exists)
                break;
            }
        }

        // attribute "conf" not found
        if (i == preconditionAttributes.size())
        {
            // create attribute for the status-type supplied
            // with the values strength-tag and direction-tag
            NameValue nv =
                new NameValue("conf", "qos " + status + " " + direction);

            AttributeField newAF = new AttributeField();
            newAF.setAttribute(nv);
            preconditionAttributes.add(newAF);


            //System.out.println("--> PreconditionField -> new \"conf\" attribute created: " + newAF.toString() );
        }
    }










    /**
     * <p>Get the attribute fields with the name "curr"
     * for current precondition.</p>
     * <p>One attribute field per status-type.
     * (eg: a=curr:qos local none)</p>
     *
     * @param status - (local, remote, e2e)
     * @return a vector with the attribute field that match status-type
     * (with only one element or null if none exists)
     * @throws SdpParseException
     */
    public Vector getPreconditionCurr(String status)
        throws SdpException, SdpParseException
    {
        if (status == null)
            throw new SdpException("The status-type is null");

        if (preconditionAttributes == null)
            return null;
        else
        {
            Vector vCurr = new Vector();
            for (int i=0; i < preconditionAttributes.size(); i++)
            {
                AttributeField af =
                    (AttributeField) this.preconditionAttributes.elementAt(i);

                // go to next attribute if this is not "curr"
                if (!af.getAttribute().getName().equals("curr"))
                    continue;

                // if it is "curr" attribute, check the status-type
                // and add the attribute value to the vector if it
                // matches the status desired
                if (af.getValue().indexOf(status) != -1)
                    vCurr.addElement(af);
            }

            // if none "curr" attribute found
            if (vCurr.size() == 0)
                return null;
            else
                return vCurr;
        }
    }




    /**
     * <p>Get the attribute fields with the name "des"
     * for desired precondition</p>
     *
     * <p>There can be more than one current precondition line
     * for a status-type (with different direction-tag values),
     * specially if strength-tag is different</p>
     *
     * @param status - (local, remote, e2e)
     * @return a vector with the attribute fields that match location-tag
     * @throws SdpParseException
     */
    public Vector getPreconditionDes(String status)
        throws SdpException, SdpParseException
    {
        if (status == null)
            throw new SdpException("The status-type is null");

        if (preconditionAttributes == null)
            return null;
        else
        {
            Vector vCurr = new Vector();
            for (int i=0; i < preconditionAttributes.size(); i++)
            {
                AttributeField af =
                    (AttributeField) this.preconditionAttributes.elementAt(i);

                // go to next attribute if this is not "des"
                if (!af.getAttribute().getName().equals("des"))
                    continue;

                // if it is "des" attribute, check the status-type
                // and add the attribute value to the vector if it
                // matches the status desired
                if (af.getValue().indexOf(status) != -1)
                    vCurr.addElement(af);
            }

            // if none "des" attribute found
            if (vCurr.size() == 0)
                return null;
            else
                return vCurr;
        }
    }



    /**
     * <p>Get the attribute fields with the name "conf"
     * for confirmation precondition.</p>
     *
     * <p>IETF RFC3312</p>
     * <p>"The confirmation status attribute carries threshold conditions
     * for a media stream. When the status of network resources reach
     * these conditions, the peer UA will send an update of the
     * session description".</p>
     *
     * <p>(eg: a=conf:qos remote sendrecv)</p>
     *
     * @return a vector with the "conf" attribute fields
     * @throws SdpException
     */
    public Vector getPreconditionConfirmStatus() throws SdpException
    {
        if (preconditionAttributes == null)
            return null;        // ignore or send SdpException?
        else
        {
            Vector vCurr = new Vector();
            for (int i=0; i < preconditionAttributes.size(); i++)
            {
                AttributeField af =
                    (AttributeField) this.preconditionAttributes.elementAt(i);

                // go to next attribute if this is not "conf"
                if (!af.getAttribute().getName().equals("conf"))
                    continue;
                else
                    // add the attribute value to the vector
                    vCurr.addElement(af);
            }

            // if none "conf" attribute found
            if (vCurr.size() == 0)
                return null;
            else
                return vCurr;
        }
    }



    /* strength-tag */
    public static final int STRENGTH_UNKNOWN    = 0;
    public static final int STRENGTH_FAILURE    = 1;
    public static final int STRENGTH_NONE       = 2;
    public static final int STRENGTH_OPTIONAL   = 3;
    public static final int STRENGTH_MANDATORY  = 4;
    public static final String[] STRENGTH = { "unknown",
                                                  "failure",
                                                  "none",
                                                  "optional",
                                                  "mandatory"
                                            };
    /* direction-tag */
    public static final int DIRECTION_NONE      = 0;
    public static final int DIRECTION_SEND      = 1;
    public static final int DIRECTION_RECV      = 2;
    public static final int DIRECTION_SENDRECV  = 3;
    public static final String[] DIRECTION = { "none",
                                                  "send",
                                                  "recv",
                                                  "sendrecv"
                                            };

    /* status-tag */
    public static final int STATUS_E2E      = 0;
    public static final int STATUS_LOCAL    = 1;
    public static final int STATUS_REMOTE   = 2;
    public static final String[] STATUS = { "e2e",
                                            "local",
                                            "remote"
                                            };

    /* precondition type */
    public static final int PRECONDITION_QOS    = 0;
    public static final String[] PRECONDITION   = { "qos"
                                                };






}
