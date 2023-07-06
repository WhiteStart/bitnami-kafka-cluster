package com.example.provider.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class ProviderController {

    @Value("${server.port}")
    private String serverPort;

    @RequestMapping(value="/test/zk")
    public String paymentZK(){
        return "springCloud with zookeeper: " + serverPort +"\t" + UUID.randomUUID();
    }
}

