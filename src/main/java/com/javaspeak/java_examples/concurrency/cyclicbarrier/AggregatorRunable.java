package com.javaspeak.java_examples.concurrency.cyclicbarrier;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

/**
 * @author John Dickerson - 23 Dec 2022
 */
public class AggregatorRunable implements Runnable {
	
	private ConcurrentLinkedQueue<Integer> results;
	private Integer sumOfResults = 0;
	private CountDownLatch counDownLatch;
	
	
	public AggregatorRunable(  
			ConcurrentLinkedQueue<Integer> results, 
			CountDownLatch counDownLatch ) {
		
		this.results = results;
		this.counDownLatch = counDownLatch;
	}

	@Override
	public void run() {
		
		System.out.println( Thread.currentThread().getName() + " is running the aggregate" );
		
		for ( Integer integer: results ) {
			
			sumOfResults = sumOfResults + integer;
		}
		
		counDownLatch.countDown();
	}

	
	public Integer getSumOfResults() {
	
		return sumOfResults;
	}
}
