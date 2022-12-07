package com.javaspeak.java_examples.concurrency.synchronizedblock;

import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author John Dickerson - 27 Nov 2022
 */
public class SynchronizedBlockExampleTest {

    private Logger logger = LoggerFactory.getLogger( SynchronizedBlockExampleTest.class );

    private SynchronizedBlockExample example;

    @BeforeClass
    public void setup() {

        example = new SychnronizedBlocExampleImpl();
    }

    @Test
    public void doTest() throws InterruptedException {

        CountDownLatch latch = new CountDownLatch( 6 );

        Thread[] threads = new Thread[6];
        final Integer[] keys = new Integer[] { 1, 1, 2, 2, 3, 3 };
        

        for ( int i = 0; i < 6; i++ ) {
            
            final Integer key = keys[i];

            threads[i] = new Thread() {

                public void run() {

                    example.doSomething( key, latch );
                }
            };
        }

        long start = System.nanoTime();

        // starting all the threads
        for ( Thread thread : threads ) {

            thread.start();
        }

        latch.await();
        long end = System.nanoTime();

        // The start value of the first nanoTime() is arbitrary - it is not based on an absolute
        // value like Calendar.  So what we care about is the difference in nanoTime between start
        // and end. There are 1000 million nano seconds in 1 second.
        long diffNano = end - start;
        long diffSeconds = diffNano / 1000000000;
        logger.info( "diffSeconds: " + diffSeconds );

        // We have 6 threads but only 3 different monitors. A monitor is the instance reference
        // used in the synchronized block.  We have 2 threads per monitor so the time taken is 2
        // seconds.
        Assert.assertTrue( diffSeconds == 2 );
    }


    public static void main( String[] args ) {

        TestListenerAdapter tla = new TestListenerAdapter();
        TestNG testng = new TestNG();
        testng.setTestClasses( new Class[] { SynchronizedBlockExampleTest.class } );
        testng.addListener( tla );
        testng.run();
    }
}
