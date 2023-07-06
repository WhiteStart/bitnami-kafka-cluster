package com.example.provider;

import com.example.provider.config.PropertiesConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Component
@Slf4j
public class ServiceRegistrationListener {

    private final JavaMailSender mailSender;
    private final PropertiesConfig propertiesConfig;
    private CuratorFramework curatorFramework;
    private CuratorCache cache;

    public ServiceRegistrationListener(JavaMailSender mailSender, PropertiesConfig propertiesConfig, CuratorFramework curatorFramework) {
        this.mailSender = mailSender;
        this.propertiesConfig = propertiesConfig;
    }

    @PostConstruct
    public void init() throws Exception {
        curatorFramework = CuratorFrameworkFactory.builder()
                .connectString(propertiesConfig.getConnectString()) // ZooKeeper服务器地址
                .sessionTimeoutMs(5000) // 会话超时时间
                .connectionTimeoutMs(5000) // 连接超时时间
                .retryPolicy(new ExponentialBackoffRetry(1000, 3)) // 重试策略
                .build();
        curatorFramework.start();
        String registrationPath = propertiesConfig.getRegistrationPath(); // 要监听的ZooKeeper节点路径
        String to = propertiesConfig.getTo();
        cache = CuratorCache.build(curatorFramework, registrationPath);
        Stat status = curatorFramework.checkExists().forPath(registrationPath);
        if (status == null) {
            curatorFramework.create().
                    creatingParentsIfNeeded().
                    withMode(CreateMode.PERSISTENT).
                    forPath(registrationPath);
        }

        CuratorCacheListener listener = CuratorCacheListener.builder()
                .forInitialized(() -> {
                    log.info("-----初始化节点");
                    sendEmail(to, "init", "初始化节点");
                })
                .forChanges((pre, cur) -> {
                    String prePath = pre.getPath();
                    String curPath = cur.getPath();
                    log.info("-----更新节点,{}=>{}", prePath, curPath);
                    sendEmail(to, "更新节点",
                            "旧节点:" + prePath + ",新节点:" + curPath);
                })
                .forCreates((node) -> {
                    String path = node.getPath();
                    log.info("-----创建节点,{}", path);
                    sendEmail(to, "创建节点", path);
                })
                .forDeletes((node) -> {
                    String path = node.getPath();
                    log.info("-----删除节点,{}", path);
                    sendEmail(to, "删除节点", path);
                })
                .build();
        cache.listenable().addListener(listener);
        cache.start();
    }

    @PreDestroy
    public void cleanup() throws Exception {
        if (cache != null) {
            cache.close();
        }
        if(curatorFramework != null){
            curatorFramework.close();
        }
    }

    private void sendEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setFrom(propertiesConfig.getFrom()); // 设置邮件发送者地址
            helper.setSubject(subject);
            helper.setText(content);

            mailSender.send(message);
        } catch (MessagingException e) {
            // 处理邮件发送异常
            e.printStackTrace();
        }
    }
}




