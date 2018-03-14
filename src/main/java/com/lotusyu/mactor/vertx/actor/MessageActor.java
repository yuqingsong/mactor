package com.lotusyu.mactor.vertx.actor;


import java.util.function.Consumer;

/**
 * 消息接口
 * @author yuqingsong
 * @since <pre>18-1-2</pre>
 */
public interface MessageActor<P, R> extends Consumer<P>{

//
//    Consumer DEF_ERROR_HANDLER = p->{
//        ActorHomeImpl.INS.sendError(p);
//    };
//
//    default void send(P msg, Consumer<R> onSuccessded) {
//        try {
//            this.send(msg, onSuccessded,DEF_ERROR_HANDLER);
//        } catch (Throwable throwable) {
//            ActorHomeImpl.INS.sendError(throwable);
//        }
//    }

    default void send(P msg, Consumer<R> onSuccessded) {
        this.send(msg,onSuccessded,null);
    }

    default void send(P msg) {
        this.send(msg, s -> {});
    }

    default void accept(P msg) {
        this.send(msg);
    }


    void send(P msg, Consumer<R> onSuccessded, Consumer onFailed) ;




}
