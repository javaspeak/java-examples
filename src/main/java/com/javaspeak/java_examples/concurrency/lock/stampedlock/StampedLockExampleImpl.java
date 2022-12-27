package com.javaspeak.java_examples.concurrency.lock.stampedlock;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.StampedLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author John Dickerson - 2 Dec 2022
 */
public class StampedLockExampleImpl implements StampedLockExample {

    private Logger logger = LoggerFactory.getLogger( StampedLockExample.class );

    private Map<String, String> map = new HashMap<>();
    private StampedLock lock = new StampedLock();

    @Override
    public void put( String key, String value ) {

        long stamp = lock.writeLock();

        try {
            map.put( key, value );
        }
        finally {
            lock.unlockWrite( stamp );
        }
    }


    @Override
    public String tryReadWithOptimisticLockFirst( String key ) {

        long stamp = lock.tryOptimisticRead();

        // if this method takes a long time we could be out of date by the time we call
        // lock.validate(..) - however a get is pretty fast so very unlikely.
        String value = map.get( key );

        if ( !lock.validate( stamp ) ) {

            logger.info( "optimistic read failed. Downgrading to exclusive read lock" );
            stamp = lock.readLock();

            try {
                return map.get( key );
            }
            finally {
                lock.unlock( stamp );
            }
        }

        logger.info( "Optimistic Lock suceeded" );

        return value;
    }
}
