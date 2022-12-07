package com.javaspeak.java_examples.concurrency.cyclicbarrier;

import java.util.concurrent.ExecutionException;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author John Dickerson - 2 Dec 2022
 */
public class CyclicBarrierExampleTest {

    private CyclicBarrierExample example;


    @BeforeClass
    public void setup() {

        example = new CyclicBarrierExampleImpl();
    }


    @Test
    public void doTest() throws InterruptedException, ExecutionException {


    }


    public static void main( String[] args ) {

        TestListenerAdapter tla = new TestListenerAdapter();
        TestNG testng = new TestNG();
        testng.setTestClasses( new Class[] { CyclicBarrierExampleTest.class } );
        testng.addListener( tla );
        testng.run();
    }
}
