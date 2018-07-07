package com.lotusyu.mactor.vertx.actor.impl;


import com.lotusyu.mactor.vertx.actor.FailedMsg;
import com.lotusyu.mactor.vertx.actor.MessageActor;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import io.vertx.core.Vertx;
import io.vertx.core.impl.ConcurrentHashSet;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * 支持超时
 * @author yuqingsong
 * @since <pre>18-1-2</pre>
 */
public class MessageActorWithTimeout<P,R> implements MessageActor<P,R> {

    /**
     * 超时时间，单位为ms
     */
    private final int timeout;

    /**
     * 用于定时器
     */
    private Vertx vertx;

    /**
     * 生成流水号
     */
    private AtomicLong seqCounter = new AtomicLong();

    /**
     * 正在执行的请求集合
     */
    private Set<Long> requestSeqSet = new ConcurrentHashSet<>();


    private final Consumer defaultOnFailed;

    private MessageActor<P,R> actor;

    static HashedWheelTimer hashedWheelTimer ;

            static{
                hashedWheelTimer = new HashedWheelTimer(10, TimeUnit.MILLISECONDS);
            }



    public <T> MessageActorWithTimeout(Vertx vertx, int timeout, MessageActor<P,R> actor,Consumer failedActor) {
        this.defaultOnFailed = failedActor;
        this.vertx = vertx;
        this.timeout = timeout;
        this.actor = actor;
    }


    @Override
    public void send(P msg, Consumer<R> onSuccessded) {
        this.send(msg,onSuccessded,defaultOnFailed);
    }

    @Override
    public   void send(P message, Consumer<R> onSuccessded, Consumer onFailed) {
        if (isActorNull()) {

        } else {
            Long seq = seqCounter.incrementAndGet();
            requestSeqSet.add(seq);
//            Long timerId = vertx.setTimer(timeout, r -> {
//                    if (removeRequestSeq(seq)) {
//                        // failed on timeout
//                        FailedMsg f = new FailedMsg(-1, "timeout", null);
//                        onFailed.accept(f);
//                    }
//                });

            Timeout timeout = hashedWheelTimer.newTimeout(timeout1 -> {
                if (removeRequestSeq(seq)) {
                    // failed on timeout
                    FailedMsg f = new FailedMsg(-1, "timeout", null);
                    onFailed.accept(f);
                }
            }, this.timeout, TimeUnit.MILLISECONDS);


            Consumer<R> finalOnSuccessded = r->{
                if(removeRequestSeq(seq)){
//                    vertx.cancelTimer(timerId);
                    timeout.cancel();
                    onSuccessded.accept(r);
                }
            };
            Consumer finalOnFailed = error->{
                if(removeRequestSeq(seq)){
//                    vertx.cancelTimer(timerId);
                    timeout.cancel();
                    onFailed.accept(error);
                }
            };
            this.actor.send(message,finalOnSuccessded,finalOnFailed);
        }
    }

    private boolean removeRequestSeq(Long seq) {
        return requestSeqSet.remove(seq);
    }

    public int executingRequestSize(){
        return requestSeqSet.size();
    }

    public boolean isActorNull() {
        return actor==null;
    }
}

