package com.javaspeak.java_examples.concurrency.custom.map.concurrent;

/**
 * KeyValuePair encapsulates a Key Value pair.
 *
 * It is set atomically into BucketImpl
 *
 * @author John Dickerson - 16 Dec 2022
 *
 * @param <K> Key of ConcurrentMap
 * @param <V> Value of ConcurrentMap
 */
public class KeyValuePair<K, V> {

    private final K key;
    private final V value;


    public KeyValuePair( K key, V value ) {

        this.key = key;
        this.value = value;
    }


    public K getKey() {

        return this.key;
    }


    public V getValue() {

        return this.value;
    }


    @Override
    public String toString() {

        return "KeyValuePair [key=" + key + ", value=" + value + "]";
    }
}
