package com.javaspeak.java_examples.concurrency.lock.reentrantreadwritelock;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;



/**
 * @author John Dickerson - 29 Nov 2022
 */
public class ReentrantReadWriteLockExampleImpl implements ReentrantReadWriteLockExample {

    @Override
    public Integer addToScore(
            Score score,
            Integer incrementToAddToScore,
            ReentrantReadWriteLock lock ) {

        WriteLock writeLock = lock.writeLock();

        writeLock.lock();

        try {
            score.addToScore( incrementToAddToScore );
            return incrementToAddToScore;
        }
        finally {
            writeLock.unlock();
        }
    }


    @Override
    public Integer getScore(
            Score score,
            ReentrantReadWriteLock lock ) {

        ReadLock readLock = lock.readLock();

        readLock.lock();

        try {
            return score.getScore();
        }
        finally {
            readLock.unlock();
        }
    }
}
