package com.javaspeak.java_examples.concurrency.semaphore;

import java.util.concurrent.ExecutionException;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author John Dickerson - 2 Dec 2022
 */
public class SemaphoreExampleTest {

    private BlockingThreadPool threadPool;

    @BeforeClass
    public void setup() {

    	threadPool = new BlockingThreadPoolImpl( 4 );
    }


    @Test
    public void doTest() throws InterruptedException, ExecutionException {

    	threadPool.executeRunnable(
    	
    			new Runnable() {

					@Override
					public void run() {
						
						try {
							Thread.sleep( 1000 );
							System.out.println( "Done" );
						}
						catch( InterruptedException e ) {
							
						}
					}
    			}
        );
    	
    	// Our thread is in process so we have one thread less available
    	Assert.assertEquals( threadPool.numberFreeThreads(), 3 );
    	
    	Thread.sleep( 1100 );
    	
    	// The thread has completed so it should have been given back to the pool
    	Assert.assertEquals( threadPool.numberFreeThreads(), 4 );
    }
}
