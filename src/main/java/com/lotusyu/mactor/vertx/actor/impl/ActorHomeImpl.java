package com.lotusyu.mactor.vertx.actor.impl;

import com.lotusyu.mactor.vertx.actor.ActorHome;
import com.lotusyu.mactor.vertx.actor.FailedMsg;
import com.lotusyu.mactor.vertx.actor.MessageActor;
import com.lotusyu.mactor.vertx.concurrent.DisruptorExecutor;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * @author yuqingsong
 * @since <pre>18-1-2</pre>
 */
public class ActorHomeImpl implements ActorHome {


    private static final Logger LOG = LoggerFactory.getLogger(ActorHomeImpl.class);

    private Vertx vertx;

    private int timeout=30*1000;

    private Thread mainThread;

    private static final String ERROR_ACTOR_NAME = "__error__";


    public final Consumer DEF_ERROR_HANDLER = p->{
        this.sendError(p);
    };


    public static ActorHomeImpl newInstance() {
        ActorHomeImpl actorSystemImpl = new ActorHomeImpl();
        actorSystemImpl.initErrorActor();
        return actorSystemImpl;
    }

    private  void initErrorActor() {
        this.setError(r -> {
            if (r instanceof FailedMsg) {
                LOG.error("error", ((FailedMsg) r).getCause());
            } else {
                LOG.error(r.toString());
            }
        });
    }

    private ActorHomeImpl() {
        setExecutor(newExecutor());
    }



    private void setExecutor(Executor executor) {
        this.executor = executor;
        executor.execute(()->{
            ActorHomeImpl.this.mainThread = Thread.currentThread();
        });
    }

    private Executor newExecutor() {
        Executor returnMe = null;
        Context context = Vertx.currentContext();
        if (context != null) {
            this.vertx = context.owner();
            returnMe = r -> {
                if(isActorsThread()){
                    r.run();
                }else{
                    context.runOnContext(rr -> r.run());
                }
            };

        } else {
            this.vertx = Vertx.vertx();
            //returnMe = Executors.newSingleThreadExecutor();
            returnMe = new DisruptorExecutor();
        }
        return returnMe;
    }


    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }


    private Map<String, MessageActor> actorMap = new ConcurrentHashMap<>();


    private Executor executor;


    public boolean isActorsThread() {
        return Thread.currentThread() == this.mainThread;
    }



    @Override
    public <P, R> void put(String address, MessageActor<P, R> actor) {
        MessageActor value = toInternalActor(actor);
        actorMap.put(address, value);
    }

    public <P, R> MessageActor toInternalActor(MessageActor<P, R> actor) {
        return new InternalActor(this.executor,this.mainThread,actor,DEF_ERROR_HANDLER);
    }

    public <P, R> MessageActor<P, R> get(int timeout,String... names) {
        MessageActor<P, R> prMessageActor = get(names);
        return withTimeout(prMessageActor,this.timeout);
    }

    @Override
    public <P, R> MessageActor<P, R> get(String... names) {
        if(names.length == 1){
            return this.get(names[0]);
        }
        MessageActor[] messageActors = new MessageActor[names.length];
        for (int i = 0; i < names.length; i++) {
            messageActors[i] = this.getInternal(names[i]);
        }
        return (message, onSuccessded, onFailed) -> {
            AtomicInteger index = new AtomicInteger();
            onFailed = onFailed == null ? DEF_ERROR_HANDLER : onFailed;
            Consumer finalOnFailed = onFailed;
            Consumer finalOnSuccessded = getConsumer(messageActors, index, onSuccessded, finalOnFailed);
            MessageActor messageActor = messageActors[index.get()];
            messageActor.send(message, finalOnSuccessded, finalOnFailed);
        };
    }

    private <R> Consumer<R> getConsumer(MessageActor[] messageActors, AtomicInteger index, Consumer<R> onSuccessded, Consumer onFailed) {
        int length = messageActors.length;
        Consumer<R> successded = message -> {
            int idx = index.incrementAndGet();
            Consumer<R> nextOnSuccessded = onSuccessded;
            if (index.get() == length - 1) {
            } else {
                nextOnSuccessded = getConsumer(messageActors, index, onSuccessded, onFailed);
            }
            MessageActor messageActor = messageActors[index.get()];
            messageActor.send(message, nextOnSuccessded, onFailed);
        };

        return successded;
    }

    @Override
    public <P, R> MessageActor<P, R> get(String name) {
        return getInternal(name);
    }
    public <P, R> MessageActor<P, R> get(String name,int timeout) {
        MessageActor messageActor = getInternal(name);
        return withTimeout(messageActor, timeout);
    }

    private MessageActor getInternal(String name) {
        return actorMap.get(name);
    }

    public <P, R> MessageActor<P, R> withTimeout(MessageActor messageActor, int timeout) {
        return toInternalActor(new MessageActorWithTimeout<P, R>(this.vertx, timeout,messageActor,this.DEF_ERROR_HANDLER));
    }

    public void setError(Consumer actor) {
        this.setError((p, r, e) -> actor.accept(p));
    }


    @Override
    public <P, R> void setError(MessageActor<P, R> actor) {
        this.put(ERROR_ACTOR_NAME, actor);
    }

    @Override
    public boolean contains(String address) {
        return this.actorMap.containsKey(address);
    }

    @Override
    public void remove(String address) {
        this.actorMap.remove(address);
    }

    public void removeAll() {
        this.actorMap.clear();
    }

    @Override
    public void sendError(Object msg) {
        actorMap.get(ERROR_ACTOR_NAME).send(msg, null);
    }



}



