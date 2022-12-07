package com.javaspeak.java_examples.concurrency.countdownlatch;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author John Dickerson - 2 Dec 2022
 */
public class CountdownLatchExampleTest {

    private CountdownLatchExample example;
    private Logger logger = LoggerFactory.getLogger( CountdownLatchExampleTest.class );

    @BeforeClass
    public void setup() {

        example = new CountdownLatchExampleImpl();
    }

    @Test
    public void doTest() throws InterruptedException, ExecutionException {

        // Even though we have 5 threads we will be reusing some
        ExecutorService executorService = Executors.newFixedThreadPool( 3 );
        Runnable[] runnables = new Runnable[4];
        CountDownLatch countDownLatch = new CountDownLatch( 4 );
        int[] dataToSet = new int[] { 1, 2, 3, 4 };
        
        // This thread waits on the countDownLatch for the other 4 threads to add the data.
        // It then doubles the state.
        Runnable awaitingRunnable = new Runnable() {

            public void run() {

                try {
                    // internally this thread blocks until the other 4 threads have all called
                    // countDownLatch.countDown(). It then doubles the value of the state.
                    example.doubleSharedStateAfterOtherThreadsComplete( countDownLatch );
                }
                catch ( InterruptedException e ) {

                    logger.error( "Interrupted" );
                }
            }
        };

        executorService.submit( awaitingRunnable );

        // these four threads add the values in dataToSet
        for ( int i = 0; i < runnables.length; i++ ) {

            final int dataToIncrement = dataToSet[i];

            runnables[i] = new Runnable() {

                public void run() {

                    // internally calls countDownLatch.countDown()
                    example.updateSharedState( countDownLatch, dataToIncrement );
                }
            };
        }

        for ( Runnable runnable : runnables ) {

            executorService.submit( runnable );
        }

        // this prevents other tasks being run but lets existing scheduled ones finish
        // If this is not called before awaitTermination awairw
        executorService.shutdown();

        boolean terminatedWithoutTimeout =
                executorService.awaitTermination( 5, TimeUnit.SECONDS );

        Assert.assertTrue( terminatedWithoutTimeout );

        // value should be ( 1 + 2 + 3 + 4 ) * 2.
        Assert.assertEquals( example.getSharedState(), 20 );
    }


    public static void main( String[] args ) {

        TestListenerAdapter tla = new TestListenerAdapter();
        TestNG testng = new TestNG();
        testng.setTestClasses( new Class[] { CountdownLatchExampleTest.class } );
        testng.addListener( tla );
        testng.run();
    }
}
