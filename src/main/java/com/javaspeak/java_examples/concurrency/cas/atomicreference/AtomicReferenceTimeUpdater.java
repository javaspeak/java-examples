package com.javaspeak.java_examples.concurrency.cas.atomicreference;

import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author John Dickerson - 7 Dec 2022
 */
public class AtomicReferenceTimeUpdater {

    private static Logger logger = LoggerFactory.getLogger( AtomicReferenceTimeUpdater.class );

    class Time {

        final private long time;
        final private int offset;

        public Time( long time, int offset ) {

            this.time = time;
            this.offset = offset;
        }


        public long getTime() {

            return time;
        }


        public long getOffset() {

            return offset;
        }
    }

    // This class allows several variables to be set and compared
    private AtomicReference<Time> timeAtomicReference =
            new AtomicReference<Time>( new Time( 0, 0 ) );

    // in this implementation we are setting more than value
    public Time updateTime( long time, int offset ) {

        // returns old value
        Time oldTime =
                timeAtomicReference.getAndSet( new Time( time, offset ) );
        /**
         * Above getAndSet method is same as
         *
        while ( true ) {
           Time previousTime = timeAtomicReference.get();
           if ( timeAtomicReference.compareAndSet(
                   previousTime, new Time( time, offset ) ) ){
               break;
           }
        }
        */
        return oldTime;
    }


    public Time getTime() {

        return timeAtomicReference.get();
    }


    public static void main( String[] args ) {

        Random random = new Random( 123456789 );

        // gets number between 1 and 10 inclusive
        long time = random.nextInt( 9 ) + 1;

        AtomicReferenceTimeUpdater atomicReferenceTimeUpdater = new AtomicReferenceTimeUpdater();

        // Normally many threads would be calling the updateTime( method );
        Time oldTime = atomicReferenceTimeUpdater.updateTime( time, -4 );
        logger.info( oldTime.getTime() + " : " + oldTime.getOffset() );

        Time newTime = atomicReferenceTimeUpdater.getTime();
        logger.info( newTime.getTime() + " : " + newTime.getOffset() );
    }
}
