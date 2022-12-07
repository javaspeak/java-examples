package com.javaspeak.java_examples.concurrency.lock.reentrantlock;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author John Dickerson - 29 Nov 2022
 */
public interface ReentrantLockExample {

    Integer perform(
            Score score,
            Integer incrementToAddToScore,
            ReentrantLock lock );
}
