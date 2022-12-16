package com.javaspeak.java_examples.concurrency.custom.map.concurrent;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This ConcurrentMap does not use synchronized blocks for put(..), getValue(..) and remove(..) 
 * methods during the major part of its operation. Instead it uses CAS for put and remove.  
 * <p>
 * This ConcurrentMap does however have housekeeping operations which use hard synchronization 
 * (synchronized blocks) to do cleanups when certain thresholds are reached.
 * <p>
 * The ConcurrentMap bucket array is pre-populated with BucketImpl instances.
 * <p>
 * When an entry is being added, the hashCode() of the key is called and the resultant hash has a 
 * modulus performed on it against the number of buckets in the array:
 * <p>
 *     int hashCode = key.hashCode();<p>
 *     int arrayIndex = hashCode % bucketSize;
 * <p>
 * This maps a hashcode to an array index. The problem with this is that more than one hashcode may 
 * end up being assigned to the same arrayIndex.  For example "BB" and "Aa" have the same hashcode 
 * which would result in them being stored in a chain of Buckets at the same array index.
 * <p>
 *     hashCode = "BB".hashCode() = 2112<p>
 *     hashCode = "Aa".hashCode() = 2112
 * <p>
 * If the number of buckets are 33 both "BB" and "Aa" would need to be stored at index 0:
 * <p>
 *     int arrayIndex = 2112 % 33 = 0
 * <p>
 * If we are reducing bigger hashcode numbers to smaller array index numbers using the modulus, 
 * then it is very possible that different hashcodes will also produce the same modulus.
 * <p>
 * To get around this, further buckets can be chained to the bucket already in the arrayIndex.  
 * Each Bucket has a reference to a child bucket.  If a new key translates to an index in the bucket 
 * array that already has a KeyValuePair in it then a new bucket is created and added as a child 
 * to the last bucket in the chain.
 * <p>
 * The method, put(K key, V values) uses CAS functionality to update child bucket references in 
 * Buckets and to update a Bucket with a KeyValuePair.
 * <p>
 * If there is already a Bucket in the chain of Buckets connected to a certain array index that has 
 * a null KeyValuePair then that bucket will be used for a new keyValuePair.  If there are no null 
 * KeyValuePairs in the buckets chained to a certain index then a new Bucket is created at the end
 * of the bucket chain and the KeyValuePair set in that new Bucket.
 * <p>
 * The method, getValue( Key k ) uses CAS to read the child bucket and KeyValuePair
 * <p>
 * The method, remove( K k ) uses CAS functionality to update the KeyValuePair of a bucket with 
 * null.  The remove method does not delete the Bucket itself for that would require more than a 
 * simple CAAS operation which would affect performance.  What this means is that if objects are 
 * added to the map which have the same arrayIndex and they are then removed, we could end up
 * with buckets in the chain of buckets leading of an index which have no KeyValuePairs.  This would 
 * make get() operations slightly longer as we would needless be traversing elements in a linked 
 * list which had no values in them.
 * <p>
 * There is however a configuration parameter called "numberOfKeyValuePairsDeletedBeforeHouseKeeping" 
 * which on reaching the threshold triggers a house keeping process which removes all chained 
 * Buckets which have null KeyValuePairs in them.  If there is a bucket chain A -> B -> C then 
 * removing B would result in A being mapped to C:  A -> C.  During this house keeping exercise 
 * all calls to put(..) and getValue(..) block until the house keeping is finished.
 * <p>
 * The advantage of this implementation is that the cost of heavy weight synchronization is reduced 
 * to occasional house keeping exercises.  All other cases of synchronization use CAS which is 
 * more performant.
 * <p>
 * @author John Dickerson
 *
 * @param <K> Key wish to put in the Map
 * @param <V> Value wish to put in the Map
 */
/**
 * @author John Dickerson - 16 Dec 2022
 */
public class ConcurrentMapImpl<K, V> implements ConcurrentMap<K, V> {

    // Used for Spring Cleaning
    private Queue<Bucket<K, V>> bucketsToDeleteConcurrentLinkedQueue;
    private Bucket<K, V>[] buckets;
    private int bucketSize;
    private int numberOfKeyValuePairsDeletedBeforeHouseKeeping;
    private AtomicInteger numberDeleted = new AtomicInteger( 0 );

    private Object removeLock;

