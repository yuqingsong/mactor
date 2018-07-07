package com.lotusyu.mactor.vertx.actor.impl;

import com.lotusyu.mactor.vertx.actor.MessageActor;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

class InternalActor<P,R> implements MessageActor<P,R> {

    private final Consumer defaultOnFailed;
    private Executor executor;

    private Thread mainThread;

    private MessageActor<P,R> actor;



    private String address;

    public InternalActor(Executor executor, Thread mainThread, MessageActor<P, R> actor,Consumer defaultOnFailed) {
        this(null,executor,mainThread,actor,defaultOnFailed);
    }
    public InternalActor(String address,Executor executor, Thread mainThread, MessageActor<P, R> actor,Consumer defaultOnFailed) {
        this.address = address;
        this.executor = executor;
        this.mainThread = mainThread;
        this.actor = actor;
        this.defaultOnFailed = defaultOnFailed;
    }


    @Override
    public void send(P msg, Consumer<R> onSuccessded){
        this.send(msg,onSuccessded,defaultOnFailed);
    }

    @Override
    public void send(P msg, Consumer<R> onSuccessded, Consumer onFailed) {
//        System.out.println("address:"+address+"\tmsg:"+msg);
        Consumer r = wrapByExecutor(onSuccessded);
        Consumer e = wrapByExecutor(onFailed);
        if(isActorsThread()){
            sendToActor(msg,  r, e);
        }else{
            runOnExecutor(() -> {
                this.mainThread = Thread.currentThread();
                sendToActor(msg, r, e);
            });
        }
    }

    public void sendToActor(P msg,  Consumer r, Consumer e) {
        try {
            actor.send(msg,r,e);
        }catch (Exception ex){
            e.accept(ex);
        }
    }


    public boolean isActorsThread() {
        return Thread.currentThread() == this.mainThread;
    }


    public void runOnExecutor(Object p,Consumer task) {
        if(isActorsThread()){
            task.accept(p);
        }else{
            this.executor.execute(() -> {
                task.accept(p);
            });
        }
    }
    private void runOnExecutor(Runnable task) {
            this.executor.execute(() -> {
                task.run();
            });
    }

    public Consumer wrapByExecutor(Consumer onFailed) {
        return p -> {
            runOnExecutor(p,onFailed);
        };
    }

    public String getAddress() {
        return address;
    }
}