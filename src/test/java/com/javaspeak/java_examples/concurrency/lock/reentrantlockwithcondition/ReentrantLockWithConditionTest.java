package com.javaspeak.java_examples.concurrency.lock.reentrantlockwithcondition;

import java.util.concurrent.CountDownLatch;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author John Dickerson - 24 Dec 2022
 */
public class ReentrantLockWithConditionTest {

    private static int CAPACITY = 5;
    private ReentrantLockWithCondition reentrantLockWithCondition;

    @BeforeClass
    public void setUp() {

        reentrantLockWithCondition = new ReentrantLockWithConditionImpl( CAPACITY );
    }


    @Test
    public void doTest() throws Exception {

        reentrantLockWithCondition.pushToDeque( "1" );
        Assert.assertEquals( reentrantLockWithCondition.getSizeOfDeque(), 1 );

        reentrantLockWithCondition.pushToDeque( "2" );
        Assert.assertEquals( reentrantLockWithCondition.getSizeOfDeque(), 2 );

        reentrantLockWithCondition.pushToDeque( "3" );
        Assert.assertEquals( reentrantLockWithCondition.getSizeOfDeque(), 3 );

        reentrantLockWithCondition.pushToDeque( "4" );
        Assert.assertEquals( reentrantLockWithCondition.getSizeOfDeque(), 4 );

        reentrantLockWithCondition.pushToDeque( "5" );
        Assert.assertEquals( reentrantLockWithCondition.getSizeOfDeque(), 5 );

        CountDownLatch countDownLatch = new CountDownLatch( 1 );

        Thread thread = new Thread() {

            public void run() {

                try {
                    // capacity has been reached so it will block on push
                    reentrantLockWithCondition.pushToDeque( "6" );
                    countDownLatch.countDown();
                    System.out.println( "Pushed" );
                }
                catch ( InterruptedException e ) {

                }
            }
        };

        thread.start();

        // waiting for Thread to start and block on push method.
        Thread.sleep( 1000 );

        // this will free up the blocked thread
        reentrantLockWithCondition.popFromDeque();
        System.out.println( "Popped" );

        countDownLatch.await();
        Assert.assertEquals( reentrantLockWithCondition.getSizeOfDeque(), 5 );
    }


    public static void main( String[] args ) {

        TestListenerAdapter tla = new TestListenerAdapter();
        TestNG testng = new TestNG();
        testng.setTestClasses( new Class[] { ReentrantLockWithConditionTest.class } );
        testng.addListener( tla );
        testng.run();
    }
}
