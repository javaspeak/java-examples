package com.javaspeak.java_examples.concurrency.custom.map;

 
/**
 * @author John Dickerson - 15 Dec 2022
 */
public interface BlockOnGetMap<K, V> {

    V get( K k, Integer timeoutMilli );


    void put( K k, V v );


    Integer getEntryCount();
}
