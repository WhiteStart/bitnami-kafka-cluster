//package com.example.provider.config;
//
//import lombok.extern.slf4j.Slf4j;
//import org.apache.curator.RetryPolicy;
//import org.apache.curator.ensemble.EnsembleProvider;
//import org.apache.curator.framework.CuratorFramework;
//import org.apache.curator.framework.CuratorFrameworkFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
//import org.springframework.cloud.zookeeper.ZookeeperProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.annotation.Order;
//
//@Slf4j
//@Configuration
//public class ZookeeperAutoConfig {
//
//    @Autowired(required = false)
//    private EnsembleProvider ensembleProvider;
//
//    @Order
//    @Bean(
//            destroyMethod = "close"
//    )
//    @ConditionalOnMissingBean
//    public CuratorFramework curatorFramework(RetryPolicy retryPolicy, ZookeeperProperties properties) throws Exception {
//        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder();
//        //实际当用的地方
//        addAuthInfo(builder, "user", "password");
//        if (this.ensembleProvider != null) {
//            builder.ensembleProvider(this.ensembleProvider);
//        } else {
//            builder.connectString(properties.getConnectString());
//        }
//
//        CuratorFramework curator = builder.retryPolicy(retryPolicy).build();
//        curator.start();
//        log.trace("blocking until connected to zookeeper for " + properties.getBlockUntilConnectedWait() + properties.getBlockUntilConnectedUnit());
//        curator.blockUntilConnected(properties.getBlockUntilConnectedWait(), properties.getBlockUntilConnectedUnit());
//        log.trace("connected to zookeeper");
//        return curator;
//    }
//
//    /**
//     * 添加授权
//     *
//     * @param builder
//     * @param username
//     * @param password
//     * @return
//     */
//    private CuratorFrameworkFactory.Builder addAuthInfo(CuratorFrameworkFactory.Builder builder, String username, String password) {
//        log.debug("zookeeper.username={}", username);
//        log.debug("zookeeper.password={}", password);
////        if (org.apache.commons.lang.StringUtils.isNotBlank(username) && org.apache.commons.lang.StringUtils.isNotBlank(password)) {
//            String authInfo = username + ":" + password;
//            //核心代码就这么一行
//            builder.authorization("digest", authInfo.getBytes());
////        } else {
////            log.info("Cannot resolve zookeeper username or password.");
////        }
//        return builder;
//    }
//}
