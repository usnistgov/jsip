package gov.nist.core;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread factory which names threads by "pool-<basename>-thread-n".
 * This is a replacement for Executors.defaultThreadFactory() to be able to identify pools.
 * Optionally a delegate thread factory can be given which creates the Thread
 * object itself, if no delegate has been given, Executors.defaultThreadFactory is used.
 * @author Alerant
 *
 */
public class NamingThreadFactory implements ThreadFactory {
    private ThreadFactory delegate;
    private String baseName;
    private AtomicInteger index;

    public NamingThreadFactory(String baseName) {
        this(baseName, null);
    }

    public NamingThreadFactory(String baseName, ThreadFactory delegate) {
        this.baseName = baseName;
        this.delegate = delegate;
        if (this.delegate == null) {
            this.delegate = Executors.defaultThreadFactory();
        }
        this.index = new AtomicInteger(1);
    }

    @Override
    public Thread newThread(Runnable r) {
        String name = "pool-" + baseName + "-thread-" + index.getAndIncrement();
        Thread ret = delegate.newThread(r);
        ret.setName(name);
        return ret;
    }
}
