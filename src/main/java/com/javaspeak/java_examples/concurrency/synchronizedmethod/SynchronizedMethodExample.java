package com.javaspeak.java_examples.concurrency.synchronizedmethod;

import java.util.concurrent.CountDownLatch;

/**
 * @author John Dickerson - 25 Nov 2022
 */
public interface SynchronizedMethodExample {

    public void doSomething( CountDownLatch latch );
}
