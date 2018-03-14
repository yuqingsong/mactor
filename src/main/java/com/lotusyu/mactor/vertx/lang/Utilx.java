package com.lotusyu.mactor.vertx.lang;

import java.util.function.Consumer;

public class Utilx {

    static public long cost(Consumer<Long> s, long times){
        long start = System.currentTimeMillis();
        for (long i = 0; i < times; i++) {
            s.accept(i);
        }
        long end = System.currentTimeMillis();
        long cost = end - start;
        String msg = "qps";
        long qps = cost ==0?Long.MAX_VALUE:times / cost * 1000;
        System.out.println(msg+":"+ qps);
        return cost;
    }
}