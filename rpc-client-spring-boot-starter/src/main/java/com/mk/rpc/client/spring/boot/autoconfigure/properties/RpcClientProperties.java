package com.mk.rpc.client.spring.boot.autoconfigure.properties;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "rpc.client")
public class RpcClientProperties {
    //environment.getProperty()不支持集合
    //多个值，用英文逗号分割
    private String remoteApiPackages;
    private String token;
    private String ignoreExceptions;

    public String getIgnoreExceptions() {
        return ignoreExceptions;
    }

    public void setIgnoreExceptions(String ignoreExceptions) {
        this.ignoreExceptions = ignoreExceptions;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRemoteApiPackages() {
        return remoteApiPackages;
    }

    public void setRemoteApiPackages(String remoteApiPackages) {
        this.remoteApiPackages = remoteApiPackages;
    }

}
