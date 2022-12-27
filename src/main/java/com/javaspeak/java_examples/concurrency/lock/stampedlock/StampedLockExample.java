package com.javaspeak.java_examples.concurrency.lock.stampedlock;

/**
 * @author John Dickerson - 2 Dec 2022
 */
public interface StampedLockExample {

    void put( String key, String value );


    public String tryReadWithOptimisticLockFirst( String key );
}