    private AtomicBoolean removeBoolean = new AtomicBoolean( false );
    private AtomicBoolean putBoolean = new AtomicBoolean( false );

    /**
     * Finds last Bucket in the Linked List.  Each index of the buckets array is pre-populated with 
     * a BucketImpl. Each Bucket in the array can have a child Bucket and so can the child Buckets 
     * have child buckets themselves.
     * <p>
     * This method finds the last Bucket in the linked list of buckets.
     *
     * @param bucket
     * 
     * @return last child bucket in chain of buckets
     */
    private Bucket<K, V> getLastBucket( Bucket<K, V> bucket ) {

        Bucket<K, V> childBucket = bucket.getChildBucket();

        if ( childBucket == null ) {

            return bucket;
        }
        else {

            return getLastBucket( childBucket );
        }
    }


    /**
     * A bucket can optional have a KeyValuePair.  If it has no KeyValuePair that means the bucket 
     * is not being used at the moment.  
     * <p>
     * If the bucket has no KeyValuePair, and it is in one of the child buckets of the bucket 
     * which is in the array of buckets then that means that the bucket is not being used at the 
     * moment and may be removed by house keeping if the number of deleted buckets passes the 
     * threshold defined by numberOfKeyValuePairsDeletedBeforeHouseKeeping
     * <p>
     * This method looks at the bucket passed in to see if it has a KeyValuePair for the key passed 
     * in. 
     * <p>
     * If it does not it will recursively look at the child buckets to see if they have a 
     * KeyValuePair corresponding to the Key.
     * <p>
     * If a KeyValuePair is found which corresponds to the key, the value for that KeyValuePair is 
     * returned. If no KeyValuePair is found null is returned.
     * <p>
     * @param bucket 
     *      Bucket to look for KeyValuePairs recursively until one is found which matches the key
     *
     * @param key 
     *      the key to macth KeyValuePairs with
     *      
     * @return value of matched KeyValuePair
     */
    private V getValue( Bucket<K, V> bucket, K key ) {

        KeyValuePair<K, V> keyValuePair = bucket.getKeyValuePair();

        if ( keyValuePair != null ) {

            if ( keyValuePair.getKey().equals( key ) ) {

                return keyValuePair.getValue();
            }
        }

        Bucket<K, V> childBucket = bucket.getChildBucket();

        if ( childBucket != null ) {

            return getValue( childBucket, key );
        }
        else {

            return null;
        }
    }


    /**
     * BucketImpl has a getChildBucket() method which can reference a child BucketImpl.  
     * <p>
     * Consequently Buckets can be chained.  The reason buckets are chained is that if one or more 
     * keys have the same hashcode or one or mode keys have the same modulus of the hashcode then 
     * they will be assigned to the same index in the array.  Obviously only one value can be 
     * assigned to one slot in the array so to get around this we allow the BucketImpls to be 
     * chained.
     * <p>
     * Searching through chained BucketImpls is slower than going directly to the array index, but 
     * if there are not too many of them then it does not impact performance significantly.
     * <p>
     * This method will look in the bucket and recursively at its children to find a bucket which 
     * has a KeyValuePair whose Key matches the key of this method.
     *
     * @param bucket 
     *      The parent bucket to look for the key in itself or its children
     *      
     * @param key 
     *      the key to look for in the buckets
     *      
     * @return The Bucket who has a KeyValuePair which matches the key of this method
     */
    private Bucket<K, V> getBucket( Bucket<K, V> bucket, K key ) {

        KeyValuePair<K, V> keyValuePair = bucket.getKeyValuePair();

        if ( keyValuePair != null ) {

            if ( keyValuePair.getKey().equals( key ) ) {

                return bucket;
            }
        }

        Bucket<K, V> childBucket = bucket.getChildBucket();

        if ( childBucket != null ) {

            return getBucket( childBucket, key );
        }
        else {

            return null;
        }
    }


    /**
     * This method looks at the buckets children recursively to find the first child bucket which 
     * has a null KeyValuePair.  This Bucket can then be reused instead of creating a new one and 
     * adding it on the end of the chain.
     *
     * @param bucket 
     *      Parent Bucket to look for the first child recursively that has a null KeyValuePair
     *      
     * @return Return first child Bucket that has a null KeyValuePair
     */
    private Bucket<K, V> getFreeChildBucket( Bucket<K, V> bucket ) {

        Bucket<K, V> childBucket = bucket.getChildBucket();

        if ( childBucket != null ) {

            if ( childBucket.getKeyValuePair() == null ) {

                return childBucket;
            }
            else {

                return getFreeChildBucket( childBucket );
            }
        }

        return null;
    }


