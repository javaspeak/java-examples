package com.javaspeak.java_examples.concurrency.lock.reentrantlockwithcondition;

/**
 * @author John Dickerson - 27 Dec 2022
 */
public interface ReentrantLockWithCondition {

    void pushToDeque( String item ) throws InterruptedException;


    String popFromDeque() throws InterruptedException;


    int getSizeOfDeque();
}
