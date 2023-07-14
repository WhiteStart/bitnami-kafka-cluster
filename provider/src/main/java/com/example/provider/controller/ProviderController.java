package com.example.provider.controller;

import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
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
    public void testSend(){
        for (int i = 0; i < 10000; i++) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("datekey", 20210610);
            map.put("userid", i);
            map.put("salaryAmount", i);
            try {
                kafkaTemplate.send("my-topic", JSONObject.toJSONString(map));
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


}
