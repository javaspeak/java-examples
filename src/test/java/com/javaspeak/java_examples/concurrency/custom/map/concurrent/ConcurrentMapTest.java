package com.javaspeak.java_examples.concurrency.custom.map.concurrent;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author John Dickerson - 16 Dec 2022
 */
public class ConcurrentMapTest {

    private static final String EL = "\n";
    private ConcurrentMap<Long, String> concurrentMap;

    @BeforeClass
    private void setUp() {

        int bucketSize = 4;
        int numberOfKeyValuePairsDeletedBeforeHouseKeeping = 2;

        concurrentMap =
                new ConcurrentMapImpl<Long, String>(
                        bucketSize,
                        numberOfKeyValuePairsDeletedBeforeHouseKeeping );
    }


    @Test
    public void putOnDifferentIndexTest() {

        concurrentMap.put( 0l, "aa" );
        concurrentMap.put( 1l, "bb" );

        String expected =
                EL +
                        "Index 0 : KeyValuePair [key=0, value=aa] ==> null bucket" + EL +
                        "Index 1 : KeyValuePair [key=1, value=bb] ==> null bucket" + EL +
                        "Index 2 : null keyValuePair ==> null bucket" + EL +
                        "Index 3 : null keyValuePair ==> null bucket" + EL;

        String debug = concurrentMap.debug();
        System.out.println( debug );
        Assert.assertEquals( debug, expected );

        Assert.assertEquals( concurrentMap.getValue( 0l ), "aa" );
        Assert.assertEquals( concurrentMap.getValue( 1l ), "bb" );
    }


    @Test
    public void putOnSameIndexTest() {

        concurrentMap.put( 0l, "aa" );
        concurrentMap.put( 4l, "bb" );
        
        String expected = 
                EL +
                "Index 0 : KeyValuePair [key=0, value=aa] ==> KeyValuePair [key=4, value=bb] ==> null bucket" + EL +
                        "Index 1 : null keyValuePair ==> null bucket" + EL +
                        "Index 2 : null keyValuePair ==> null bucket" + EL +
                        "Index 3 : null keyValuePair ==> null bucket" + EL;

        String debug = concurrentMap.debug();
        System.out.println( debug );
        Assert.assertEquals( debug, expected );

        Assert.assertEquals( concurrentMap.getValue( 0l ), "aa" );
        Assert.assertEquals( concurrentMap.getValue( 4l ), "bb" );
    }


    @Test
    public void putOnSameIndexAndDeleteFirstTest() {

        concurrentMap.put( 0l, "aa" );
        concurrentMap.put( 4l, "bb" );
        concurrentMap.remove( 0l );

        String expected =
                EL +
                        "Index 0 : null keyValuePair ==> KeyValuePair [key=4, value=bb] ==> null bucket"
                        + EL +
                        "Index 1 : null keyValuePair ==> null bucket" + EL +
                        "Index 2 : null keyValuePair ==> null bucket" + EL +
                        "Index 3 : null keyValuePair ==> null bucket" + EL;

        String debug = concurrentMap.debug();
        System.out.println( debug );
        Assert.assertEquals( debug, expected );

        Assert.assertNull( concurrentMap.getValue( 0l ) );
        Assert.assertEquals( concurrentMap.getValue( 4l ), "bb" );
    }


    @Test
    public void triggerSpringCleanTest() {

        concurrentMap.put( 0l, "aa" );
        concurrentMap.put( 4l, "bb" );
        concurrentMap.remove( 0l );
        concurrentMap.remove( 4l );

        String expected =
                EL +
                        "Index 0 : null keyValuePair ==> null bucket" + EL +
                        "Index 1 : null keyValuePair ==> null bucket" + EL +
                        "Index 2 : null keyValuePair ==> null bucket" + EL +
                        "Index 3 : null keyValuePair ==> null bucket" + EL;

        String debug = concurrentMap.debug();
        System.out.println( debug );
        Assert.assertEquals( debug, expected );

        Assert.assertNull( concurrentMap.getValue( 0l ) );
        Assert.assertNull( concurrentMap.getValue( 4l ) );
    }


    public static void main( String[] args ) {

        TestListenerAdapter tla = new TestListenerAdapter();
        TestNG testng = new TestNG();
        testng.setTestClasses( new Class[] { ConcurrentMapTest.class } );
        testng.addListener( tla );
        testng.run();
    }
}
