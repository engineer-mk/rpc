package xmg.client.handler;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import xmg.client.RpcClient;
import xmg.codec.Request;
import xmg.codec.Response;
import xmg.codec.exception.RPcRemoteAccessException;
import xmg.codec.exception.RpcRemoteTimeOutException;
import xmg.server.RpcServer;
import xmg.utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


public class RpcFuture implements Future<Object> {
    private static final Logger log = LoggerFactory.getLogger(RpcFuture.class);
    public static final long maxWaitTime = 60 * 1000 * 3;
    private final Request request;
    private volatile Response response;
    private boolean isCancel;


    public RpcFuture(Request request) {
        this.request = request;
        this.isCancel = false;
    }


    public void done(Response response, boolean trace) {
        response.setEndTime(System.currentTimeMillis());
        if (request.isTrace()) {
            final String parentRequestId = request.getParentRequestId();
            if (StringUtils.isNotBlank(parentRequestId)) {
                final Response pp = RpcServer.getResponse(parentRequestId);
                if (pp != null) {
                    List<Response> childResponse = pp.getChildResponse();
                    if (childResponse == null) {
                        childResponse = new ArrayList<>();
                        pp.setChildResponse(childResponse);
                    }
                    childResponse.add(response);
                }
            }
        }
        this.response = response;
        if (trace && log.isDebugEnabled()) {
            log.debug(response.getTraceInfo());
        }
        synchronized (this) {
            notifyAll();
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCancelled() {
        return this.isCancel;
    }

    @Override
    public boolean isDone() {
        return this.response != null;
    }

    @Override
    public Object get() throws InterruptedException {
        if (!isDone()) {
            await();
        }
        if (!Response.State.OK.equals(this.response.getState())) {
            Throwable throwable = this.response.getThrowable();
            if (throwable instanceof RPcRemoteAccessException) {
                throwable = ((RPcRemoteAccessException) throwable).getTarget();
            }
            if (RpcClient.IGNORE_EXCEPTIONS.contains(throwable.getClass().getName())) {
                log.warn(response.getTraceInfo());
            } else {
                log.error("远程异常--->方法:" + response.getTraceInfo(), throwable);
            }
            throw new RPcRemoteAccessException(throwable.getMessage(), throwable);
        }
        return this.response.getResult();
    }

    @Override
    public Object get(long timeout, @NonNull TimeUnit unit) throws InterruptedException {
        Object result = null;
        if (!isDone()) {
            await(timeout, unit);
            result = this.response.getResult();
        }
        if (isDone() && !Response.State.OK.equals(this.response.getState())) {
            Throwable throwable = this.response.getThrowable();
            String msg = request.getMethodName() + Arrays.toString(request.getParameters())
                    + throwable.getMessage();
            log.error("远程异常--->方法:" + response.toString(), throwable);
            if (throwable instanceof RPcRemoteAccessException) {
                throwable = ((RPcRemoteAccessException) throwable).getTarget();
            }
            throw new RPcRemoteAccessException(msg, throwable);
        }
        return result;
    }

    private void await() throws InterruptedException {
        if (isDone()) {
            return;
        }
        if (Thread.interrupted()) {
            throw new InterruptedException(toString());
        }
        synchronized (this) {
            while (!isDone()) {
                if (System.currentTimeMillis() - this.request.getCreateTime() >= maxWaitTime) {
                    this.isCancel = true;
                    throw new RpcRemoteTimeOutException("remote api timeOut");
                }
                wait(1000);
            }
        }
    }

    private void await(long timeout, TimeUnit unit) throws InterruptedException {
        if (isDone()) {
            return;
        }
        if (Thread.interrupted()) {
            throw new InterruptedException(toString());
        }
        synchronized (this) {
            wait(Math.min(unit.toMillis(timeout), maxWaitTime));
            if (System.currentTimeMillis() - this.request.getCreateTime() >= maxWaitTime) {
                this.isCancel = true;
                throw new RpcRemoteTimeOutException("remote api timeOut");
            }
            if (!isDone()) {
                this.isCancel = true;
            }
        }
    }

    public Request getRequest() {
        return request;
    }
}
