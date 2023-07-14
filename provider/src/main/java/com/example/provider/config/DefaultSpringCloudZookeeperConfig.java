package com.example.provider.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class DefaultSpringCloudZookeeperConfig {

//    @Value("${spring.cloud.zookeeper.username}")
//    private String username;
//
//    @Value("${spring.cloud.zookeeper.password}")
//    private String password;

    @Bean
    public CuratorFramework curatorFramework(RetryPolicy retryPolicy, org.springframework.cloud.zookeeper.ZookeeperProperties properties) throws Exception {
//        System.out.println("username = " + username);
//        System.out.println("password = " + password);
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder();
        builder.connectString("127.0.0.1");
//        if (StringUtils.isNotEmpty(username)
//                && StringUtils.isNotEmpty(password)) {
            builder.authorization("digest", ("user" + ":" + "password").getBytes());
//        }
        CuratorFramework curator = builder.retryPolicy(retryPolicy).build();
        curator.start();
        curator.blockUntilConnected(properties.getBlockUntilConnectedWait(), properties.getBlockUntilConnectedUnit());
        log.trace("connected to zookeeper");
        return curator;
    }
}
