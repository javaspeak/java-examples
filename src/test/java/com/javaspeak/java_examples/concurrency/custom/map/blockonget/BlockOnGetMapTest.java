package com.javaspeak.java_examples.concurrency.custom.map.blockonget;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author John Dickerson - 15 Dec 2022
 */
public class BlockOnGetMapTest {

    private BlockOnGetMap<Long, String> blockOnGetMap;

    @BeforeClass
    public void setup() {

        blockOnGetMap = new BlockOnGetMapImpl<Long, String>( 3000l );
    }


    @Test
    public void getAndPutTest() throws Exception {

        Callable<String> callable = new Callable<String>() {

            @Override
            public String call() throws Exception {

                String value = blockOnGetMap.get( 1l, 4000 );
                System.out.println( "got: " + value );
                return value;
            }
        };

        ExecutorService executorService = Executors.newFixedThreadPool( 2 );
        Future<String> future = executorService.submit( callable );

        Runnable runnable = new Runnable() {

            @Override
            public void run() {

                try {
                    Thread.sleep( 2000 );
                    blockOnGetMap.put( 1l, "hello" );
                    System.out.println( "Put" );
                }
                catch ( InterruptedException e ) {

                    System.out.println( "Was interrupted sleeping" );
                }
            }
        };

        executorService.submit( runnable );
        executorService.shutdown();

        String value = future.get();

        Assert.assertNotNull( value );
        Assert.assertEquals( value, "hello" );
    }


    @Test
    public void purgeTest() throws Exception {

        blockOnGetMap.put( 1l, "hello" );
        blockOnGetMap.put( 2l, "world" );
        Thread.sleep( 3500 );
        blockOnGetMap.put( 3l, "Only this should remain" );
        
        Assert.assertEquals( blockOnGetMap.getEntryCount().intValue(), 1 );
    }
}
