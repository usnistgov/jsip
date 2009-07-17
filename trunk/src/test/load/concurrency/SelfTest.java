package test.load.concurrency;

import javax.sip.SipProvider;

public class SelfTest {


    public static void main(String[] args) throws Exception {
        ProtocolObjects.init("shootist", true);
        if ( args.length == 0 ) Shootist.NDIALOGS = 10000;
        else Shootist.NDIALOGS = Integer.parseInt(args[0]);
        Shootist.addressFactory = ProtocolObjects.addressFactory;
        Shootist.messageFactory = ProtocolObjects.messageFactory;
        Shootist.headerFactory = ProtocolObjects.headerFactory;
        Shootist.sipStack = ProtocolObjects.sipStack;
        Shootist.transport = ProtocolObjects.transport;
        Shootist shootist = new Shootist();
        shootist.createProvider(shootist);

        ProtocolObjects.init("shootme", true);
        Shootme.addressFactory = ProtocolObjects.addressFactory;
        Shootme.messageFactory = ProtocolObjects.messageFactory;
        Shootme.headerFactory = ProtocolObjects.headerFactory;
        Shootme.sipStack = ProtocolObjects.sipStack;
        Shootme.transport = ProtocolObjects.transport;
        Shootme shootme = new Shootme();
        SipProvider sipProvider = shootme.createSipProvider();
        sipProvider.addSipListener(shootme);

        shootist.start = System.currentTimeMillis();
        for (int i = 0; i < Shootist.NDIALOGS; i++) {
            Thread.sleep(3);
            shootist.sendInvite();
        }
    }

}
