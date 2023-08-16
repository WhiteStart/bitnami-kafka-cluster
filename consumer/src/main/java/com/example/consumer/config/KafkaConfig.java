package com.example.consumer.config;

import com.alibaba.fastjson2.JSONObject;
import com.example.consumer.dataobject.Consumer;
import com.example.consumer.mapper.ConsumerMapper;
import io.netty.handler.codec.json.JsonObjectDecoder;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.scheduling.annotation.Async;


@Configuration
@Log4j2(topic = "consumer")
public class KafkaConfig {

    @Autowired
    private ConsumerMapper consumerMapper;

    // 逐条操作
    @KafkaListener(topics = "my-topic", concurrency = "12")
    public void consumer(ConsumerRecord<String, String> record, Acknowledgment ack){
        Consumer consumer = fill(record);
        consumerMapper.insert(consumer);
        // 手动提交 offset
        ack.acknowledge();
    }

    @KafkaListener(topics = "my-topic", concurrency = "12")
    public void consumer2(ConsumerRecord<String, String> record, Acknowledgment ack){
        Consumer consumer = fill(record);
        consumerMapper.insert(consumer);
        // 手动提交 offset
        ack.acknowledge();
    }

    @KafkaListener(topics = "my-topic", concurrency = "12")
    public void consumer3(ConsumerRecord<String, String> record, Acknowledgment ack){
        Consumer consumer = fill(record);
        consumerMapper.insert(consumer);
        // 手动提交 offset
        ack.acknowledge();
    }

    private Consumer fill(ConsumerRecord<String, String> record) {
        Consumer consumer = new Consumer();
        JSONObject jsonObject = JSONObject.parseObject(record.value());
        consumer.setTaskId(jsonObject.getString("index"));
        consumer.setData(record.value());
        consumer.setTopic(record.topic());
        consumer.setUsed_partition(record.partition());
        consumer.setOffset(String.valueOf(record.offset()));
        consumer.setTime(String.valueOf(record.timestamp()));
        consumer.setKey(record.key());
        consumer.setLeaderEpoch(String.valueOf(record.leaderEpoch()));
        return consumer;
    }

    // 一次操作所有消息
    //    @KafkaListener(topics = {"my-topic"}, groupId = "test")
//    public void consumer(ConsumerRecords<String, String> records, Acknowledgment ack){
//
//    }

}
