package com.javaspeak.java_examples.concurrency.cas.atomicinteger;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author John Dickerson - 5 Dec 2022
 */
public class AtomicIntegerCounter {

    // The disadvantage of this approach is that there is a cost for wrapping
    // a volatile int in a AtomicInteger. If you want to keep using a
    // volatile int then you should use AtomicIntegerFieldUpdater instead
    private AtomicInteger atomicInteger = new AtomicInteger( -1 );
    
    /**
     * @return counter value after it has been incremented by one
     */
    public int incrementByOne() {

        // This operation is atomic ( x = x + 1 )
        return atomicInteger.addAndGet( 1 );
    }


    public static void main( String[] args ) {

        AtomicIntegerCounter atomicIntegerCounter = new AtomicIntegerCounter();
        // This call would ordinarily be called by another thread
        System.out.println( atomicIntegerCounter.incrementByOne() );
    }
}
