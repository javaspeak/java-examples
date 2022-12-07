package com.javaspeak.java_examples.concurrency.lock.reentrantlock;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author John Dickerson - 28 Nov 2022
 */
public class ReentrantLockExampleImpl implements ReentrantLockExample {


    @Override
    public Integer perform(
            Score score,
            Integer incrementToAddToScore,
            ReentrantLock lock ) {

        lock.lock();

        try {
            score.addToScore( incrementToAddToScore );
            return incrementToAddToScore;
        }
        finally {
            lock.unlock();
        }
    }
}
