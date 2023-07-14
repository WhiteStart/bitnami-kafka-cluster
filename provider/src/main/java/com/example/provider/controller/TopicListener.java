package com.example.provider.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TopicListener {

//    @KafkaListener(topics = {"test_search"})
//    public void consumer(String message){
//        log.info("收到数据:{}", message);
//    }
}