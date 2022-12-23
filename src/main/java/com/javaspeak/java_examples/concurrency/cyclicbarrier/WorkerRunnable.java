package com.javaspeak.java_examples.concurrency.cyclicbarrier;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CyclicBarrier;

/**
 * @author John Dickerson - 23 Dec 2022
 */
public class WorkerRunnable implements Runnable {

	private Integer[] numbersToAdd;
	private CyclicBarrier cyclicBarrier;
	private ConcurrentLinkedQueue<Integer> results;
	
	public WorkerRunnable( 
			Integer[] numbersToAdd, 
			CyclicBarrier cyclicBarrier, 
			ConcurrentLinkedQueue<Integer> results ) {
		
		this.numbersToAdd = numbersToAdd;
		this.cyclicBarrier = cyclicBarrier;
		this.results = results;
	}
	
	@Override
	public void run() {
		
		try {
			Integer sum = 0;
			
			for ( Integer number: numbersToAdd ) {
				
				sum = sum + number;
			}
			
			System.out.println( 
					Thread.currentThread().getName() + " finished work. Adding sum " + 
							sum + " to results" );
			
			results.add( sum );
			
			// this thread will be blocked by the await until all threads have reached await and
			// the AggregatorRunnable has been run
			cyclicBarrier.await();
		}
		catch( BrokenBarrierException | InterruptedException e ) {
			
		}
	}
}
