package com.javaspeak.java_examples.concurrency.cas.atomicStampedReference;

import java.util.concurrent.atomic.AtomicStampedReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author John Dickerson - 7 Dec 2022
 */
public class AtomicStampedReferenceUpdater {

    private static Logger logger = LoggerFactory.getLogger( AtomicStampedReferenceUpdater.class );

    private Long time = Long.decode( "1" );

    private AtomicStampedReference<Long> atomicStampedReference =
            new AtomicStampedReference<Long>( time, 1 );

    public Long setTime( Long time ) {

        int[] oldStamp = new int[1];

        while ( true ) {

            Long oldTime = atomicStampedReference.get( oldStamp );
            int newStamp = oldStamp[0] + 1;

            if ( atomicStampedReference.compareAndSet( oldTime, time, oldStamp[0], newStamp ) ) {

                return oldTime;
            }
        }
    }


    public Long getTime( int[] updatedStamp ) {

        int[] stamp = new int[1];
        Long time = atomicStampedReference.get( stamp );
        updatedStamp[0] = stamp[0];
        return time;
    }


    public static void main( String[] args ) {

        AtomicStampedReferenceUpdater atomicStampedReferenceUpdater =
                new AtomicStampedReferenceUpdater();

        Long oldTime = atomicStampedReferenceUpdater.setTime( Long.valueOf( 1000 ) );
        logger.info( "oldTime = " + oldTime );

        int[] version = new int[1];
        Long newTime = atomicStampedReferenceUpdater.getTime( version );
        logger.info( "newTime = " + newTime + ", version: " + version[0] );
    }
}
