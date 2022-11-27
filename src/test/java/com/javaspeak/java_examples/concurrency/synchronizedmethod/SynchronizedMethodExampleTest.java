package com.javaspeak.java_examples.concurrency.synchronizedmethod;

import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

/**
 * @author John Dickerson - 25 Nov 2022
 */
public class SynchronizedMethodExampleTest {

    private Logger logger = LoggerFactory.getLogger( SynchronizedMethodExampleTest.class );

    @Test
    public void doTest() throws InterruptedException {

        SynchronizedMethodExample example = new SynchronizedMethodExampleImpl();
        CountDownLatch latch = new CountDownLatch( 3 );
        
        Thread[] threads = new Thread[3];
        
        for ( int i = 0; i < 3; i++ ) {
        
            threads[i] = new Thread() {
                
                public void run() {
                    
                    // this doSomething method has a 1 second sleep in it and also is marked
                    // synchronized. synchronized means only 1 thread can call this method
                    // at a time.  The other threads will block until the thread running it has
                    // finished.  Then one of the next waiting threads will grab it.  Here we
                    // have 3 threads so we expect the total time taken to be more or equal to 3
                    // seconds
                    //
                    // We added a CountDownLatch with 3 counts in.  After doing the 1 second sleep
                    // each thread calls countDown.  The latch.await() method below waits until
                    // all threads have called countDown
                    example.doSomething( latch );
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

        // As we have 3 threads and each one sleeps 1 second and we have the syncronized keyword
        // on the method this means that at anyone time only one thread can run the doSomething()
        // method. This means we expect the time taken to be equal or greater than 3 seconds.
        Assert.assertTrue( diffSeconds >= 3 );
    }


    public static void main( String[] args ) {

        TestListenerAdapter tla = new TestListenerAdapter();
        TestNG testng = new TestNG();
        testng.setTestClasses( new Class[] { SynchronizedMethodExampleTest.class } );
        testng.addListener( tla );
        testng.run();
    }
}
