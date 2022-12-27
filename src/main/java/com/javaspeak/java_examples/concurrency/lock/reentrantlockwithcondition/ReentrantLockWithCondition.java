package com.javaspeak.java_examples.concurrency.lock.reentrantlockwithcondition;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author John Dickerson - 26 Dec 2022
 */
public class ReentrantLockWithCondition {

    private Deque<String> deque = new LinkedList<>();
    private int capacity = 5;

    private ReentrantLock lock = new ReentrantLock();
    private Condition dequeEmptyCondition = lock.newCondition();
    private Condition dequeFullCondition = lock.newCondition();

    public ReentrantLockWithCondition( int capacity ) {

        this.capacity = 5;
    }


    public int getSizeOfDeque() {

        return deque.size();
    }


    public void pushToDeque( String item ) throws InterruptedException {

        try {
            lock.lock();

            while ( deque.size() == capacity ) {

                dequeFullCondition.await();
            }

            deque.push( item );
            dequeEmptyCondition.signalAll();
        }
        finally {

            lock.unlock();
        }
    }


    public String popFromDeque() throws InterruptedException {

        try {
            lock.lock();

            while ( deque.size() == 0 ) {

                dequeEmptyCondition.await();
            }

            return deque.removeFirst();
        }
        finally {

            dequeFullCondition.signalAll();
            lock.unlock();
        }
    }
}
