/*
 * This source code has been contributed to the public domain by Mobicents
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
 * of the terms of this agreement.
 */
 package test.unit.gov.nist.javax.sip.stack.dialog.timeout;

import gov.nist.javax.sip.SipStackImpl;

import javax.sip.SipProvider;

import org.apache.log4j.Logger;

import test.tck.msgflow.callflows.ProtocolObjects;
import test.tck.msgflow.callflows.ScenarioHarness;

/**
 * Testing if Dialog Timeout Event is correctly passed to the application layer on both sides if the 
 * ACK is not sent by the UAC and the autodialog flag is false only
 * 
 * @author jean deruelle
 *
 */
public class DialogTimeoutTest extends ScenarioHarness {

	private ProtocolObjects shootistProtocolObjs;

	private ProtocolObjects shootmeProtocolObjs;

	    
    protected Shootist shootist;
    
    protected ShootistNotImplementingSipListenerExt shootistNotImplementingSipListenerExt;

    private Shootme shootme;
    
    private ShootmeNotImplementingListener shootmeNotImplementingListener;

    private static final Logger logger = Logger.getLogger("test.tck");
    private static final int TIMEOUT = 60000;

    static {
        if (!logger.isAttached(console))
            logger.addAppender(console);
    }

    public DialogTimeoutTest() {
        super("DialogTerminationOn500Test", true);

    }

    public void setUp() {
        doSetUp();

    }

    private void doSetUp() {
        try {            
            super.setUp();            
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("unexpected exception ");
        }
    }

    // check that the apps gets called for timeout event when no ack is received nor sent
    public void testDialogTimeoutSipListenerExt() {
        
            try {
            	this.shootistProtocolObjs = new ProtocolObjects("shootist", "gov.nist", "udp", false,false, true);
                shootist = new Shootist(shootistProtocolObjs);
                SipProvider shootistProvider = shootist.createSipProvider();

                this.shootmeProtocolObjs = new ProtocolObjects("shootme", "gov.nist", "udp", false,false, true);
                ((SipStackImpl)shootmeProtocolObjs.sipStack).setAggressiveCleanup(true);
                shootme = new Shootme(shootmeProtocolObjs);
                SipProvider shootmeProvider = shootme.createSipProvider();
               
                shootist.init();
                providerTable.put(shootistProvider, shootist);

                shootme.init();
                providerTable.put(shootmeProvider, shootme);
                shootistProvider.addSipListener(shootist);
                shootmeProvider.addSipListener(shootme);

                getRiProtocolObjects().start();
                if (getTiProtocolObjects() != getRiProtocolObjects())
                    getTiProtocolObjects().start();
                
                this.shootist.sendInviteRequest();
                Thread.currentThread().sleep(TIMEOUT);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                fail("unexpected exception ");
            }

            if (!this.shootme.checkState() || !this.shootist.checkState()) {
                fail("Test Failed - Didnt receive Dialog Timeout Event");
                return;
            }

            doTearDown(false);
            doSetUp();
    }
    
    // check that the apps gets called for timeout event when no ack is received nor sent
    // and for terminated event after the BYE is sent
    public void testDialogTimeoutAndTerminatedSipListenerExt() {
        
        try {
        	this.shootistProtocolObjs = new ProtocolObjects("shootist", "gov.nist", "udp", false,false, true);
            shootist = new Shootist(shootistProtocolObjs);
            shootist.setSendByeOnDialogTimeout(true);
            SipProvider shootistProvider = shootist.createSipProvider();

            this.shootmeProtocolObjs = new ProtocolObjects("shootme", "gov.nist", "udp", false,false, true);
            ((SipStackImpl)shootmeProtocolObjs.sipStack).setAggressiveCleanup(true);
            shootme = new Shootme(shootmeProtocolObjs);
            shootme.setReceiveBye(true);
            SipProvider shootmeProvider = shootme.createSipProvider();
           
            shootist.init();
            providerTable.put(shootistProvider, shootist);

            shootme.init();
            providerTable.put(shootmeProvider, shootme);
            shootistProvider.addSipListener(shootist);
            shootmeProvider.addSipListener(shootme);

            getRiProtocolObjects().start();
            if (getTiProtocolObjects() != getRiProtocolObjects())
                getTiProtocolObjects().start();
            
            this.shootist.sendInviteRequest();
            Thread.currentThread().sleep(TIMEOUT);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail("unexpected exception ");
        }

        if (!this.shootme.checkState() || !this.shootist.checkState()) {
            fail("Test Failed - Didnt receive Dialog Timeout Event");
            return;
        }

        doTearDown(false);
        doSetUp();
    }
    
