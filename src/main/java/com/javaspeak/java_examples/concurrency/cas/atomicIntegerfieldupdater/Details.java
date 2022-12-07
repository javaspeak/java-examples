package com.javaspeak.java_examples.concurrency.cas.atomicIntegerfieldupdater;

 
/**
 * @author John Dickerson - 5 Dec 2022
 */
public class Details {

    volatile int numberTimesInvoked;

    public int getNumberTimesInvoked() {

        return numberTimesInvoked;
    }


    public void setNumberTimesInvoked( int numberTimesInvoked ) {

        this.numberTimesInvoked = numberTimesInvoked;
    }
}
