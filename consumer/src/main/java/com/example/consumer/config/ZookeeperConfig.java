package com.example.consumer.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.acl.Acl;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Configuration
public class ZookeeperConfig {

    private CuratorFramework curatorFramework;

    @Bean
    public CuratorFramework curatorFramework() {

        List<ACL> list = new ArrayList<>();
        // 将明文账户密码通过api生成密文
        String digest = null;
        try {
            digest = DigestAuthenticationProvider.generateDigest("user:password");
            ACL acl = new ACL(ZooDefs.Perms.ALL, new Id("digest", digest));
            list.add(acl);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        curatorFramework = CuratorFrameworkFactory.builder()
//                 预设登录时的账户密码
                .authorization("digest", "user:password".getBytes(StandardCharsets.UTF_8))
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
//                 127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183
                .connectString("47.96.65.118:2181,47.96.65.118:2182,47.96.65.118:2183")
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




