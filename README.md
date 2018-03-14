# mactor
## 简介
mactor是一个轻量级的actor容器。线程模型与vertx类似，使用简单方便。
<br>优点：轻量（目前只有几个类）、快（跨线程发送消息QPS可达百万，actor之间发送消息QPS可达千万）
## 示例
### 创建ActorHome
```
ActorHomeImpl actorHome = ActorHomeImpl.newInstance();
```
### 添加Actor
```
actorHome.<Integer,Integer>put("add",(p, s, f)->{
            s.accept(p+1);
});

actorHome.<Integer,Integer>put("sub",(p, s, f)->{
            s.accept(p-1);
});
```
### 给Actor发送消息
```
actorHome.get("add").send(1,result->{
      //此处会打印2
      System.out.println(result);      
});
```
### 串联多个actor并发送消息
```
//消息会先发送到add actor,将add的计算结果发送到 sub actor，最终sub actor会通过回调返回计算结果。
actorHome.get("add","sub").send(1,result->{
      //此处会打印1
      System.out.println(result);      
});

```






