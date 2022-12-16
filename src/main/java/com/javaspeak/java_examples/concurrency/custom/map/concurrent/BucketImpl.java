package com.javaspeak.java_examples.concurrency.custom.map.concurrent;

import java.util.concurrent.atomic.AtomicReference;


/**
 * In the ConcurrentMap the buckets array is initialized with instances of BucketImpl.
 *
 * When an entry is being added to the ConcurrentMap, the hashCode() of the key is called and the 
 * resultant hash has a modulus performed on it against the number of buckets in the array:
 *
 *     int hashCode = key.hashCode();
 *     int arrayIndex = hashCode % bucketSize;
 *
 * This maps a hashcode to an array index.
 *
 * The problem with this is that more than one hashcode may end up being assigned to the same 
 * arrayIndex.  For example "BB" and "Aa" have the same hashcode which would result in them being 
 * stored in a chain of Buckets at the same array index.
 *
 *     hashCode = "BB".hashCode() = 2112
 *     hashCode = "Aa".hashCode() = 2112
 *
 * If the number of buckets are 33 both "BB" and "Aa" would need to be stored at index 0:
 *
 *     int arrayIndex = 2112 % 33 = 0
 *
 * If the size of the hashcodes is much bigger than the number of slots in the array we will also 
 * expect the modulus to give the same array index for different hashcodes.  If many more keys are 
 * added to the map then there are array slots then we will expect the number of collisions to be 
 * even greater as many different keys will have the same index.
 *
 * To get around this, further buckets can be chained to the bucket already in the arrayIndex.  
 * Each Bucket has a reference to a child bucket.  If a new key translates to an index in the bucket 
 * array that already has a KeyValuePair in it then a new bucket is created and added as a child 
 * to the last bucket in the chain.
 *
 * Note that these chains make looking up a key slower so it is good to initialize the ConcurrentMap 
 * with a size which is bigger than the expected number of items you are putting in it.
 *
 * @author John Dickerson - 16 Dec 2022
 *
 * @param <K> Key of ConcurrentMap
 * @param <V> Value of ConcurrentMap
 */
public class BucketImpl<K, V> implements Bucket<K, V> {

    private int index;
    private AtomicReference<KeyValuePair<K, V>> keyValuePairReference;
    private AtomicReference<Bucket<K, V>> childBucketReference = new AtomicReference<>();

    /**
     * Constructor
     */
    public BucketImpl() {

    }


    /**
     * Constructor
     *
     * @param index Index of buckets array that this Bucket resides in
     */
    public BucketImpl( int index ) {

        this.index = index;
        keyValuePairReference = new AtomicReference<>();
    }


    /**
     * @param key 
     *  Key of ConcurrentMap
     *  
     * @param value 
     *  Value of ConcurrentMap
     *  
     * @param index 
     *  Index of buckets array that this Bucket resides in
     */
    public BucketImpl( K key, V value, int index ) {

        this.index = index;
        KeyValuePair<K, V> keyValuePair = new KeyValuePair<>( key, value );
        keyValuePairReference = new AtomicReference<>( keyValuePair );

    }


    @Override
    public KeyValuePair<K, V> getKeyValuePair() {

        return keyValuePairReference.get();
    }


    @Override
    public boolean setKeyValuePairAtomically(
            KeyValuePair<K, V> oldKeyValuePair,
            KeyValuePair<K, V> newKeyValuePair ) {

        return keyValuePairReference.compareAndSet(
                oldKeyValuePair, newKeyValuePair );
    }


    @Override
    public int getIndex() {

        return index;
    }


    @Override
    public Bucket<K, V> getChildBucket() {

        return childBucketReference.get();
    }


    @Override
    public boolean setChildBucketAtomically(
            Bucket<K, V> oldChildBucket, Bucket<K, V> newChildBucket ) {

        return childBucketReference.compareAndSet(
                oldChildBucket, newChildBucket );
    }


    @Override
    public void setChildBucket( Bucket<K, V> childBucket ) {

        childBucketReference.set( childBucket );
    }


    public String toString() {

        StringBuilder sb = new StringBuilder();

        KeyValuePair<K, V> keyValuePair = keyValuePairReference.get();
        sb.append( keyValuePair == null ? "null keyValuePair" : keyValuePair.toString() );
        sb.append( " ==> " );

        Bucket<K, V> childBucket = childBucketReference.get();
        sb.append( childBucket == null ? "null bucket" : childBucket.toString() );
        return sb.toString();
    }
}
