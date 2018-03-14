package com.lotusyu.mactor.vertx.concurrent;

import com.lotusyu.mactor.vertx.lang.Utilx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * DisruptorExecutor Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>Mar 2, 2018</pre>
 */
@RunWith(VertxUnitRunner.class)
public class DisruptorExecutorTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: execute(Runnable command)
     */
    @Test
    public void testExecute(TestContext context) throws Exception {
        Async async = context.async();
        DisruptorExecutor executor = new DisruptorExecutor();
        AtomicInteger counter = new AtomicInteger();
        Runnable runnable = () -> {
            counter.incrementAndGet();
            async.complete();
        };
        executor.execute(runnable);
    }

    @Test
    public void testExecutePerformance(TestContext context) throws Exception {
        Async async = context.async();
        DisruptorExecutor executor = new DisruptorExecutor(








        );
        AtomicInteger counter = new AtomicInteger();
        int t = 100 * 1000 * 1000;
//        t = 1;
        int times = t;

        Runnable runnable = () -> {
//            System.out.println("receive:"+Thread.currentThread());
            if(counter.incrementAndGet()==times){
                async.complete();
            };
        };
        AtomicInteger sendCounter = new AtomicInteger();
        long start = System.currentTimeMillis();
        Utilx.cost(c->{
//            sendCounter.incrementAndGet();
            executor.execute(runnable);
            if(c == times-1){
                //最后一次循环
                while(counter.get()!=times){
                }

            }
        }, times);




    }

    /**
     * Method: getValue()
     */
    @Test
    public void testGetValue() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: setValue(Runnable value)
     */
    @Test
    public void testSetValue() throws Exception {
//TODO: Test goes here... 
    }


} 
