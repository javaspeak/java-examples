package com.javaspeak.java_examples.concurrency.callable;

/**
 * 
 * @author John Dickerson - 2 Dec 2022
 */
public interface CallableExample {

    Integer updateState( Integer amountToAdd );


    Integer getState();
}
