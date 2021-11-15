package examples.publish;
import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.*;

public class Publisher implements SipListener {
    SipProvider udpProvider;
    HeaderFactory headerFactory;
    MessageFactory messageFactory;

    class MyEventSource implements Runnable {
        private Notifier notifier;

        private EventHeader eventHeader;

        public MyEventSource(Notifier notifier, EventHeader eventHeader) {
            this.notifier = notifier;
            this.eventHeader = eventHeader;
        }

        public void run() {
            try {
                for (int i = 0; i < 1; i++) {

                    Thread.sleep(1000);
                    Request request = this.notifier.dialog
                            .createRequest(Request.NOTIFY);
                    SubscriptionStateHeader subscriptionState = headerFactory
                            .createSubscriptionStateHeader(SubscriptionStateHeader.ACTIVE);
                    request.addHeader(subscriptionState);
                    request.addHeader(eventHeader);
                    ClientTransaction ct = udpProvider
                            .getNewClientTransaction(request);
                    this.notifier.dialog.sendRequest(ct);

                }
                Request request = this.notifier.dialog
                        .createRequest(Request.NOTIFY);
                SubscriptionStateHeader subscriptionState = headerFactory
                        .createSubscriptionStateHeader(SubscriptionStateHeader.TERMINATED);
                request.addHeader(eventHeader);
                request.addHeader(subscriptionState);
                ClientTransaction ct = udpProvider
                        .getNewClientTransaction(request);
                this.notifier.dialog.sendRequest(ct);
            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(0);
            }
        }
    }

    public void processRequest(RequestEvent requestEvent) {
        // TODO Auto-generated method stub

    }

    public void processResponse(ResponseEvent responseEvent) {
        // TODO Auto-generated method stub

    }

    public void processTimeout(TimeoutEvent timeoutEvent) {
        // TODO Auto-generated method stub

    }

    public void processIOException(IOExceptionEvent exceptionEvent) {
        // TODO Auto-generated method stub

    }

    public void processTransactionTerminated(
            TransactionTerminatedEvent transactionTerminatedEvent) {
        // TODO Auto-generated method stub

    }

    public void processDialogTerminated(
            DialogTerminatedEvent dialogTerminatedEvent) {
        // TODO Auto-generated method stub

    }

    private Publisher() {

    }
    public static void main(String[] args) {

    }

}
