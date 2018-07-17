package com.lotusyu.mactor.vertx.actor;

import com.lotusyu.mactor.vertx.actor.impl.MessageActorWithTimeout;
import com.lotusyu.mactor.vertx.lang.Utilx;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * ActorHomeImpl Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>Jan 4, 2018</pre>
 */
@RunWith(VertxUnitRunner.class)
public class ActorHomeTest {

    private ActorHome actors;
    private Vertx vertx;
    private int timeout = 1 * 1000;
    private static AtomicInteger beforeCounter = new AtomicInteger();

    @Before
    public void before(TestContext context) throws Exception {
            this.actors = newActorHome();
            this.vertx = Vertx.vertx();
    }

    private ActorHome newActorHome() {
        ActorHome actorHome = ActorHomes.newDisruptorActorHome();
        actorHome.setTimeout(timeout);
        return actorHome;
    }

    @After
    public void after() throws Exception {

        MessageActor<Integer,String> actor;
    }

    /**
     * Method: send(String address, Object msg, Consumer onSuccessded, Consumer<FailedMsg<?>> onFailed)
     */
    @Test
    public void testSend(TestContext ctx) throws Exception {

        Async async = ctx.async(2);
        MessageActor<Integer, Integer> integerIntegerMessageActor = (MessageActor<Integer, Integer>) (p, s, f) -> {
            s.accept(p + 1);
        };
        this.actors.<Integer,Integer>put("abc", integerIntegerMessageActor);

        this.actors.<String,String>put("xyz",(p, s, f)->{
            s.accept(p+"\thello");
        });

        this.actors.get("xyz").send("jim",f->{
            ctx.assertEquals("jim\thello", f);
            async.countDown();
        });

        Consumer<Object> successded = f -> {
            ctx.assertEquals(2, f);
            async.countDown();
        };
        this.actors.get("abc").send(1,successded);

        MessageActor<Object, Object> abc = this.actors.get("abc");

        abc.send(1, successded);


    }


    /**
     * Method: put(String address, MessageActor actor)
     */
    @Test
    public void testSendError(TestContext ctx) throws Exception {

        Async async = ctx.async();

        FailedMsg<Integer, String> error = new FailedMsg<>(1, "error", new Exception());
        this.actors.sendError(error);
        this.actors.sendError("this is error string");


        this.actors.put("e",(p,r,e)->{
            r.accept(1/0);
        });

        this.actors.get("e").send(1);

        this.actors.setError((p,r,e)->{
            System.out.println("error in method testSendError,p"+p);
            if(async.isCompleted()){

            }else{
                async.complete();
            }
        });

        this.actors.get("e").send(1);
    }



    /**
     * Method: newInstance()
     */
    @Test
    public void testNewInstance() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: newActor(Vertx vertx)
     */
    @Test
    public void testNewActorVertx() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: newActor(Vertx vertx, Messageer<FailedMsg<String>> failedMsgMessageer)
     */
    @Test
    public void testNewActorForVertxFailedMsgMessageer() throws Exception {
//TODO: Test goes here... 
    }

    @Test
    public void testRemove() throws InterruptedException {
        AtomicBoolean flag = new AtomicBoolean(false);
        String actorName = "aa";
        CountDownLatch countDownLatch = new CountDownLatch(1);
        actors.put(actorName,(p, r, e)->{
            flag.set(true);
            countDownLatch.countDown();
        });
        actors.get(actorName).send(null);
        countDownLatch.await();
        Assert.assertTrue(flag.get());
        actors.remove(actorName);

        Assert.assertFalse(actors.contains(actorName));

        actors.put("temp",(p,r,e)->{

        });


    }

    @Test
    public void testGetActors(TestContext context){
        Async async = context.async();
        AtomicReference threadRef = new AtomicReference();
        actors.put("a",(Integer p, Consumer<Integer> r, Consumer e)->{
            System.out.println("thread "+Thread.currentThread());
            threadRef.set(Thread.currentThread());
            new Thread(()->{
                r.accept(p+1);
            }).start();

        });
        actors.put("b",(Integer p, Consumer<String> r, Consumer e)->{
            System.out.println("thread "+Thread.currentThread());
            context.assertEquals(threadRef.get(),Thread.currentThread());
            Vertx.vertx().executeBlocking(h->{
                System.out.println("work "+Thread.currentThread());
                r.accept(String.valueOf(p+1));
            },rr->{});
        });
        actors.put("c",(String p, Consumer<String> r, Consumer e)->{
            System.out.println("thread "+Thread.currentThread());
            context.assertEquals(threadRef.get(),Thread.currentThread());
            new Thread(()->{r.accept(p+"_end");}).start();

        });
        MessageActor<Integer, String> actor = actors.<Integer, String>get("a", "b", "c");
        actor.send(1,r->{
            System.out.println(" result thread "+Thread.currentThread());
            context.assertEquals(threadRef.get(),Thread.currentThread());
            System.out.println(r);
            async.complete();
        });

        Async lock = context.async();
        actors.get(new String[]{"a"}).send(1,r->{
            context.assertEquals(2,r);
            lock.complete();;
        });
    }

