package com.javaspeak.java_examples.concurrency.countdownlatch;

import java.util.concurrent.CountDownLatch;

/**
 * @author John Dickerson - 2 Dec 2022
 */
public interface CountdownLatchExample {

    void updateSharedState( CountDownLatch countdownLatch, int amountToIncrement );


    int getSharedState();


    void doubleSharedStateAfterOtherThreadsComplete( CountDownLatch countdownLatch )
            throws InterruptedException;
}
