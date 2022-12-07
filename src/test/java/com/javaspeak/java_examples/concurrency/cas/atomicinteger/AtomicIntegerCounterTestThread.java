package com.javaspeak.java_examples.concurrency.cas.atomicinteger;

import java.util.concurrent.CountDownLatch;

/**
 * @author John Dickerson - 5 Dec 2022
 */
public class AtomicIntegerCounterTestThread extends Thread {

    private int maxNumber;
    private int[] numbers;
    private CountDownLatch countDownLatch;
    private AtomicIntegerCounter atomicIntegerCounter;

    public AtomicIntegerCounterTestThread(
            CountDownLatch countDownLatch,
            AtomicIntegerCounter atomicIntegerCounter, int maxNumber ) {

        this.countDownLatch = countDownLatch;
        this.atomicIntegerCounter = atomicIntegerCounter;
        this.maxNumber = maxNumber;
        this.numbers = new int[maxNumber];

        for ( int i = 0; i < numbers.length; i++ ) {

            numbers[i] = -1;
        }
    }


    public int[] getNumbers() {

        return numbers;
    }


    @Override
    public void run() {

        int currentNumber;

        while ( true ) {

            currentNumber = atomicIntegerCounter.incrementByOne();

            if ( currentNumber >= maxNumber ) {

                break;
            }
            else {

                numbers[currentNumber] = currentNumber;
            }
        }

        countDownLatch.countDown();
    }
}
