package com.javaspeak.java_examples.concurrency.custom.map.concurrent;

 
/**
 * @author John Dickerson - 16 Dec 2022
 */
public interface Bucket<K, V> {

    /**
     * Gets the KeyValuePair.  A KeyValuePair contains a Key and Value
     *
     * @return KeyValuePair
     */
    public KeyValuePair<K, V> getKeyValuePair();


    /**
     * Sets the KeyValuePair.  A KeyValuePair contains a Key and Value
     *
     * @param oldKeyValuePair
     * @param newKeyValuePair
     * 
     * @return true if managed to set, else false
     */
    public boolean setKeyValuePairAtomically(
            KeyValuePair<K, V> oldKeyValuePair, KeyValuePair<K, V> newKeyValuePair );


    /**
     * A Bucket can have child Buckets if more than key is assigned the same array index.
     *
     * @return child Bucket of the parent Bucket
     */
    public Bucket<K, V> getChildBucket();


    /**
     * Sets a child Bucket to the Bucket
     *
     * @param oldChildBucket
     * @param newChildBucket
     * 
     * @return true if managed to set, else false
     */
    public boolean setChildBucketAtomically(
            Bucket<K, V> oldChildBucket, Bucket<K, V> newChildBucket );


    /**
     * Sets a child Bucket to the Bucket
     *
     * @param childBucket
     */
    public void setChildBucket( Bucket<K, V> childBucket );


    /**
     * Gets the array index which this Bucket is at. Note the several buckets may have the same 
     * index if they are chained.
     *
     * @return the index of this Bucket
     */
    public int getIndex();
}
