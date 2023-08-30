package com.example.provider.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@Slf4j
public class KafkaConfig {

    @Bean
    public NewTopic myTopic() {
        return TopicBuilder.name("topic-1")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic myTopic2() {
        return TopicBuilder.name("topic-2")
                .partitions(3)
                .replicas(1)
                .build();
    }

}
