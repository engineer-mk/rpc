package xmg.server.support;


import java.lang.reflect.Method;
import java.util.Objects;


public class ServerMethod {
    private String beanName;
    private Method method;

    public ServerMethod() {
    }

    public ServerMethod(String beanName, Method method) {
        this.beanName = beanName;
        this.method = method;
    }

    @Override
    public String toString() {
        return "ServerMethod{" +
                "beanName='" + beanName + '\'' +
                ", method=" + method +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerMethod)) return false;
        ServerMethod that = (ServerMethod) o;
        return Objects.equals(beanName, that.beanName) && Objects.equals(method, that.method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(beanName, method);
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }
}
