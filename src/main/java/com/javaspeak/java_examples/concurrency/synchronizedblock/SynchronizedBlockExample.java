package com.javaspeak.java_examples.concurrency.synchronizedblock;

import java.util.concurrent.CountDownLatch;

/**
 * @author John Dickerson - 27 Nov 2022
 */
public interface SynchronizedBlockExample {

    void doSomething( Integer key, CountDownLatch latch );
}