    // check that the caller application doesn't get called on timeout but called on dialog terminated event when it is not implementing the new listener
    public void testDialogTimeoutDialogDeletedNotImplementedSipListenerExt() {
        
        try {
        	this.shootistProtocolObjs = new ProtocolObjects("shootist", "gov.nist", "udp", false,false, true);
            shootistNotImplementingSipListenerExt = new ShootistNotImplementingSipListenerExt(shootistProtocolObjs);
            SipProvider shootistProvider = shootistNotImplementingSipListenerExt.createSipProvider();

            this.shootmeProtocolObjs = new ProtocolObjects("shootme", "gov.nist", "udp", false,false, true);
            ((SipStackImpl)shootmeProtocolObjs.sipStack).setAggressiveCleanup(true);
            shootme = new Shootme(shootmeProtocolObjs);
            SipProvider shootmeProvider = shootme.createSipProvider();
           
            shootistNotImplementingSipListenerExt.init();
            providerTable.put(shootistProvider, shootistNotImplementingSipListenerExt);

            shootme.init();
            providerTable.put(shootmeProvider, shootme);
            shootistProvider.addSipListener(shootistNotImplementingSipListenerExt);
            shootmeProvider.addSipListener(shootme);

            getRiProtocolObjects().start();
            if (getTiProtocolObjects() != getRiProtocolObjects())
                getTiProtocolObjects().start();
            
            this.shootistNotImplementingSipListenerExt.sendInviteRequest();
            Thread.currentThread().sleep(TIMEOUT);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail("unexpected exception ");
        }

        if (!this.shootme.checkState() || !this.shootistNotImplementingSipListenerExt.checkState()) {
            fail("Test Failed - Didnt receive Dialog Timeout Event");
            return;
        }

        doTearDown(false);
        doSetUp();
    }
    
    // check that the apps don't get called on tiemout event if autodialog is true but get called on dialog terminated event
    public void testDialogTimeoutAutoDialog() {
        
        try {
        	this.shootistProtocolObjs = new ProtocolObjects("shootist", "gov.nist", "udp", true,false, true);
            shootist = new Shootist(shootistProtocolObjs);
            SipProvider shootistProvider = shootist.createSipProvider();

            this.shootmeProtocolObjs = new ProtocolObjects("shootme", "gov.nist", "udp", true,false, true);
            ((SipStackImpl)shootmeProtocolObjs.sipStack).setAggressiveCleanup(true);
            shootme = new Shootme(shootmeProtocolObjs);
            SipProvider shootmeProvider = shootme.createSipProvider();
           
            shootist.init();
            providerTable.put(shootistProvider, shootist);

            shootme.init();
            providerTable.put(shootmeProvider, shootme);
            shootistProvider.addSipListener(shootist);
            shootmeProvider.addSipListener(shootme);

            getRiProtocolObjects().start();
            if (getTiProtocolObjects() != getRiProtocolObjects())
                getTiProtocolObjects().start();
            
            this.shootist.sendInviteRequest();
            Thread.currentThread().sleep(TIMEOUT);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail("unexpected exception ");
        }

        if (!this.shootme.checkState() || !this.shootist.checkState()) {
            fail("Test Failed - received Dialog Timeout Event");
            return;
        }

        doTearDown(false);
        doSetUp();
    }
    
    // test checking that when the B2BUA flag is set to true, the app doesn't get called on dialog timeout but get called for dialog terminated 
    public void testDialogTimeoutB2BUABothCalled() {
        
        try {
        	this.shootistProtocolObjs = new ProtocolObjects("shootist", "gov.nist", "udp", false,true, true);
            shootist = new Shootist(shootistProtocolObjs);
            SipProvider shootistProvider = shootist.createSipProvider();

            this.shootmeProtocolObjs = new ProtocolObjects("shootme", "gov.nist", "udp", false,false, true);
            ((SipStackImpl)shootmeProtocolObjs.sipStack).setAggressiveCleanup(true);
            shootmeNotImplementingListener = new ShootmeNotImplementingListener(shootmeProtocolObjs);
            shootmeNotImplementingListener.setStateIsOk(true);
            SipProvider shootmeProvider = shootmeNotImplementingListener.createSipProvider();
           
            shootist.init();
            providerTable.put(shootistProvider, shootist);

            shootmeNotImplementingListener.init();
            providerTable.put(shootmeProvider, shootmeNotImplementingListener);
            shootistProvider.addSipListener(shootist);
            shootmeProvider.addSipListener(shootmeNotImplementingListener);

            getRiProtocolObjects().start();
            if (getTiProtocolObjects() != getRiProtocolObjects())
                getTiProtocolObjects().start();
            
            this.shootist.sendInviteRequest();
            Thread.currentThread().sleep(TIMEOUT);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail("unexpected exception ");
        }

        if (!this.shootmeNotImplementingListener.checkState()) {
        	fail("Test Failed - received Dialog Timeout Event");
        }
        		
        if(!this.shootist.checkState()) {
            fail("Test Failed - didn't received Dialog Terminated Event");
            return;
        }

        doTearDown(false);
        doSetUp();
    }

    public void tearDown() {
        doTearDown(true);

    }

    private void doTearDown(boolean definetly) {
        try {
            Thread.sleep(3000);
            // this.shootist.checkState();
            // this.shootme.checkState();
            shootmeProtocolObjs.destroy();
            shootistProtocolObjs.destroy();
            Thread.sleep(1000);
            this.providerTable.clear();
            if (definetly)
                logTestCompleted();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
