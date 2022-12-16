package com.javaspeak.java_examples.concurrency.countdownlatch;

import java.util.concurrent.CountDownLatch;

/**
 * @author John Dickerson - 2 Dec 2022
 */
public interface CountDownLatchExample {

    void updateSharedState( CountDownLatch countDownLatch, int amountToIncrement );


    int getSharedState();


    void doubleSharedStateAfterOtherThreadsComplete( CountDownLatch countDownLatch )
            throws InterruptedException;
}
