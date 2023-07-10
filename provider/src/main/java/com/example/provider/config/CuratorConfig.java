package com.example.provider.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Slf4j
@Configuration
public class CuratorConfig {

    private final MailConfig mailSender;
    private final PropertiesConfig propertiesConfig;

    public CuratorConfig(MailConfig mailSender, PropertiesConfig propertiesConfig) {
        this.mailSender = mailSender;
        this.propertiesConfig = propertiesConfig;
    }

    private CuratorFramework curatorFramework;
    private CuratorCache curatorCache;

    @Bean
    public CuratorFramework curatorFramework(){
        curatorFramework = CuratorFrameworkFactory.builder()
                // 127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183
                .connectString(propertiesConfig.getConnectString()) // ZooKeeper服务器地址
                .sessionTimeoutMs(5000) // 会话超时时间
                .connectionTimeoutMs(5000) // 连接超时时间
                .retryPolicy(new ExponentialBackoffRetry(1000, 3)) // 重试策略
                .build();
        curatorFramework.start();
        return curatorFramework;
    }

    @Bean
    public CuratorCache curatorCache(CuratorFramework curatorFramework) throws Exception {
        String registrationPath = propertiesConfig.getRegistrationPath(); // 要监听的ZooKeeper节点路径
        curatorCache = CuratorCache.build(curatorFramework, registrationPath);
        Stat status = curatorFramework.checkExists().forPath(registrationPath);
        if (status == null) {
            try {
                curatorFramework.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.PERSISTENT)
                        .forPath(registrationPath);
            } catch (Exception e) {
                // 处理异常
            }
        }
        return curatorCache;
    }

    @Bean
    public CuratorCacheListener curatorCacheListener(CuratorCache curatorCache){
        String to = propertiesConfig.getTo();

        CuratorCacheListener listener = CuratorCacheListener.builder()
                .forInitialized(() -> {
                    log.info("-----初始化节点");
                    mailSender.sendEmail(to, "init", "初始化节点");
                })
                .forChanges((pre, cur) -> {
                    String prePath = pre.getPath();
                    String curPath = cur.getPath();
                    log.info("-----更新节点,{}=>{}", prePath, curPath);
                    mailSender.sendEmail(to, "更新节点",
                            "旧节点:" + prePath + ",新节点:" + curPath);
                })
                .forCreates((node) -> {
                    String path = node.getPath();
                    log.info("-----创建节点,{}", path);
                    mailSender.sendEmail(to, "创建节点", path);
                })
                .forDeletes((node) -> {
                    String path = node.getPath();
                    log.info("-----删除节点,{}", path);
                    mailSender.sendEmail(to, "删除节点", path);
                })
                .build();
        curatorCache.listenable().addListener(listener);
        curatorCache.start();
        return listener;
    }

    @PreDestroy
    public void cleanUp() {
        curatorCache.close();
        curatorFramework.close();
    }
}




