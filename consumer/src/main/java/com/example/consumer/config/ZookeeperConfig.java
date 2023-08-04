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
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private PropertiesConfig propertiesConfig;

    private CuratorFramework curatorFramework;

    @Bean
    public CuratorFramework curatorFramework() {

        List<ACL> list = new ArrayList<>();
        // 将明文账户密码通过api生成密文
        String pass = propertiesConfig.getUsername() + ":" + propertiesConfig.getPassword();
        try {
            String digest = DigestAuthenticationProvider.generateDigest(pass);
            ACL acl = new ACL(ZooDefs.Perms.ALL, new Id("digest", digest));
            list.add(acl);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        curatorFramework = CuratorFrameworkFactory.builder()
//                 预设登录时的账户密码
                .authorization("digest", pass.getBytes(StandardCharsets.UTF_8))
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
                .connectString(propertiesConfig.getConnectString())
                .sessionTimeoutMs(propertiesConfig.getSessionTimeout()) // 会话超时时间
                .connectionTimeoutMs(propertiesConfig.getConnectionTimeout()) // 连接超时时间
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




