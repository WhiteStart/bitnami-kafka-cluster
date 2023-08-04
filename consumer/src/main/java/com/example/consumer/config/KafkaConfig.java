package com.example.consumer.config;

import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.PartitionOffset;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.support.Acknowledgment;


@Configuration
@Log4j2(topic = "consumer")
public class KafkaConfig {

    // 逐条操作
    @KafkaListener(topics = {"my-topic"}, groupId = "test", concurrency = "6")
    public void consumer(ConsumerRecord<String, String> record, Acknowledgment ack){
        String value = record.value();
        log.info(record);
        log.info(value);
        // 手动提交 offset
        ack.acknowledge();
    }

    // 一次操作所有消息
    //    @KafkaListener(topics = {"my-topic"}, groupId = "test")
//    public void consumer(ConsumerRecords<String, String> records, Acknowledgment ack){
//
//    }

    @KafkaListener(groupId = "test",
    topicPartitions = {
            @TopicPartition(topic = "topic1", partitions = {"0","1","2"}),
            @TopicPartition(topic = "topic1", partitions = {"3","4","5"}),
//                    partitionOffsets = @PartitionOffset(partition = "1", initialOffset = "100")),
    }, concurrency = "3") //  同组下的消费者个数
    public void consumer2(ConsumerRecord<String, String> record, Acknowledgment ack){
        String value = record.value();
        log.info(record);
        log.info(value);
        ack.acknowledge();
    }


}
