package com.mk.rpc.server.spring.boot.autoconfigure;

import com.mk.rpc.server.spring.boot.autoconfigure.properties.RpcServerProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xmg.server.RpcServer;
import xmg.utils.StringUtils;

import java.util.Arrays;

@Configuration
@EnableConfigurationProperties(RpcServerProperties.class)
public class RpcServerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RpcServer rpcServer(RpcServerProperties properties) {
        RpcServer.TOKEN = properties.getToken();
        if (StringUtils.isNotBlank(properties.getIgnoreExceptions())) {
            final String[] split = properties.getIgnoreExceptions().split(",");
            RpcServer.IGNORE_EXCEPTIONS.addAll(Arrays.asList(split));
        }
        return new RpcServer(properties.getPort());
    }

}
