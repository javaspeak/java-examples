package com.javaspeak.java_examples.concurrency.lock.reentrantreadwritelock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author John Dickerson - 29 Nov 2022
 */
public class ReentrantReadWriteLockExampleTest {

    private ReentrantReadWriteLockExample example;

    @BeforeClass
    public void setup() {

        example = new ReentrantReadWriteLockExampleImpl();
    }

    @Test
    public void doTest() throws InterruptedException, ExecutionException {

        ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

        Score sharedScore = new Score();
        ExecutorService executorService = Executors.newFixedThreadPool( 8 );

        List<Callable<Integer>> callables = new ArrayList<>();
        Integer[] numbers = new Integer[] { 1, 1, 1, 1 };

        for ( int i = 0; i < 4; i++ ) {

            // needs to be a final as we are passing it into the perform method.
            final Integer incrementToAdd = numbers[i];

            // Callable is like a Runnable or Thread except it returns a value.
            Callable<Integer> callable = new Callable<Integer>() {

                public Integer call() throws Exception {

                    return example.addToScore(
                            sharedScore, incrementToAdd, readWriteLock );
                }
            };

            callables.add( callable );
        }

        for ( int i = 0; i < 4; i++ ) {

            Callable<Integer> callable = new Callable<Integer>() {

                public Integer call() throws Exception {

                    return example.getScore(
                            sharedScore, readWriteLock );
                }
            };

            callables.add( callable );
        }

        List<Future<Integer>> futures = executorService.invokeAll( callables );

        for ( Future<Integer> future : futures ) {

            future.get();
        }

        Assert.assertEquals( sharedScore.getScore().intValue(), 4 );
    }


    public static void main( String[] args ) {

        TestListenerAdapter tla = new TestListenerAdapter();
        TestNG testng = new TestNG();
        testng.setTestClasses( new Class[] { ReentrantReadWriteLockExampleTest.class } );
        testng.addListener( tla );
        testng.run();
    }
}
