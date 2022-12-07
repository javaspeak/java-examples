package com.javaspeak.java_examples.concurrency.callable;

import java.util.concurrent.ExecutionException;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author John Dickerson - 2 Dec 2022
 */
public class CallableExampleTest {

    private CallableExample example;

    @BeforeClass
    public void setup() {

        example = new CallableExampleImpl();
    }

    @Test
    public void doTest() throws InterruptedException, ExecutionException {

    }


    public static void main( String[] args ) {

        TestListenerAdapter tla = new TestListenerAdapter();
        TestNG testng = new TestNG();
        testng.setTestClasses( new Class[] { CallableExampleTest.class } );
        testng.addListener( tla );
        testng.run();
    }
}
