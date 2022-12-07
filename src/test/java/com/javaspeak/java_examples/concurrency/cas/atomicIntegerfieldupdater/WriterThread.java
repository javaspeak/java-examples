package com.javaspeak.java_examples.concurrency.cas.atomicIntegerfieldupdater;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author John Dickerson - 5 Dec 2022
 */
public class WriterThread extends Thread {

    private int maxNumberCalls;
    private ReaderThread[] readerThreads;
    private AtomicIntegerFieldUpdaterCounter atomicIntegerFieldUpdaterCounter;

    private Map<Integer, List<Integer>> readerCountByReaderMap =
            new HashMap<Integer, List<Integer>>();

    public WriterThread(
            int maxNumberCalls, ReaderThread[] readerThreads,
            AtomicIntegerFieldUpdaterCounter atomicIntegerFieldUpdaterCounter ) {

        this.maxNumberCalls = maxNumberCalls;
        this.readerThreads = readerThreads;
        this.atomicIntegerFieldUpdaterCounter = atomicIntegerFieldUpdaterCounter;

        for ( int i = 0; i < readerThreads.length; i++ ) {

            readerCountByReaderMap.put( Integer.valueOf( i ), new ArrayList<Integer>() );
        }
    }


    public Map<Integer, List<Integer>> getReaderCountByReaderMap() {

        return readerCountByReaderMap;
    }


    @Override
    public void run() {

        int count;
        List<Integer> counts;

        for ( int i = 0; i < maxNumberCalls; i++ ) {

            for ( int j = 0; j < readerThreads.length; j++ ) {

                counts = readerCountByReaderMap.get( Integer.valueOf( j ) );
                count = atomicIntegerFieldUpdaterCounter.addOne( readerThreads[j].getDetails() );
                counts.add( count );
            }
        }
    }
}
