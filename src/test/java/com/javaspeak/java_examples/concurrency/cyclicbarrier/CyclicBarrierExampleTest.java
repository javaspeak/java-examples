package com.javaspeak.java_examples.concurrency.cyclicbarrier;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author John Dickerson - 2 Dec 2022
 */
public class CyclicBarrierExampleTest {

    private AggregatorRunable aggregatorRunable;
    private WorkerRunnable[] workerRunnables;
    private CountDownLatch countDownLatch;
    
    private Integer[][] work = {
    		{ 1, 2, 3 },
    		{ 4, 5, 6 },
    		{ 7, 8, 9 },
    		{ 10, 11, 12 }
    };


    @BeforeClass
    public void setup() {
    	
    	countDownLatch = new CountDownLatch( 1 );

    	ConcurrentLinkedQueue<Integer> results = new ConcurrentLinkedQueue<>();
    	aggregatorRunable = new AggregatorRunable( results, countDownLatch );
    	
        CyclicBarrier cyclicBarrier = new CyclicBarrier( 4, aggregatorRunable );
        
        workerRunnables = new WorkerRunnable[4];
 
        for ( int i=0; i<4; i++ ) {
        	
        	workerRunnables[ i ] = new WorkerRunnable( 
            		work[i], 
        			cyclicBarrier, 
        			results );
        }
    }


    @Test 
    public void doTest() throws InterruptedException, ExecutionException {

    	ExecutorService executorService = Executors.newFixedThreadPool( 4 );
    	
    	for ( int i=0; i<4; i++ ) {
    		
    		executorService.submit( workerRunnables[i] );
    	}
    	
    	executorService.shutdown();
    	countDownLatch.await();
    	Integer expected = 0;
    	
    	for ( Integer[] workItem : work ) {
    		
    		for( Integer number: workItem ) {
    			
    			expected = expected + number;
    		}
    	}
    	
    	Integer actual = aggregatorRunable.getSumOfResults();
    	Assert.assertEquals(actual.intValue(), expected.intValue() );
    }


    public static void main( String[] args ) {

        TestListenerAdapter tla = new TestListenerAdapter();
        TestNG testng = new TestNG();
        testng.setTestClasses( new Class[] { CyclicBarrierExampleTest.class } );
        testng.addListener( tla );
        testng.run();
    }
}
