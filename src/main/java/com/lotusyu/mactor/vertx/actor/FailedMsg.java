package com.lotusyu.mactor.vertx.actor;

/**
 * 失败消息
 * @author yuqingsong
 * @since <pre>18-1-2</pre>
 */
public class FailedMsg<C,T> {

    private C code;

    private T msg;

    private Throwable cause;

    public FailedMsg(C code, T msg, Throwable cause) {
        this.code = code;
        this.msg = msg;
        this.cause = cause;
    }

    public C getCode() {
        return code;
    }

    public void setCode(C code) {
        this.code = code;
    }

    public T getMsg() {
        return msg;
    }

    public void setMsg(T msg) {
        this.msg = msg;
    }

    public Throwable getCause() {
        return cause;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    @Override
    public String toString() {
        return "code:"+code+",\tmessage:"+msg+",\tcause:"+(cause==null?"":cause.getMessage());
    }
}