    /**
     * If more buckets are deleted then the value denoted by 
     * numberOfKeyValuePairsDeletedBeforeHouseKeeping then a remove will trigger a spring cleaning 
     * exercise to tidy up chains of Buckets who have had an element deleted in them.
     * <p>
     * The reason this cleanup is necessary is that calling remove on a key nullifies the 
     * KeyWordPair of the bucket in question without deleting the bucket itself. The reason the 
     * bucket is not deleted itself is that deleting the bucket itself could necessitate non 
     * CAS hard synchronization as several bucket references could need to be updated in the chain 
     * in one atomic transaction.
     * <p>
     * Imagine the chain array:
     * <p>
     *     Index 0 : Aa:Apple ==> [null keyValuePair] ==> BB:Bat ==> null bucket
     * <p>
     * Deleting the bucket denoted [null keyValuePair] would involve changing Aa:Apple so it 
     * references BB:Bat. If another thread removed BB:Bat and another thread added a new bucket 
     * to [null keyValuePair] it would be missed from the chain. This is why hard synchronization 
     * would be required to block any threads putting or getting while a remove was taking place.
     * <p>
     * To involve the expensive hard sychronization on the remove we instead just nullify the 
     * KeyValuePair and add a reference to the nullified bucket in a 
     * bucketsToDeleteConcurrentLinkedQueue so that later on we can block all threads while we do a 
     * proper clean up.
     * <p>
     * The principle is not too dissimilar to garbage collection
     */
    private void springClean() {

        Bucket<K, V> bucketToRemove;

        while ( ( bucketToRemove = bucketsToDeleteConcurrentLinkedQueue.poll() ) != null ) {

            if ( bucketToRemove.getKeyValuePair() == null ) {

                removeBucket( bucketToRemove );
            }
        }
    }


    /**
     * This method is called by springClean().  It called when put and get methods are blocked 
     * by springClean(). springClean() does not occur on each removal of a key and value from the 
     * ConcurrentMap. The springClean() occurs after a certain number of removes have been called 
     * denoted by numberOfKeyValuePairsDeletedBeforeHouseKeeping.
     * <p>
     * The springClean() means that put, get and remove methods all occur without hard core 
     * synchronization most of the time. Instead CAS is used ensure state changes are visible 
     * across all threads.
     *
     * @param bucketToRemove
     */
    private void removeBucket( Bucket<K, V> bucketToRemove ) {

        Bucket<K, V> parentBucket = buckets[bucketToRemove.getIndex()];

        if ( parentBucket != null ) {

            if ( parentBucket.getKeyValuePair() == null ) {

                Bucket<K, V> nextBucket = parentBucket.getChildBucket();

                if ( nextBucket != null ) {

                    buckets[parentBucket.getIndex()] = nextBucket;
                }
            }
        }

        while ( parentBucket != null ) {

            Bucket<K, V> nextBucket = parentBucket.getChildBucket();

            if ( nextBucket != null ) {

                if ( nextBucket.getKeyValuePair() == null ) {

                    Bucket<K, V> afterNextBucket = nextBucket.getChildBucket();
                    parentBucket.setChildBucket( afterNextBucket );
                }
            }

            parentBucket = nextBucket;
        }
    }


    /**
     * Constructor
     *
     * @param bucketSize  
     *      The number of slots in this Map.  Note that if we try and place more items in the map 
     *      then there are slots chaining will for sure occur which can degrade performance if 
     *      there is significant chaining.  Best using a prime number for the bucket size to 
     *      minimise the amount of chaining.
     *
     * @param numberOfKeyValuePairsDeletedBeforeHouseKeeping 
     *      This threshold determines how many KeyValuePairs can be deleted before houseKeeping is
     *      triggered.  House Keeping removes child buckets which have had their KeyValuePairs 
     *      nullified.
     */
    @SuppressWarnings( "unchecked" )
    public ConcurrentMapImpl(
            int bucketSize, int numberOfKeyValuePairsDeletedBeforeHouseKeeping ) {

        this.bucketSize = bucketSize;

        this.numberOfKeyValuePairsDeletedBeforeHouseKeeping =
                numberOfKeyValuePairsDeletedBeforeHouseKeeping;

        buckets = new Bucket[bucketSize];

        bucketsToDeleteConcurrentLinkedQueue = new ConcurrentLinkedQueue<Bucket<K, V>>();

        for ( int i = 0; i < bucketSize; i++ ) {

            buckets[i] = new BucketImpl<K, V>( i );
        }
    }


