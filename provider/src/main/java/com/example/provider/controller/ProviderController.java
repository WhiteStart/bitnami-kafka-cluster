package com.example.provider.controller;

import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProviderController {

    @Autowired
    private CuratorFramework curatorFramework;

    @GetMapping("/test")
    public void test() throws Exception {
        curatorFramework.create().creatingParentsIfNeeded().forPath("/test");
    }
}

