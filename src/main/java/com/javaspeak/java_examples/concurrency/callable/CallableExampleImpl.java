package com.javaspeak.java_examples.concurrency.callable;

 
/**
 * 
 * @author John Dickerson - 2 Dec 2022
 */
public class CallableExampleImpl implements CallableExample {

    private volatile Integer state = 0;

    @Override
    public Integer updateState( Integer amountToAdd ) {
        
        synchronized ( this ) {
            
            this.state = this.state + amountToAdd;
            return state;
        }
    }


    @Override
    public Integer getState() {

        return state;
    }
}
