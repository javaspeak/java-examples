package com.javaspeak.java_examples.concurrency.lock.stampedlock;

import java.util.concurrent.ExecutionException;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author John Dickerson - 2 Dec 2022
 */
public class StampedLockExampleTest {

    private StampedLockExample stampedLockExample;

    @BeforeClass
    public void setup() {

        stampedLockExample = new StampedLockExampleImpl();
    }


    @Test
    public void doTest() throws InterruptedException, ExecutionException {

        stampedLockExample.put( "greeting", "hello" );

        String greeting =
                stampedLockExample.tryReadWithOptimisticLockFirst( "greeting" );

        Assert.assertEquals( greeting, "hello" );
    }


    public static void main( String[] args ) {

        TestListenerAdapter tla = new TestListenerAdapter();
        TestNG testng = new TestNG();
        testng.setTestClasses( new Class[] { StampedLockExampleTest.class } );
        testng.addListener( tla );
        testng.run();
    }
}
