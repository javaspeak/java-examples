package com.javaspeak.java_examples.concurrency.semaphore;

import java.util.concurrent.ExecutionException;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author John Dickerson - 2 Dec 2022
 */
public class SemaphoreExampleTest {

    private SemaphoreExample example;

    @BeforeClass
    public void setup() {

        example = new SemaphoreExampleImpl();
    }


    @Test
    public void doTest() throws InterruptedException, ExecutionException {

    }

}
