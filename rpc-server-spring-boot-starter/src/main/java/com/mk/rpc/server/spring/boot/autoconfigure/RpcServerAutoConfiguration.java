package com.mk.rpc.server.spring.boot.autoconfigure;

import com.mk.rpc.server.spring.boot.autoconfigure.properties.RpcServerProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xmg.server.RpcServer;

@Configuration
@EnableConfigurationProperties(RpcServerProperties.class)
public class RpcServerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RpcServer rpcServer(RpcServerProperties properties) {
        RpcServer.TOKEN = properties.getToken();
        return new RpcServer(properties.getPort());
    }

}