    /**
     *  Constructor
     */
    public ConcurrentMapImpl() {

        this( 33, 10 );
    }


    private void putValue( K key, V value ) {

        putBoolean.set( true );

        int hashCode = key.hashCode();
        int arrayIndex = hashCode % bucketSize;

        while ( true ) {

            KeyValuePair<K, V> keyValuePair = buckets[arrayIndex].getKeyValuePair();

            // If the parent bucket in the index has a Null KeyValuePair then set the new 
            // KeyValuePair in it
            if ( keyValuePair == null ) {

                if ( buckets[arrayIndex].setKeyValuePairAtomically(
                        keyValuePair, new KeyValuePair<K, V>( key, value ) ) ) {

                    break;
                }
            }
            else {
                // Navigate down the chain of buckets starting at the child of the parent bucket 
                // at the index and return the first bucket that has a null KeyValuePair.  If
                // a Bucket is returned that means we are reusing an old bucket.
                Bucket<K, V> freeBucket = getFreeChildBucket( buckets[arrayIndex] );

                if ( freeBucket != null ) {

                    if ( freeBucket.setKeyValuePairAtomically(
                            null, new KeyValuePair<K, V>( key, value ) ) ) {

                        numberDeleted.getAndDecrement();
                        break;
                    }
                }
                else {
                    // We did not find any unused buckets so we get the last bucket in the chain 
                    // and add a new bucket to it.
                    Bucket<K, V> lastBucket = getLastBucket( buckets[arrayIndex] );

                    if ( lastBucket.setChildBucketAtomically(
                            null, new BucketImpl<K, V>(
                                    key, value, arrayIndex ) ) ) {

                        break;
                    }
                }
            }
        }

        putBoolean.set( false );
    }


    @Override
    public void put( K key, V value ) {

        while ( removeBoolean.get() ) {

            synchronized ( removeLock ) {

                try {
                    removeLock.wait();
                    putValue( key, value );
                }
                catch ( InterruptedException e ) {
                    // do nothing
                }
            }
        }

        putValue( key, value );
    }


    @Override
    public V getValue( K key ) {

        while ( true ) {

            while ( removeBoolean.get() ) {

                synchronized ( removeLock ) {

                    try {
                        this.wait();
                        break;
                    }
                    catch ( InterruptedException e ) {

                    }
                }
            }

            int hashCode = key.hashCode();
            int arrayIndex = hashCode % bucketSize;

            V v = getValue( buckets[arrayIndex], key );

            if ( !removeBoolean.get() ) {

                return v;
            }
        }
    }


    @Override
    public void remove( K key ) {

        int hashCode = key.hashCode();
        int arrayIndex = hashCode % bucketSize;

        while ( true ) {

            Bucket<K, V> bucket = getBucket( buckets[arrayIndex], key );

            if ( bucket == null ) {

                return;
            }

            if ( bucket.setKeyValuePairAtomically( bucket.getKeyValuePair(), null ) ) {

                bucketsToDeleteConcurrentLinkedQueue.add( bucket );
                break;
            }
        }

        if ( numberDeleted.getAndIncrement()
                + 2 > numberOfKeyValuePairsDeletedBeforeHouseKeeping ) {

            synchronized ( this ) {

                if ( numberDeleted.getAndIncrement()
                        + 2 > numberOfKeyValuePairsDeletedBeforeHouseKeeping ) {

                    try {
                        removeBoolean.getAndSet( true );

                        springClean();
                        numberDeleted.set( 0 );
                    }
                    finally {
                        removeBoolean.getAndSet( false );
                    }
                }
            }
        }

    }


    @Override
    public String debug() {

        StringBuilder sb = new StringBuilder( "\n" );

        for ( int i = 0; i < buckets.length; i++ ) {

            sb.append( "Index " ).append( i ).append( " : " );
            sb.append( buckets[i].toString() ).append( "\n" );
        }

        return sb.toString();
    }
}
