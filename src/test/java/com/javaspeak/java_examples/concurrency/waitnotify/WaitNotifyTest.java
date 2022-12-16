package com.javaspeak.java_examples.concurrency.waitnotify;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author John Dickerson - 15 Dec 2022
 */
public class WaitNotifyTest {

    @Test
    public void waitNotifyTest() throws Exception {

        String monitor = "monitor";

        ExecutorService executorService = Executors.newFixedThreadPool( 2 );

        Callable<String> callablWait = new Callable<>() {

            @Override
            public String call() throws Exception {

                synchronized ( monitor ) {

                    monitor.wait();
                }

                return "notified";
            }

        };

        Future<String> future = executorService.submit( callablWait );

        Thread.sleep( 1000 );


        Runnable runnableNotify = new Runnable() {

            @Override
            public void run() {

                synchronized ( monitor ) {

                    monitor.notifyAll();
                    System.out.println( "Notified others" );
                }

            }
        };

        executorService.submit( runnableNotify );
        executorService.shutdown();

        String message = future.get();
        Assert.assertEquals( message, "notified" );
    }


    @Test( )
    public void waitTimeoutTest() throws Exception {

        String monitor = "monitor";

        ExecutorService executorService = Executors.newFixedThreadPool( 2 );

        Callable<String> callablWait = new Callable<>() {

            @Override
            public String call() throws Exception {

                synchronized ( monitor ) {
                    
                    long before = System.nanoTime();

                    monitor.wait( 1000 );
                    
                    long after = System.nanoTime();
                    long diff = after - before;
                    long diffMilli = diff / 1000000;

                    System.out.println( diffMilli );
                    
                    if ( diffMilli >= 1000 ) {

                        return "failed";
                    }
                }

                return "notified";
            }
        };

        Future<String> future = executorService.submit( callablWait );
        executorService.shutdown();
        Thread.sleep( 2000 );
        String message = future.get();
        Assert.assertEquals( message, "failed" );
    }
}
