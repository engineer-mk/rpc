package xmg.client.handler;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import xmg.client.connect.exception.RemoteAccessException;
import xmg.client.connect.exception.RemoteTimeOutException;
import xmg.codec.Request;
import xmg.codec.Response;

import java.util.Arrays;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


public class RpcFuture implements Future<Object> {
    private static final Logger log = LoggerFactory.getLogger(RpcFuture.class);
    private Response response;
    private final Request request;
    private final long startTime;
    private final long maxWaitTime = 10 * 1000;
    private boolean isCancel;

    public void setCancel(boolean cancel) {
        isCancel = cancel;
    }

    public RpcFuture(Request request) {
        this.request = request;
        this.startTime = System.currentTimeMillis();
        this.isCancel = false;
    }


    public void done(Response response) {
        this.response = response;
        if (log.isDebugEnabled()) {
            log.debug("调用完成--->方法:" + response.toString() + " 耗时:" + (System.currentTimeMillis() - startTime) + "ms");
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
        return response != null;
    }

    @Override
    public Object get() throws InterruptedException {
        Object result = null;
        if (!isDone()) {
            await();
            result = this.response.getResult();
        }
        if (!Response.States.OK.equals(this.response.getStates())) {
            final Exception exception = this.response.getException();
            log.error(exception.getMessage(), exception);
            String msg = request.getMethodName() + Arrays.toString(request.getParameters())
                    + exception.getMessage();
            throw new RemoteAccessException(msg);
        }
        return result;
    }

    @Override
    public Object get(long timeout, @NonNull TimeUnit unit) throws InterruptedException {
        Object result = null;
        if (!isDone()) {
            await(timeout, unit);
            result = this.response.getResult();
        }
        if (isDone() && !Response.States.OK.equals(this.response.getStates())) {
            final Exception exception = this.response.getException();
            log.error(exception.getMessage(), exception);
            String msg = request.getMethodName() + Arrays.toString(request.getParameters())
                    + exception.getMessage();
            throw new RemoteAccessException(msg);
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
                if (System.currentTimeMillis() - this.startTime >= maxWaitTime) {
                    this.isCancel = true;
                    throw new RemoteTimeOutException("remote api timeOut");
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
            if (System.currentTimeMillis() - this.startTime >= maxWaitTime) {
                this.isCancel = true;
                throw new RemoteTimeOutException("remote api timeOut");
            }
            if (!isDone()) {
                this.isCancel = true;
            }
        }
    }
}
