package com.javaspeak.java_examples.concurrency.cas.atomicIntegerfieldupdater;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.LockSupport;

/**
 * @author John Dickerson - 5 Dec 2022
 */
public class ReaderThread extends Thread {

    private CountDownLatch countDownLatch;
    private Details details;
    private int maxNumberCalls;

    public ReaderThread(
            CountDownLatch countDownLatch, int maxNumberCalls ) {

        details = new Details();
        this.countDownLatch = countDownLatch;
        this.maxNumberCalls = maxNumberCalls;
    }


    public Details getDetails() {

        return this.details;
    }


    @Override
    public void run() {

        while ( true ) {

            if ( details.getNumberTimesInvoked() >= maxNumberCalls ) {

                break;
            }

            LockSupport.parkNanos( 1 );
        }
        countDownLatch.countDown();
    }
}
