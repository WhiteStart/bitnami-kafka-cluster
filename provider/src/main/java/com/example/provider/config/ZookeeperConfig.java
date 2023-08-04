package com.example.provider.config;

//import com.example.provider.util.MyMailSender;

import com.example.provider.util.MyMailSender;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Configuration
public class ZookeeperConfig {

    private final MyMailSender mailSender;
    private final PropertiesConfig propertiesConfig;

    public ZookeeperConfig(MyMailSender mailSender, PropertiesConfig propertiesConfig) {
        this.mailSender = mailSender;
        this.propertiesConfig = propertiesConfig;
    }

    private CuratorFramework curatorFramework;
    private CuratorCache curatorCache;

    @PostConstruct
    public void init() throws Exception {
        List<ACL> list = new ArrayList<>();
        String digestString = propertiesConfig.getUsername() + ":" + propertiesConfig.getPassword();
        // 将明文账户密码通过api生成密文
        String digest = DigestAuthenticationProvider.generateDigest(digestString);
        ACL acl = new ACL(ZooDefs.Perms.ALL, new Id("digest", digest));
        list.add(acl);

        curatorFramework = CuratorFrameworkFactory.builder()
                .authorization("digest", digestString.getBytes(StandardCharsets.UTF_8))
                .aclProvider(new ACLProvider() {
                    @Override
                    public List<ACL> getDefaultAcl() {
                        return list;
                    }

                    @Override
                    public List<ACL> getAclForPath(String s) {
                        return list;
                    }
                })
                .connectString(propertiesConfig.getConnectString()) // ZooKeeper服务器地址
                .sessionTimeoutMs(5000) // 会话超时时间
                .connectionTimeoutMs(5000) // 连接超时时间
                .retryPolicy(new ExponentialBackoffRetry(1000, 3)) // 重试策略
                .build();
        curatorFramework.start();

//        if (curatorFramework.checkExists().forPath("/services") != null) {
//            curatorFramework.delete().deletingChildrenIfNeeded().forPath("/services");
//        }

        String registrationPath = propertiesConfig.getRegistrationPath(); // 要监听的ZooKeeper节点路径
        String to = propertiesConfig.getTo();
        curatorCache = CuratorCache.build(curatorFramework, registrationPath);

        CuratorCacheListener listener = CuratorCacheListener.builder()
                .forInitialized(() -> {
                    log.info("-----初始化监听器");
                })
                .forChanges((pre, cur) -> {
                    String prePath = pre.getPath();
                    String curPath = cur.getPath();
                    log.info("-----更新节点,{}=>{}", prePath, curPath);
//                    mailSender.sendEmail(to, "更新节点",
//                            "旧节点:" + prePath + ",新节点:" + curPath);
                })
                .forCreates((node) -> {
                    String path = node.getPath();
                    log.info("-----创建节点,{}", path);
                    if (!"/services".equals(path)) {
//                        mailSender.sendEmail(to, "创建节点", path);
                    }
                })
                .forDeletes((node) -> {
                    String path = node.getPath();
                    log.info("-----删除节点,{}", path);
                    if (!"/services".equals(path)) {
//                        mailSender.sendEmail(to, "删除节点", path);
                    }
                })
                .build();
        curatorCache.listenable().addListener(listener);
        curatorCache.start();
    }

//    @PostConstruct
//    public void setAclList() throws NoSuchAlgorithmException {
//        list = new ArrayList<>();
//        digestString = propertiesConfig.getUsername() + ":" + propertiesConfig.getPassword();
//        // 将明文账户密码通过api生成密文
//        String digest = DigestAuthenticationProvider.generateDigest(digestString);
//        ACL acl = new ACL(ZooDefs.Perms.ALL, new Id("digest", digest));
//        list.add(acl);
//    }
//
//    @Bean
//    public CuratorFramework curatorFramework() throws Exception {
//        curatorFramework = CuratorFrameworkFactory.builder()
////                 预设登录时的账户密码
//                .authorization("digest", digestString.getBytes(StandardCharsets.UTF_8))
//                .aclProvider(new ACLProvider() {
//                    @Override
//                    public List<ACL> getDefaultAcl() {
//                        return list;
//                    }
//
//                    @Override
//                    public List<ACL> getAclForPath(String s) {
//                        return list;
//                    }
//                })
////              // 127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183
//                .connectString(propertiesConfig.getConnectString())
//                .sessionTimeoutMs(propertiesConfig.getSessionTimeout()) // 会话超时时间
//                .connectionTimeoutMs(propertiesConfig.getConnectionTimeout()) // 连接超时时间
//                .retryPolicy(new ExponentialBackoffRetry(1000, 3)) // 重试策略
//                .build();
//        curatorFramework.start();
//
//        if(curatorFramework.checkExists().forPath("/services") != null) {
//            curatorFramework.delete().deletingChildrenIfNeeded().forPath("/services");
//        }
//
//        return curatorFramework;
//    }
//
//    @Bean
//    public CuratorCache curatorCache() {
//        curatorCache = CuratorCache.build(curatorFramework,
//                propertiesConfig.getRegistrationPath());
//
////        CuratorCacheListener listener = CuratorCacheListener.builder()
////                .forTreeCache(curatorFramework, (client, event) -> {
////                    System.out.println("++++++" + event);
////                })
////                .build();
//
////        curatorCache.listenable().addListener(listener);
//        curatorCache.listenable().addListener(curatorCacheListener());
//        curatorCache.start();
//        return curatorCache;
//    }

    @PreDestroy
    public void cleanUp() {
        curatorCache.close();
        curatorFramework.close();
    }

}




