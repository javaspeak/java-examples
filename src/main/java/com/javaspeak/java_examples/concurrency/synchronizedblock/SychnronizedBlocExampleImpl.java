package com.javaspeak.java_examples.concurrency.synchronizedblock;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * @author John Dickerson - 27 Nov 2022
 */
public class SychnronizedBlocExampleImpl implements SynchronizedBlockExample {

    class MyLock {

    }

    private static Map<Integer, MyLock> map = new HashMap<>();

    @Override
    public void doSomething( Integer key, CountDownLatch latch ) {

        MyLock valueforFineGrainedSyncronization = null;

        // Here we are are sychronizing on the reference of the instance of this class. 
        // A syncronized method syncronizes on the "this" instance.  With a block you need to
        // specify what you are synchronizing on.
        //
        // Here all threads will block on getting the key from the map.  However it is a short
        // lived and fast operation.
        synchronized ( this ) {
            
            valueforFineGrainedSyncronization = map.get( key );

            if ( valueforFineGrainedSyncronization == null ) {

                valueforFineGrainedSyncronization = new MyLock();
                map.put( key, valueforFineGrainedSyncronization );
            }
        }

        // Note this is synchronized by reference.  The equals and hashcode methods are not used
        // in a sychronized block.
        //
        // Here we are synchronizing on something more specific that the "this".  It means that 
        // threads will only be blocked if they are dealing with exactly the same key (i.e. same
        // instance of the key).
        //
        // This is why we created a map above, we want to ensure that we are synchronizing on the
        // same reference.
        synchronized ( valueforFineGrainedSyncronization ) {

            // some long running job.
            try {
                Thread.sleep( 1000 );
                latch.countDown();
            }
            catch ( InterruptedException e ) {

            }
        }
    }

}
