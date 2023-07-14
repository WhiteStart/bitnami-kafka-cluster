package com.example.provider.config;

import org.apache.curator.RetryPolicy;
import org.apache.curator.drivers.TracerDriver;
import org.apache.curator.ensemble.EnsembleProvider;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.zookeeper.CuratorFactory;
import org.springframework.cloud.zookeeper.CuratorFrameworkCustomizer;
import org.springframework.cloud.zookeeper.ZookeeperProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

//@Configuration
//public class CuratorConfig {
//
//    @Bean(destroyMethod = "close")
//    @ConditionalOnMissingBean
//    public CuratorFramework curatorFramework(ZookeeperProperties properties, RetryPolicy retryPolicy,
//                                             ObjectProvider<CuratorFrameworkCustomizer> optionalCuratorFrameworkCustomizerProvider,
//                                             ObjectProvider<EnsembleProvider> optionalEnsembleProvider,
//                                             ObjectProvider<TracerDriver> optionalTracerDriverProvider) throws Exception {
//        System.out.println(12345);
//        return CuratorFactory.curatorFramework(properties, retryPolicy,
//                optionalCuratorFrameworkCustomizerProvider::orderedStream, optionalEnsembleProvider::getIfAvailable,
//                optionalTracerDriverProvider::getIfAvailable);
//    }
//}
