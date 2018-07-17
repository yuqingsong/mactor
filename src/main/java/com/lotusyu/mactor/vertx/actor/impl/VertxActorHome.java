package com.lotusyu.mactor.vertx.actor.impl;

import com.lotusyu.mactor.vertx.actor.ActorHome;
import com.lotusyu.mactor.vertx.actor.FailedMsg;
import com.lotusyu.mactor.vertx.actor.MessageActor;
import com.lotusyu.mactor.vertx.concurrent.DisruptorExecutor;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * @author yuqingsong
 * @since <pre>18-1-2</pre>
 */
@Deprecated
class VertxActorHome extends ActorHomeImpl{

    public VertxActorHome(){
        super.init();
    }

    @Override
    protected Executor newExecutor() {

        Context context = Vertx.currentContext();
        if (context == null) {
           context = Vertx.vertx().getOrCreateContext();
        }
        Context finalContext = context;
        Executor returnMe = r -> {
            if (isActorsThread()) {
                r.run();
            } else {
                finalContext.runOnContext(rr -> r.run());
            }
        };
        return returnMe;
    }
}



