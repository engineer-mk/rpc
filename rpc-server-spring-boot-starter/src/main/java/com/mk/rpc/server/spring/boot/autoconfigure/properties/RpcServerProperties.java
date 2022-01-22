package com.mk.rpc.server.spring.boot.autoconfigure.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rpc.server")
public class RpcServerProperties {
    private Integer port = 7000;

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

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}
