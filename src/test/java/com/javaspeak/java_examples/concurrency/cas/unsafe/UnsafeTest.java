package com.javaspeak.java_examples.concurrency.cas.unsafe;

import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import sun.misc.Unsafe;

/**
* See:
*
* http://classpath.sourcearchive.com/documentation/0.97.1/
*      classsun_1_1misc_1_1Unsafe_55497e7c323175975118fb67ed9496ea.html#
*          55497e7c323175975118fb67ed9496ea
*
* @author John Dickerson
*/
public class UnsafeTest {

    public Logger logger = LoggerFactory.getLogger( UnsafeTest.class );
    private Unsafe unsafe;

    @BeforeClass
    public void setUp() throws Exception {

        Class unsafeClass = Class.forName( "sun.misc.Unsafe" );
        Field unsafeField = unsafeClass.getDeclaredField( "theUnsafe" );
        unsafeField.setAccessible( true );
        unsafe = ( Unsafe )unsafeField.get( unsafeClass );
    }


    @Test
    public void testAllocateMemoryAndGetAddress() throws Exception {

        long value = 12345;
        byte size = 1;
        long allocateMemory = unsafe.allocateMemory( size );
        unsafe.putAddress( allocateMemory, value );
        long readValue = unsafe.getAddress( allocateMemory );
        org.junit.Assert.assertEquals( value, readValue );
        logger.debug( "read value : " + readValue );
    }


    /**
    * This class tests using Unsafe.java to update a certain index of an array
    * <p>
    * The Unsafe.java method, compareAndSwapLong is used in an environment where multiple threads 
    * are trying to update the same index of an array
    *
    * @throws Exception
    */
    @Test
    public void testCompareAndSwapLongInArray() throws Exception {

        // Create an array
        long[] longArray = new long[15];

        // Fill array up with sample numbers
        for ( int i = 0; i < longArray.length; i++ ) {

            longArray[i] = i;
        }

        // Decide which slot of the array we are going to update the value with
        int indexToUpdate = 5;

        // Decode on the value we will update the slot in the array with
        long valueToInsert = longArray[indexToUpdate] + 1;

        // Check that we are not attempting to update a slot outside the array
        if ( indexToUpdate < 0 || indexToUpdate >= longArray.length ) {

            throw new IndexOutOfBoundsException( "index " + indexToUpdate );
        }

        // returns the logical offset of where the indexes start in the physical memory
        int base = unsafe.arrayBaseOffset( long[].class );

        // The docs say:
        //
        // "Returns the scale factor used for addressing elements of the
        // supplied array class. Where a suitable scale factor can not be
        // returned (e.g. for primitive types), zero should be returned.
        // The returned value can be used with arrayBaseOffset to access
        // elements of the class"
        //
        // The way I understand the scale is that it is a number to multiple
        // by the index of the slot you want to read or write to.  When added
        // to the base you get get the logical address of the slot you want to
        // write to relative to the physical address
        int scale = unsafe.arrayIndexScale( long[].class );

        // This is the raw Offset memory location of the index of the array
        // we want to update.  Note that the raw Offset location is not the
        // absolute physical address. It is the logical memory location of the
        // index  of the array we wish to update relative to the physical
        // address of the beginning of the array object.
        long rawOffset = base + indexToUpdate * scale;

        logger.info( "The raw offset is: " + rawOffset );

        boolean changed =
                unsafe.compareAndSwapLong(
                        longArray, rawOffset, longArray[indexToUpdate], valueToInsert );
        
        Assert.assertTrue( changed );
        Assert.assertEquals( valueToInsert, longArray[indexToUpdate] );
        logger.debug( "Value in index " + indexToUpdate + " : " + longArray[5] );
    }


    public static void main( String args[] ) {

        TestListenerAdapter tla = new TestListenerAdapter();
        TestNG testng = new TestNG();
        testng.setTestClasses( new Class[] { UnsafeTest.class } );
        testng.addListener( tla );
        testng.run();
    }
}
