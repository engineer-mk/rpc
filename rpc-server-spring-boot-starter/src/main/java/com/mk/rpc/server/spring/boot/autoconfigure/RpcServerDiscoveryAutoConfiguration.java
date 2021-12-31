package com.mk.rpc.server.spring.boot.autoconfigure;


import com.alibaba.cloud.nacos.ConditionalOnNacosDiscoveryEnabled;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.discovery.NacosDiscoveryAutoConfiguration;
import com.mk.rpc.server.spring.boot.autoconfigure.properties.RpcServerProperties;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(NacosDiscoveryProperties.class)
@ConditionalOnDiscoveryEnabled
@ConditionalOnNacosDiscoveryEnabled
@AutoConfigureAfter(RpcServerAutoConfiguration.class)
@AutoConfigureBefore(NacosDiscoveryAutoConfiguration.class)
@EnableConfigurationProperties(RpcServerProperties.class)
public class RpcServerDiscoveryAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public NacosDiscoveryProperties nacosDiscoveryProperties(RpcServerProperties properties) {
        NacosDiscoveryProperties nacosDiscoveryProperties = new NacosDiscoveryProperties();
        nacosDiscoveryProperties.getMetadata().put("rpcServerPort", properties.getPort().toString());
        return nacosDiscoveryProperties;
    }
}


