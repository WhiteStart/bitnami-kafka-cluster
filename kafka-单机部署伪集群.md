Kafka 安装包官方下载地址：[Apache Kafka](http://kafka.apache.org/downloads) ，这里下载的是当前最新版3.5.0

## 1. 单机部署伪集群

进入config 目录下 ，拷贝三份配置文件：

```bash
cp server.properties server-1.properties
cp server.properties server-2.properties
cp server.properties server-3.properties
```

分别修改三份配置文件中的部分配置，如下：

server-1.properties：

```properties
# 节点唯一标识
broker.id=0
# 监听地址
listeners=PLAINTEXT://127.0.0.1:9092 
# 数据日志存储位置
# 程序日志，通过同一目录下的 log4j.properties 进行配置
log.dirs=/usr/local/kafka-logs/00
# zookeeper连接地址
zookeeper.connect=127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183
```

server-2.properties：

```bash
broker.id=1
listeners=PLAINTEXT://127.0.0.1:9093
log.dirs=/usr/local/kafka-logs/01
zookeeper.connect=127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183
```

server-3.properties：

```bash
broker.id=2
listeners=PLAINTEXT://127.0.0.1:9094
log.dirs=/usr/local/kafka-logs/02
zookeeper.connect=127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183
```

由于关闭命令行，kafka shut down，这里采用了后台启动方式

```bash
nohup bin/kafka-server-start.sh config/server-1.properties &
nohup bin/kafka-server-start.sh config/server-2.properties &
nohup bin/kafka-server-start.sh config/server-3.properties &
```



- 创建测试主题

```bash
bin/kafka-topics.sh --create --bootstrap-server 127.0.0.1:9092 \
--replication-factor 3 \
--partitions 1 --topic my-replicated-topic
```

-  查看主题信息

```bash
bin/kafka-topics.sh --describe --bootstrap-server hadoop001:9092 --topic my-replicated-topic
```

![img](https://wdcdn.qpic.cn/MTY4ODg1NTczNDQ5MjQ2Mw_976409_7JejvBjr58-_yqxR_1688966995?w=567&h=66&type=image/png)