package com.javaspeak.java_examples.concurrency.lock.reentrantreadwritelock;

import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * @author John Dickerson - 29 Nov 2022
 */
public interface ReentrantReadWriteLockExample {

    Integer addToScore(
            Score score,
            Integer incrementToAddToScore,
            ReentrantReadWriteLock lock );


    Integer getScore(
            Score score,
            ReentrantReadWriteLock lock );
}
