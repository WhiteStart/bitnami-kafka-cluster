package com.example.provider.controller;

import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class ProviderController {

    @Autowired
    private KafkaTemplate<String,Object> kafkaTemplate;

    @GetMapping("/send")
    public void testSend(@RequestParam String test, @RequestParam String number){
//        for (int i = 0; i < 10; i++) {
//            Map<String, Object> map = new LinkedHashMap<>();
//            map.put(test, number);
//            map.put("userid", i);
//            map.put("娜可露露有", i);
            while (true){
                try {
//                    Thread.sleep(1);
                    // 加上 get 后变为同步发送
                    ListenableFuture<SendResult<String, Object>> sync = kafkaTemplate.send("my-topic", JSONObject.toJSONString("map"));
                    sync.addCallback(new ListenableFutureCallback<SendResult<String, Object>>() {
                        @Override
                        public void onFailure(Throwable ex) {

                        }

                        @Override
                        public void onSuccess(SendResult<String, Object> result) {

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
//        }
    }

    @GetMapping("/topicTest")
    public void topicTest(){
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
