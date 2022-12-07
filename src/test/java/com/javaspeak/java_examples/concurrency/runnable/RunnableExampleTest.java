package com.javaspeak.java_examples.concurrency.runnable;

import java.util.concurrent.ExecutionException;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author John Dickerson - 2 Dec 2022
 */
public class RunnableExampleTest {

    private RunnableExample example;

    @BeforeClass
    public void setup() {

        example = new RunnableExampleImpl();
    }

    @Test
    public void doTest() throws InterruptedException, ExecutionException {

    }
}
