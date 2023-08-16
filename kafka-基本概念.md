# 基本概念

|           |                                                              |
| --------- | ------------------------------------------------------------ |
| Broker    | 消息中间件处理节点，一个 Kafka 节点就是一个 broker，一个或多个Broker可以组成一个Kafka集群 |
| Topic     | Kafka根据 topic 对消息进行归类，发布到Kafka集群的每条消息都需要指定 topic |
| Producer  | 消息生产者，向 Broker 发送消息的客户端                       |
| Consumer  | 消息消费者，从 Broker 读取消息的客户端                       |
| partition | 分区 分布式存储消息                                          |
|           |                                                              |



## 分区

- 分区存储的优点
  - 解决统一存储文件过大的问题
  - 提高了吞吐量，读写均可以同时在多个分区进行

![截屏2023-08-11 下午2.03.03](/Users/huangminzhi/Library/Application Support/typora-user-images/截屏2023-08-11 下午2.03.03.png)



## 副本

replicas 为主题中的分区创建多个备份【实现高可用】，多个副本在 kafka 集群中会有一个作为 leader

- leader
  - kafka的读写都在 leader 上
  - leader 负责把数据同步给 follower
  - 主从选举
- follower
  - 接收 leader 的数据
- isr
  - 可以同步和已经同步的节点会被存入到 isr 集合中
  - 如果某节点性能较差，会被【踢出】集合

![截屏2023-08-11 下午2.02.35](/Users/huangminzhi/Library/Application Support/typora-user-images/截屏2023-08-11 下午2.02.35.png)



## acks

| 0    | kafka-cluster 不需要收到任何 broker 消息就会返回ack给 producer，效率最高但容易丢失消息 |
| ---- | :----------------------------------------------------------- |
| 1    | 多副本之间的 leader 已经收到消息，并写入本地log中，才会返回ack，性能与安全均衡 |
| -1   | 默认配置min.insync.replicas=2，此时需要 leader 和一个 follower 同步完成后才会返回，最安全但性能最差 |



## offset的提交方式

### 自动提交

- 直接提交 offset【如果此时cosumer挂了，可能会导致消息丢失】

```yml
# 是否自动提交
enable-auto-commit: true
# 自动提交时间间隔，单位ms
auto-commit-interval: 1000
```

### 手动提交

- 在消费消息后再提交 offset

```yml
enable-auto-commit: false
```

- 手动同步提交
  - 在消费完消息后调用同步提交的方法
  - 集群返回 ack 前一直阻塞，返回 ack 后表示提交成功，执行之后的逻辑
- 手动异步提交
  - 在消息消费完后提交
  - 不需要等到集群 ack 返回，直接执行之后的逻辑
  - 可以设置一个回调方法，供集群调用



## Controller

Kafka集群中的broker在zk中创建临时序号节点，序号最小的节点（最先创建的节点）将作为集群的Controller，管理所有分区和副本的状态

- 当某个分区的leader副本出现故障时，由控制器负责为该分区选举新的 leader 副本
- 当检测到某个分区的ISR集合发生变化时，由控制器负责通知所有broker更新其元数据信息
- 分区变化时，同步信息给broker

![截屏2023-07-30 下午7.32.54](/Users/huangminzhi/Library/Application Support/typora-user-images/截屏2023-07-30 下午7.32.54.png)



## Rebalance 机制

前提：消费者没有指明分区消费。当消费组里的消费者和分区的关系发生变化时，触发 Rebalance

重新调整消费者消费哪个分区

在触发Rebalance之前，消费者消费哪个分区有三种策略

- range：通过公式计算某个消费者消费哪个分区
  - 前面的消费者：分区总数/消费者总数 + 1
  - 之后的消费者：分区总数/消费者数量
- 轮询：轮流消费
- sticky（粘合策略）：
  - 如果需要Rebalance，在原分区不变的基础上进行调整
  - 如果没有开启该策略，全部重新分配【影响性能】



## 集群消费

- 同一个消费组中，一个partition只能被一个consumer消费
  - 保证消费的顺序
  - kafka只在partition范围内保证消费的局部顺序性

- partition 的数量决定了消费组中消费者的数量
  - 建议同一个消费组中的消费者数量不要超过 partition 数量
  - 超过了会有 consumer 消费不到消息