    @Test
    public void testRemoveAll(TestContext context){
        this.actors.put("abc",(p,r,e)->{});
        this.actors.removeAll();
        context.assertFalse(this.actors.contains("abc"));
    }

    @Test
    public void testActorPerformance(TestContext context){
        Async async = context.async();
        char start = 'a';

        int channelLength = 1;
        String actorNames[] = new String[channelLength];
        for (int i = 0; i < channelLength; i++) {
            String name = String.valueOf(start + i);
            actorNames[i]=name;
            actors.put(name,(Integer p, Consumer<Integer> r, Consumer e)->{
                r.accept(p+1);
            });
        }
        MessageActor<Integer, Integer> actor = actors.<Integer, Integer>get(actorNames);
        int num = 10 * 1000 * 1000;
//        num = 1;
        int times = num;

        AtomicInteger count = new AtomicInteger();
        long startTime = System.currentTimeMillis();
        System.out.println(startTime);

            for (int i = 0; i < times; i++) {
                actor.send(0,r->{
                    int c = count.incrementAndGet();
                    if(c ==times){
                        long end = System.currentTimeMillis();
                        long cost = end - startTime;
                        System.out.println(Thread.currentThread()+" \t cost:"+cost+"\trps:"+(times/ cost *1000));
                        async.complete();
                    }
//                    if(c%10000==0){
//                        System.out.println("counter:"+c);
//                    }
                });
            }


    }

    @Test
    public void currentThread(){
        Utilx.cost(r->{Thread.currentThread();},100*1000*1000);
    }



    @Test
    public void timeoutPerformance(TestContext ctx) {
        Async async = ctx.async();

        actors.put("a", (Integer p, Consumer<Integer> r, Consumer e) -> {
            r.accept(p + 1);
        });

        MessageActor<Object, Object> actor = this.actors.get("a",timeout);

//        this.actors.put("main", (pp, rr, ee) -> {
            int times = 10*1000 * 1000;
            AtomicInteger counter = new AtomicInteger();
            Utilx.cost(t -> {
                actor.send(1, r -> {
                    int i = counter.incrementAndGet();
                    if (i == times) {
                        System.out.println("finish");
                        async.complete();
                    }
                }, f -> {

                });
            }, times);

//        });
//        this.actors.get("main").send( 1);

    }

    @Test
    public void timeoutSuccessed(TestContext ctx){
        Async async = ctx.async(2);
        this.actors.put("a",(p, r, e)->{
            System.out.println("processed");
            this.vertx.setTimer(this.timeout-500,h->{
                r.accept("timeout compelete");
            });
        });

        this.actors.get("a",this.timeout).send(1,r->{
            async.complete();
        },f->{
            ctx.fail();
            async.complete();
        });
        this.vertx.setTimer(this.timeout + 1000, h -> {
            ctx.assertTrue(async.isCompleted());
        });
    }

    @Test
    public void timeoutFailed(TestContext ctx){
        Async async = ctx.async(2);
        this.actors.put("a",(p, r, e)->{
            System.out.println("processed");
            this.vertx.setTimer(this.timeout+10,h->{
                r.accept("timeout compelete");
            });
        });

        this.actors.get("a",this.timeout).send(1,r->{
            ctx.fail();
            async.complete();
        },f->{
            System.out.println("timeout");
            async.countDown();
        });
        long l = this.vertx.setTimer(this.timeout + 100, h -> {
            if(async.count() == 1){
                async.complete();
            }else{
                ctx.fail();
            }
        });
    }

    @Test
    public void testTimeoutOnFailed(TestContext ctx) throws InterruptedException {
        Async lock = ctx.async(2);
        String errorMsg = "test timeout error";
        this.actors.put("f",(p,r,e)->{
            try {
                TimeUnit.MICROSECONDS.sleep(100);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            lock.countDown();
            e.accept(errorMsg);
        });

        this.actors.get("f",this.timeout+1000).send(1,r->{});

        MessageActor<Object, Object> actor = this.actors.get("f");
        MessageActorWithTimeout timeoutActor = new MessageActorWithTimeout(this.vertx, timeout, actor,eMsg->{
            ctx.assertEquals(errorMsg,eMsg);
        });
        timeoutActor.send(1,r->{});
        ctx.assertEquals(1,timeoutActor.executingRequestSize());

        this.vertx.setTimer(200,h->{
            ctx.assertEquals(0,timeoutActor.executingRequestSize());
        });
    }
}

