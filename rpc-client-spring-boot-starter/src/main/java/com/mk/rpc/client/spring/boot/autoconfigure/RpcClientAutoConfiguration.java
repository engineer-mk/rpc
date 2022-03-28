package com.mk.rpc.client.spring.boot.autoconfigure;


import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xmg.client.RpcClient;
import xmg.client.connect.ConnectionManager;

@Configuration
public class RpcClientAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public static RpcClient rpcClient() {
        return new RpcClient();
    }

    @Bean
    @ConditionalOnMissingBean
    public ConnectionManager connectionManager() {
        return ConnectionManager.getInstance();
    }
}
