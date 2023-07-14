package com.example.consumer.config;

//import com.example.provider.util.MyMailSender;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
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
import java.util.List;

@Slf4j
@Configuration
public class ZookeeperConfig {

    private CuratorFramework curatorFramework;

    @Bean
    public CuratorFramework curatorFramework(){
        curatorFramework = CuratorFrameworkFactory.builder()
//                 预设登录时的账户密码
                .authorization("digest", "user:password".getBytes(StandardCharsets.UTF_8))
//                 127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183
                .connectString("127.0.0.1")
                .sessionTimeoutMs(5000) // 会话超时时间
                .connectionTimeoutMs(5000) // 连接超时时间
                .retryPolicy(new ExponentialBackoffRetry(1000, 3)) // 重试策略
                .build();
        curatorFramework.start();
        return curatorFramework;
    }

    @PreDestroy
    public void cleanUp() {
        curatorFramework.close();
    }

}




