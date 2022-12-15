package com.javaspeak.java_examples.concurrency.custom.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This class is to be used as follows:
 * 
 * We have a request with an asynchronous response. We want to block waiting for the asynchronous 
 * response.
 * 
 * Here are the steps:
 * 
 * (i) We create an ID.
 * 
 * (ii) We make the request to a remote service and pass it the ID.  The response from the remote
 *      service is asynchronous and it will pass back the ID.
 *      
 * (iii) We call BlockOnGetMap.get( ID ) and block on it.
 * 
 * (iv) Another thread handles the asynchronous response, gets the ID and puts it in the 
 *     BlockOnGetMap.
 * 
 * (v) The method that was blocking on BlockOnGetMap now gets its value.
 * 
 * The call doing the get then removes the key and value from the underlying maps.
 * 
 * Note that a synchronized block is synchronizing on a monitor.  It cares about the reference
 * of the object. That is why we have a second map so we can dig out the monitor.
 * 
 * @author John Dickerson - 15 Dec 2022
 */
public class BlockOnGetMapImpl<K, V> implements BlockOnGetMap<K, V> {

    private Map<K, V> valueMap = new HashMap<>();
    private Map<K, K> keyMap = new HashMap<>();
    private Map<K, Long> nanoTimeMap = new HashMap<>();
    private ReadWriteLock readWriteLockKey = new ReentrantReadWriteLock();
    private Long maxMilliSecondsInMap;


    public BlockOnGetMapImpl( Long maxMilliSecondsInMap ) {

        this.maxMilliSecondsInMap = maxMilliSecondsInMap;
    }


    private K getMonitorKey( K k ) {

        Lock writeLockKey = readWriteLockKey.writeLock();
        K monitor = null;

        try {
            writeLockKey.lock();
            monitor = keyMap.get( k );

            if ( monitor == null ) {

                monitor = k;
                keyMap.put( k, monitor );
            }

            return monitor;
        }
        finally {
            writeLockKey.unlock();
        }
    }


    private void removeKey( K k ) {

        Lock writeLockKey = readWriteLockKey.writeLock();

        try {
            writeLockKey.lock();
            keyMap.remove( k );
            nanoTimeMap.remove( k );
        }
        finally {

            writeLockKey.unlock();
        }
    }


    private void purgeExpiredKeys() {

        Long now = System.nanoTime();
        List<K> keysToDelete = new ArrayList<>();
        
        for ( Entry<K, Long> entry : nanoTimeMap.entrySet() ) {

            Long diff = ( now - entry.getValue() ) / 1000000;

            if ( diff > maxMilliSecondsInMap ) {

                keysToDelete.add( entry.getKey() );
            }
        }

        for ( K k : keysToDelete ) {

            K valueKey = keyMap.get( k );
            valueMap.remove( valueKey );
            keyMap.remove( k );
            nanoTimeMap.remove( k );
            System.out.println( "Purged key: " + k );
        }
    }


    @Override
    public V get( K k, Integer timeoutMilli ) {

        K monitor = getMonitorKey( k );
        V value = null;

        synchronized ( monitor ) {

            try {
                value = valueMap.get( monitor );

                if ( value != null ) {

                    return value;
                }

                k.wait( timeoutMilli );
                value = valueMap.get( monitor );
                return value;
            }
            catch ( InterruptedException e ) {

                System.out.println( "Interrupted" );
                return null;
            }
            finally {
                valueMap.remove( monitor );
                removeKey( k );
            }
        }
    }


    @Override
    public void put( K k, V v ) {

        K monitor = null;
        Lock writeLockKey = readWriteLockKey.writeLock();

        try {
            writeLockKey.lock();
            purgeExpiredKeys();

            monitor = keyMap.get( k );

            if ( monitor == null ) {

                monitor = k;
                keyMap.put( k, monitor );
                nanoTimeMap.put( k, System.nanoTime() );
            }
        }
        finally {

            writeLockKey.unlock();
        }

        synchronized ( monitor ) {

            valueMap.put( monitor, v );
            monitor.notifyAll();
        }
    }


    @Override
    public Integer getEntryCount() {

        return valueMap.size();
    }
}
