package com.javaspeak.java_examples.concurrency.lock.reentrantlock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author John Dickerson - 28 Nov 2022
 */
public class ReentrantLockExampleTest {

    private ReentrantLockExample example;

    @BeforeClass
    public void setup() {

        example = new ReentrantLockExampleImpl();
    }


    @Test
    public void doTest() throws InterruptedException, ExecutionException {

        ReentrantLock lock = new ReentrantLock();
        
        Score sharedScore = new Score();
        ExecutorService executorService = Executors.newFixedThreadPool( 4 );
        
        List<Callable<Integer>> callables = new ArrayList<>();
        Integer[] numbers = new Integer[] { 1, 1, 1, 1 };

        for ( int i = 0; i < 4; i++ ) {
            
            // needs to be a final as we are passing it into the perform method.
            final Integer incrementToAdd = numbers[i];

            // Callable is like a Runnable or Thread except it returns a value.
            Callable<Integer> callable  = new Callable<Integer>() {
                
                public Integer call() throws Exception {
                    
                    return example.perform( sharedScore, incrementToAdd, lock );
                }
            };
            
            callables.add( callable );
        }
        
        // The futures return straight away but you will notice that below we block on the get 
        // method of each future.  The future returns the value returned by the callable.
        List<Future<Integer>> futures = executorService.invokeAll( callables );

        int count = 0;

        for ( Future<Integer> future : futures ) {

            // the future.get() method blocks until the Callable has finished executing and 
            // returned its value
            count = count + future.get();
        }

        Assert.assertEquals( count, 4 );
        Assert.assertEquals( sharedScore.getScore().intValue(), 4 );
    }


    public static void main( String[] args ) {

        TestListenerAdapter tla = new TestListenerAdapter();
        TestNG testng = new TestNG();
        testng.setTestClasses( new Class[] { ReentrantLockExampleTest.class } );
        testng.addListener( tla );
        testng.run();
    }
}
