package com.javaspeak.java_examples.concurrency.lock.reentrantlock;

 
/**
 * @author John Dickerson - 29 Nov 2022
 */
public class Score {

    private Integer score = 0;

    public void addToScore( Integer number ) {

        this.score = score + number;
    }


    public Integer getScore() {

        return score;
    }
}
