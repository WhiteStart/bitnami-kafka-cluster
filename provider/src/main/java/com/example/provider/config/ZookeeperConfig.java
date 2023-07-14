package com.example.provider.config;

//import com.example.provider.util.MyMailSender;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.zookeeper.CuratorFrameworkCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
public class ZookeeperConfig {

//    private final MyMailSender mailSender;
    private final PropertiesConfig propertiesConfig;

    public ZookeeperConfig(PropertiesConfig propertiesConfig) {
        this.propertiesConfig = propertiesConfig;
    }

    private CuratorFramework curatorFramework;
    private CuratorCache curatorCache;
    private List<ACL> list;
    private String digestString;

    @PostConstruct
    public void setAclList() throws NoSuchAlgorithmException {
        list = new ArrayList<>();
//        digestString = propertiesConfig.getName() + ":" + propertiesConfig.getPassword();
        digestString = "user:password";
        // 将明文账户密码通过api生成密文
        String digest = DigestAuthenticationProvider.generateDigest(digestString);
        ACL acl = new ACL(ZooDefs.Perms.ALL, new Id("digest", digest));
        list.add(acl);
    }

    @Bean
    public CuratorFramework curatorFramework(){
        curatorFramework = CuratorFrameworkFactory.builder()
//                 预设登录时的账户密码
                .authorization("digest", "user:password".getBytes(StandardCharsets.UTF_8))
//                 127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183
                .connectString(propertiesConfig.getConnectString())
                .sessionTimeoutMs(propertiesConfig.getSessionTimeout()) // 会话超时时间
                .connectionTimeoutMs(propertiesConfig.getConnectionTimeout()) // 连接超时时间
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
                        // 根据预设的账户密码登录，保证只有该账户可以操作相关节点
                        .withACL(list, true)
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
//                    mailSender.sendEmail(to, "init", "初始化节点");
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
//                    mailSender.sendEmail(to, "创建节点", path);
                })
                .forDeletes((node) -> {
                    String path = node.getPath();
                    log.info("-----删除节点,{}", path);
//                    mailSender.sendEmail(to, "删除节点", path);
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




