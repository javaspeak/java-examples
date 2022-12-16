package com.javaspeak.java_examples.concurrency.countdownlatch;

import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author John Dickerson - 2 Dec 2022
 */
public class CountDownLatchExampleImpl implements CountDownLatchExample {

    private Logger logger = LoggerFactory.getLogger( CountDownLatchExampleImpl.class );

    private volatile int sharedState = 0;

    @Override
    public void updateSharedState(
            CountDownLatch countDownLatch,
            int amountToIncrement ) {
        
        logger.info( "Starting method updateSharedState of thread: " +
                Thread.currentThread().getName() );

        synchronized ( this ) {

            this.sharedState = this.sharedState + amountToIncrement;
            countDownLatch.countDown();
        }

        logger.info( "Completed method updateSharedState  of thread: " +
                Thread.currentThread().getName() );

        logger.info( " Still to count down: " + countDownLatch.getCount() );
    }


    @Override
    public int getSharedState() {

        return sharedState;
    }


    @Override
    public void doubleSharedStateAfterOtherThreadsComplete(
            CountDownLatch countDownLatch ) throws InterruptedException {

        logger.info( "Starting method doubleSharedStateAfterOtherThreadsComplete in thread: "
                + Thread.currentThread().getName() );

        // this method blocks until the other threads have counted down the countDownLatch
        countDownLatch.await();


        synchronized ( this ) {
            this.sharedState = this.sharedState * 2;

            logger.info( "sharedState = " + sharedState );
        }

        logger.info( "Completed method doubleSharedStateAfterOtherThreadsComplete in thread: "
                + Thread.currentThread().getName() );
    }
}
