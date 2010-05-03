package test.load.subsnotify;

public class SubsNotify {

    /**
     * @param args
     */
    public static void main(String[] args) {
        Notifier notifier = new Notifier();
        Subscriber subscriber = new Subscriber();
        notifier.init();
        subscriber.init();

    }

}
