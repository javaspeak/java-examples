package com.javaspeak.java_examples.concurrency.synchronizedmethod;

import java.util.concurrent.CountDownLatch;

/**
 * @author John Dickerson - 25 Nov 2022
 */
public class SynchronizedMethodExampleImpl implements SynchronizedMethodExample {


    // See SynchronizedMethodExampleTest for prove that only one thread can access a synchronized method
    // at a time.  Also See SynchronizedMethodExampleTest for explanation of what the CountDownLatch is
    // about.
    @Override
    public synchronized void doSomething( CountDownLatch latch ) {
        
        try {
            Thread.sleep( 1000 );
            latch.countDown();
        }
        catch ( InterruptedException e ) {

            // do nothing
        }
    }
}
