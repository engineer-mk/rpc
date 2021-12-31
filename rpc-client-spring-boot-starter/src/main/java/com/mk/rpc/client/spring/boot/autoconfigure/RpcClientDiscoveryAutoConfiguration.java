package com.mk.rpc.client.spring.boot.autoconfigure;


import com.alibaba.cloud.nacos.ConditionalOnNacosDiscoveryEnabled;
import com.alibaba.cloud.nacos.discovery.NacosDiscoveryClient;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xmg.client.connect.ConnectionManager;
import xmg.client.connect.ServerDiscovery;

@Configuration
@ConditionalOnClass(NacosDiscoveryClient.class)
@ConditionalOnDiscoveryEnabled
@ConditionalOnNacosDiscoveryEnabled
@AutoConfigureAfter(RpcClientAutoConfiguration.class)
public class RpcClientDiscoveryAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ConnectionManager.class)
    public ServerDiscovery serverDiscovery(ConnectionManager connectionManager, DiscoveryClient discoveryClient) {
        return new ServerDiscovery(discoveryClient, connectionManager);

    }
}


