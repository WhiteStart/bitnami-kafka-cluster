package com.example.provider.controller;

import com.alibaba.fastjson2.JSONObject;
import com.example.provider.dataobject.Provider;
import com.example.provider.mapper.ProviderMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@Log4j2
public class ProviderController {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ProviderMapper providerMapper;

    public ProviderController(KafkaTemplate<String, Object> kafkaTemplate, ProviderMapper providerMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.providerMapper = providerMapper;
    }

    @GetMapping("/sync")
    public void sync(@RequestParam String test, @RequestParam int number) {
        ExecutorService service = Executors.newFixedThreadPool(8);

        for (int i = 0; i < number; i++) {
            int j = i;
            service.execute(()->{
                Map<String, String> map = new HashMap<>();
                map.put("name", String.valueOf(j * j));
                map.put("age", String.valueOf(j * 2));
                map.put("index", String.valueOf(j));
                map.put("code", "gagadgagegaegaegaegaegegtwoqriqwriqnwruioqcwnrioqnwcriqwcoqrcnqoiwrunqocwr:" + j);
                ListenableFuture<SendResult<String, Object>> send = kafkaTemplate.send("my-topic", JSONObject.toJSONString(map));
                send.addCallback(new ListenableFutureCallback<SendResult<String, Object>>() {
                    @Override
                    public void onFailure(Throwable ex) {
                        ex.printStackTrace();
                    }

                    @Override
                    public void onSuccess(SendResult<String, Object> result) {
                        RecordMetadata recordMetadata = result.getRecordMetadata();
                        Provider provider = new Provider();
                        provider.setTopic(recordMetadata.topic());
                        provider.setUsed_partition(recordMetadata.partition());
                        provider.setOffset(String.valueOf(recordMetadata.offset()));
                        provider.setTimestamp(String.valueOf(recordMetadata.timestamp()));
                        providerMapper.insert(provider);
                        log.info("插入数据{}", recordMetadata.topic());
                    }
                });
            });
        }

    }


    @GetMapping("/topicTest")
    public void topicTest() {
        for (int i = 0; i < 2; i++) {
            try {
                Thread.sleep(1);
                // 加上 get 后变为同步发送
                SendResult<String, Object> result = kafkaTemplate.send("single-xiotpull-NK", JSONObject.toJSONString("消息测试")).get();
                System.out.println("stringObjectSendResult.getRecordMetadata().topic() = " + result.getRecordMetadata().topic());
                System.out.println("result.getRecordMetadata().partition() = " + result.getRecordMetadata().partition());
                System.out.println("result.getRecordMetadata().offset() = " + result.getRecordMetadata().offset());
                System.out.println("------------------------");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
