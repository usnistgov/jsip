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
package gov.nist.javax.sip.stack;

import gov.nist.core.CommonLogger;
import gov.nist.core.LogWriter;
import gov.nist.core.StackLogger;

import java.nio.ByteBuffer;

/**
 * Contributed by Alexander Saveliev, Avistar Communications for Issue http://java.net/jira/browse/JSIP-430
 * Allows to choose between direct vs non direct buffers
 * 
 */
public class ByteBufferFactory {

    private static StackLogger logger = CommonLogger.getLogger(ByteBufferFactory.class);

    private static ByteBufferFactory instance = new ByteBufferFactory();

    private boolean useDirect = true;

    public static ByteBufferFactory getInstance() {
        return instance;
    }

    public ByteBuffer allocateDirect(int capacity) {
        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
            logger.logTrace("Allocating direct buffer " + capacity);
        return useDirect ? ByteBuffer.allocateDirect(capacity) : ByteBuffer.allocate(capacity);
    }

    public ByteBuffer allocate(int capacity) {
        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
            logger.logTrace("Allocating buffer " + capacity);
        return ByteBuffer.allocate(capacity);
    }


    public void setUseDirect(boolean useDirect) {
        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
            logger.logTrace("Direct buffers are " + (useDirect ? "enabled" : "disabled"));
        this.useDirect = useDirect;
    }
}
