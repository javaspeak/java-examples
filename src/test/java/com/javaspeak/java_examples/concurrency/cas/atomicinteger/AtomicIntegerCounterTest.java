package com.javaspeak.java_examples.concurrency.cas.atomicinteger;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

/**
 * @author John Dickerson - 5 Dec 2022
 */
public class AtomicIntegerCounterTest {

    private AtomicIntegerCounterTestThread[] runThreads(
            int numberThreads, int maxNumber )
            throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch( numberThreads );
        AtomicIntegerCounter atomicIntegerCounter = new AtomicIntegerCounter();

        AtomicIntegerCounterTestThread[] threads =
                new AtomicIntegerCounterTestThread[numberThreads];

        for ( int i = 0; i < numberThreads; i++ ) {

            threads[i] =
                    new AtomicIntegerCounterTestThread(
                            countDownLatch, atomicIntegerCounter, maxNumber );
        }

        for ( int i = 0; i < threads.length; i++ ) {

            threads[i].start();
        }

        countDownLatch.await( 20, TimeUnit.SECONDS );
        return threads;
    }
    

    /**
     * Tests multiple threads incrementing the AtomicIntegerCounter counter at the same time.
     * <p>
     * Whenever a thread updates the counter it saves the value in ints own array at the index of 
     * the array which is the same as the value.
     * <p>
     * Initially the array is initialised with -1 in each index.
     * <p>
     * after running the array could look like:
     * <p>
     * 1  2  -1  4  5 -1 -1 7
     * <p>
     * After all the threads have finished running the values in the arrays of all threads are 
     * compared to make sure that no more than one thread had the same value in its array (ie there 
     * are no duplicates).
     * <p>
     * This was achieved by reading the values from the arrays and putting them in a TreeSet.  
     * Before adding an entry to the set it was checked whether there was a value in the set 
     * already.  If there was the same value in the set already that means we have a duplicate and 
     * the test fails.
     * <p>
     * Finally the test checks whether there are any values that were skipped in the Set. if there 
     * are gaps the test fails.
     *
     * @throws InterruptedException
     */
    @Test
    public void testUpdateCounter() throws InterruptedException {

        int numberThreads = 3;
        int maxNumber = 1000;

        // blocks until all threads complete
        AtomicIntegerCounterTestThread[] testThreads = runThreads( numberThreads, maxNumber );

        int[] numbers;
        Set<Integer> numberSet = new TreeSet<Integer>();

        for ( int i = 0; i < testThreads.length; i++ ) {

            numbers = testThreads[i].getNumbers();

            for ( int j = 0; j < numbers.length; j++ ) {

                if ( numbers[j] != -1 ) {

                    if ( numberSet.contains( numbers[j] ) ) {

                        Assert.fail( "duplicate" );
                    }
                    else {
                        numberSet.add( numbers[j] );
                    }
                }
            }
        }

        int numberElements = numberSet.size();

        Iterator<Integer> numberIterator = numberSet.iterator();

        // check for gaps
        for ( int i = 0; i < numberElements; i++ ) {

            Assert.assertEquals( numberIterator.next(), Integer.valueOf( i ) );
        }
    }


    public static void main( String[] args ) {

        TestListenerAdapter tla = new TestListenerAdapter();
        TestNG testng = new TestNG();
        testng.setTestClasses( new Class[] { AtomicIntegerCounterTest.class } );
        testng.addListener( tla );
        testng.run();
    }
}
