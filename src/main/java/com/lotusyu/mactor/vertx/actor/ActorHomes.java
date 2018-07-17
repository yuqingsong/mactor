package com.lotusyu.mactor.vertx.actor;

import com.lotusyu.mactor.vertx.actor.impl.ActorHomeImpl;
import com.lotusyu.mactor.vertx.concurrent.DisruptorExecutor;

import java.util.concurrent.Executors;

/**
 * @author yuqingsong
 * @since <pre>18-1-2</pre>
 */
public class ActorHomes {

//    public static ActorHome newVertxActorHome() {
//        return new VertxActorHome();
//    }

    public static ActorHome newFixedThreadPoolActorHome() {
        ActorHomeImpl actorHome = ActorHomeImpl.newInstance();
        actorHome.setExecutor(Executors.newSingleThreadExecutor());
        return actorHome;
    }

    public static ActorHome newDisruptorActorHome() {
        ActorHomeImpl actorHome = ActorHomeImpl.newInstance();
        actorHome.setExecutor(new DisruptorExecutor());
        return actorHome;
    }

    public static ActorHome newDefaultActorHome(){
        return newDisruptorActorHome();
    }

//    public static ActorHome newCurrentThreadActorHome() {
//        ActorHomeImpl actorHome = ActorHomeImpl.newInstance();
//        actorHome.setExecutor(r ->r.run());
//        return actorHome;
//    }

}



