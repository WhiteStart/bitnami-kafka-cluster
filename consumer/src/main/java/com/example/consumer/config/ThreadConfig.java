package com.example.consumer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class ThreadConfig {

    ThreadFactory customThreadFactory = new ThreadFactory() {
        private final AtomicInteger threadCount = new AtomicInteger(1); // 用于生成线程编号

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("MyThread-" + threadCount.getAndIncrement()); // 添加前缀名和编号
            return thread;
        }
    };

    @Bean("taskExecutor")
    public ThreadPoolExecutor taskExecutor() {
        return new ThreadPoolExecutor(
                36,
                72,
                2000,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<>(),
                customThreadFactory
        );
    }
}
