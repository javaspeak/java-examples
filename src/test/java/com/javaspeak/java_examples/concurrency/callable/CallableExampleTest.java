package com.javaspeak.java_examples.concurrency.callable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author John Dickerson - 2 Dec 2022
 */
public class CallableExampleTest {

    private Logger logger = LoggerFactory.getLogger( CallableExampleTest.class );

    private CallableExample example;

    @BeforeClass
    public void setup() {

        example = new CallableExampleImpl();
    }

    @Test
    public void doTest() throws InterruptedException, ExecutionException {

        ExecutorService executorService = Executors.newFixedThreadPool( 4 );
        List<Callable<Integer>> callables = new ArrayList<>();
        
        for ( int i = 0; i < 4; i++ ) {

            Callable<Integer> callable = new Callable<Integer>() {

                @Override
                public Integer call() throws Exception {
                    
                    return example.updateState( 1 );
                }
            };

            callables.add( callable );
        }
        
        List<Future<Integer>> futures = executorService.invokeAll( callables );
        
        for ( Future<Integer> future: futures ) {
            
            // The get method blocks until the callable has returned its value
            Integer state = future.get();
            logger.info( "state = " + state );
        }

        Integer endState = example.getState();
        Assert.assertEquals( endState.intValue(), 4 );
    }


    public static void main( String[] args ) {

        TestListenerAdapter tla = new TestListenerAdapter();
        TestNG testng = new TestNG();
        testng.setTestClasses( new Class[] { CallableExampleTest.class } );
        testng.addListener( tla );
        testng.run();
    }
}
