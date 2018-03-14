package com.lotusyu.mactor.vertx.actor;

/**
 * actorhome 提供添加、移除查询等相关功能。
 * actor线程：执行actor逻辑的线程。
 * actorhome 的实现类必须遵守以下规则
 * 1：请不要在阻塞actor线程，如果要在actor中执行阻塞代码，请另开线程执行阻塞代码。
 * 2：确保同一个actor不会并行执行。
 * 3：不同的actor之间可以并行执行（当然，也可以让所有的actor都不会并行执行）
 * 4：所有的actor相关的代码，不管是成功回调，还是失败回调，都运行在actor线程。也就是说，当调用actor 的 send方法时，如果执行send方法不是actor线程，会切换到actor线程执行。

 * @author yqs
 */
public interface ActorHome {

    <P, R> void put(String address, MessageActor<P, R> actor);

    <P, R> MessageActor<P, R> get(String... names);

    /**
     * 获取actor（相当于依次执行 get（names[0]）,get(names[1)...get(names[n),并返回最后一个actor的执行结果)
     * @param timeout 超时时间，不是调用get方法的超时时间，而是获取actor后，调用send方法的响应的超时时间。
     * @param names
     * @param <P>
     * @param <R>
     * @return
     */
    <P, R> MessageActor<P, R> get(int timeout,String... names);

    <P, R> MessageActor<P, R> get(String name);
    <P, R> MessageActor<P, R> get(String name,int timeout);

    /**
     * 统一的错误处理的actor
     * @param actor
     * @param <P>
     * @param <R>
     */
    <P, R> void setError(MessageActor<P, R> actor);

    boolean contains(String address);

    void remove(String address);

    void removeAll();

//    <P> void send(String address, P msg);
//
//    <P, R> void send(String address, P msg, Consumer<R> onSuccessded);
//
//    <P, R, E> void send(String address, P msg, Consumer<R> onSuccessded, Consumer<E> onFailed);

    void sendError(Object msg);
}
