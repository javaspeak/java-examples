package com.javaspeak.java_examples.concurrency.custom.map.concurrent;

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
 * Each Bucket has a reference to a child bucket.  If a new key translates to an index in the 
 * bucket array that already has a KeyValuePair in it then a new bucket is created and added as a 
 * child to the last bucket in the chain.
 * <p>
 * The method, put(K key, V values) uses CAS functionality to update child bucket references in 
 * Buckets and to update a Bucket with a KeyValuePair.
 * <p>
 * If there is already a Bucket in the chain of Buckets connected to a certain array index that has 
 * a null KeyValuePair then that bucket will be used for a new keyValuePair.  If there are no 
 * null KeyValuePairs in the buckets chained to a certain index then a new Bucket is created at the 
 * end of the bucket chain and the KeyValuePair set in that new Bucket.
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
 * to occasional house keeping exercises.  All other cases of synchronization use CAS which is more 
 * performant.
 * <p>
 * @author John Dickerson
 *
 * @param <K> Key wish to put in the Map
 * @param <V> Value wish to put in the Map
 */
/**
 * @author John Dickerson - 16 Dec 2022
 */
public interface ConcurrentMap<K, V> {

    /**
     * This method uses CAS instead of heavy synchronization
     *
     * @param key Key wish to put in the Map
     * 
     * @param value Value wish to put in the Map
     */
    public void put( K key, V value );


    /**
     * This method does not require synchronization
     *
     * @param key Key wish to retrieve the value with
     * 
     * @return V 
     *      value
     */
    public V getValue( K key );


    /**
     * This method uses CAS instead of heavy synchronization
     *
     * @param key 
     *      Key wish to remove the value with
     */
    public void remove( K key );


    /**
     * Debugging the contents of the internal array and its chained buckets
     *
     * @return a String showing the contents of the internal array and its chained buckets
     */
    public String debug();
}