- 如果 consumer 挂了，触发 rebalance 机制，让其他 consumer来消费该分区

![截屏2023-08-11 下午2.03.12](/Users/huangminzhi/Library/Application Support/typora-user-images/截屏2023-08-11 下午2.03.12.png)



## HW 和 LEO

HighWatermark and Last end offset

LEO是某个副本最后消息的消息位置

HW是已完成同步的位置。消息在写入broker时，且每个broker完成这条消息的同步后，hw才会变化。在这之前消费者消费不到这条消息。

同步完成之后，HW更新之后，消费者才能消费到这条消息，【防止消息的丢失】

![截屏2023-07-31 下午2.29.57](/Users/huangminzhi/Library/Application Support/typora-user-images/截屏2023-07-31 下午2.29.57.png)



# Spring Boot应用

## 生产者

```java
@GetMapping("/sendAsync")
public void sendAsync(){
    for (int i = 0; i < 10; i++) {
        kafkaTemplate.send("my-topic", JSONObject.toJSONString(i));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
}

@GetMapping("/send")
    public void send(){
        for (int i = 0; i < 10; i++) {
            try {
                Thread.sleep(1);
                // 加上 get 后变为同步发送
                SendResult<String, Object> result = kafkaTemplate.send("single-xiotpull-NK", JSONObject.toJSONString("消息测试")).get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
```

## 消费者

```java
@KafkaListener(topics = {"my-topic"}, groupId = "test")
public void consumer(ConsumerRecord<String, String> record, Acknowledgment ack){
    String value = record.value();
    ack.acknowledge();
}
```



# $\textcolor{orange}{问题优化}$

## 1. 如何防止消息丢失

- 发送方
  - 使用同步发送
  - ack 设置为 1 或者 -1，可以防止消息丢失【效率作为代价，0 > 1 > -1】，设置同步分区数 >= 2
- 消费方
  - 将自动提交改为手动提交



## 2. 如何防止消息重复消费

当生产者发生消息后，由于网络抖动等原因，没有收到 ack，但实际上 broker 已经收到了

此时生产者会进行重试，于是 broker 就会收到多条相同的消息，造成【重复消费】

- 生产者
  - 关闭重试【造成消息丢失，不建议】
- 消费者
  - 解决非幂等性消费问题
  - 在数据库中创建联合主键，防止相同的主键创建出多条记录
  - 分布式锁，以业务id为锁，保证只有一条记录能够创建成功

![截屏2023-07-31 上午11.02.40](/Users/huangminzhi/Library/Application Support/typora-user-images/截屏2023-07-31 上午11.02.40.png)



## 3.如何保证消息的顺序性

- 生产者
  - 保证消息按照顺序发送，且消息不丢失【即使用同步发送，且ack不为0】
- 消费者
  - topic 只能设置一个分区，消费组中只能有一个消费者

使用场景不多，因为牺牲掉了性能



## 4.如何解决消息积压问题

生产速度远大于消费速度，大量消息没有被消费，随着没有被消费的数据堆积越多，消费者寻址的性能会越来越差，导致整个kafka对外提供的服务性能很差，从而造成其他服务也访问速度变慢，造成服务雪崩

- 在这个消费者中，使用多线程，充分利用机器的性能进行消费信息
- 优化业务代码，提升业务层面的性能
- 创建多个消费组，多个消费者，部署到其他机器上，提高消费者的消费速度

- 创建一个消费者，该消费者在kafka中另建一个主题，配上多个分区，该消费者将 poll 下来的消息，不进行消费，直接转发到新建的主题上。此时新的主题的多分区多消费者就一起开始消费el【不常用】

![](/Users/huangminzhi/Library/Application Support/typora-user-images/截屏2023-07-31 上午11.33.56.png)



## 5.延时队列

在订单创建成功后超过30分钟，则需要取消订单

- kafka中创建相应的主题
- 消费者消费该主题的消息（轮询）
- 消费者消费消息时判断消息的创建时间和当前时间是否超过30分钟（前提订单还未支付）
  - 如果是：去数据库中修改订单状态为已取消
  - 如果否：记录当前消息的offset，并不在继续消费之后的消息，等待1min后，再次向kafka拉取该offset及之后的消息循环