package com.javaspeak.java_examples.concurrency.semaphore;

 /**
 * @author John Dickerson - 23 Dec 2022
 */
public interface BlockingThreadPool {

	void executeRunnable( Runnable runnable ); 
	
	int numberFreeThreads();
}
