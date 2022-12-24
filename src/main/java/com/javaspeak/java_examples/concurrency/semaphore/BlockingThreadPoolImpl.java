package com.javaspeak.java_examples.concurrency.semaphore;

import java.util.concurrent.Semaphore;

/**
 * This Thread Pool will block on executeRunnable(..) for a thread to become available
 * 
 * @author John Dickerson - 23 Dec 2022
 */
public class BlockingThreadPoolImpl implements BlockingThreadPool {
	
	private Semaphore semaphore;

	public BlockingThreadPoolImpl( Integer numberThreads ) {
		
		semaphore = new Semaphore( numberThreads );
	}
	
	@Override
	public int numberFreeThreads() {
		
		// returns number of available permits
		return semaphore.availablePermits();
	}
	
	
	@Override
	public void executeRunnable( Runnable runnable ) {
		
		try {
			// This will block if we have used all the permits
			semaphore.acquire();
			
			Thread thread = new Thread() {	
			
				public void run() {
					
					try {
						runnable.run();
					}
					finally {
						// the permit is added back to the semaphore
						semaphore.release();
					}
				}
			};

			thread.start();
		}
		catch( InterruptedException e ) {
			
		}
	}
}
