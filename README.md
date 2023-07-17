## 1、快速部署zk-kafka伪集群

clone示例项目dev分支【目前代码属于可运行阶段，部分还需要优化】

https://github.com/WhiteStart/zookeeper-kafka

运行如下命令

```
# 启动可视化界面
docker-compose -f docker-compose-ui.yml up -d
# 启动zk-kafka集群
docker-compose -f docker-compose-pseudo-cluster-SASL.yml up -d
```

![img](https://wdcdn.qpic.cn/MTY4ODg1NTczNDQ5MjQ2Mw_898989_QgZE7SlzBNBTJ6nZ_1689563873?w=1091&h=623&type=image/png)



可视化界面地址:127.0.0.1:80

根据如下图配置

```
{
  "security.protocol": "SASL_PLAINTEXT",
  "sasl.mechanism": "SCRAM-SHA-256",
  "sasl.jaas.config": "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"user\" password=\"password\";"
}
```

![img](https://wdcdn.qpic.cn/MTY4ODg1NTczNDQ5MjQ2Mw_744972_BjYGe9cEfccmAj1K_1689563553?w=1414&h=919&type=image/png)![img](https://wdcdn.qpic.cn/MTY4ODg1NTczNDQ5MjQ2Mw_717504_-j70QUMhJq8_t7M0_1689563672?w=1410&h=823&type=image/png)



## 2、微服务启动

示例项目中的provider、consumer暂时没有特定的含义

provider中定义了zk监听器，因此先运行

consumer可视作陆续部署的微服务，后运行

### 2.1 运行Provider

- 添加VM参数
-  JVM参数中的conf文件包含了zk-kafka使用SASL协议的账户密码

```
-Djava.security.auth.login.config=provider/src/main/resources/jaas.conf
```

![img](https://wdcdn.qpic.cn/MTY4ODg1NTczNDQ5MjQ2Mw_917592_2XVLIKtVoxqk7S1S_1689564003?w=1043&h=667&type=image/png)



### 2.2 运行Consumer

- 添加JVM参数

- 若干个微服务，需要设置不同的端口与名称

```
-Dserver.port=4001
-Dspring.application.name=consumer1
-Djava.security.auth.login.config=consumer/src/main/resources/jaas.conf
```

```
-Dserver.port=4002
-Dspring.application.name=consumer2
-Djava.security.auth.login.config=consumer/src/main/resources/jaas.conf
```

![img](https://wdcdn.qpic.cn/MTY4ODg1NTczNDQ5MjQ2Mw_739685_BCjELNw2TzCt0CaI_1689564269?w=1007&h=312&type=image/png)



## 3、zookeeper 测试

- 启动ProviderApplication, ConsumerApplication1, ConsumerApplication2

因使用的know streaming框架Zookeeper似乎存在bug，先使用命令行观察zk。

```
docker exec -it zookeeper1 /bin/bash
cd opt/bitnami/zookeeper/bin ; ./zkCli.sh -server 127.0.0.1
```

provder、consumer1、consumer2成功注册

![img](https://wdcdn.qpic.cn/MTY4ODg1NTczNDQ5MjQ2Mw_736006_Fp1v3VnXFKPr2YJC_1689564595?w=1135&h=143&type=image/png)

![img](https://wdcdn.qpic.cn/MTY4ODg1NTczNDQ5MjQ2Mw_351547_7jzaFneO8Xs5ZqNK_1689564717?w=805&h=159&type=image/png)

-  停止ConsumerApplication2

![img](https://wdcdn.qpic.cn/MTY4ODg1NTczNDQ5MjQ2Mw_318963_mG4jCxLivojNF1_U_1689564897?w=305&h=44&type=image/png)

![img](https://wdcdn.qpic.cn/MTY4ODg1NTczNDQ5MjQ2Mw_835961__iWJrKvEPm3VvK19_1689564906?w=850&h=51&type=image/png)



## 4、kafka 测试

测试接口

localhost:4000/send

![img](https://wdcdn.qpic.cn/MTY4ODg1NTczNDQ5MjQ2Mw_604435_vDYn8HQ0hxVTxFaB_1689565088?w=1719&h=810&type=image/png)

参考：

1. https://hub.docker.com/r/bitnami/zookeeper
2.  https://hub.docker.com/r/bitnami/kafka
3.  https://docs.spring.io/spring-cloud-zookeeper/docs/current/reference/html/#_spring_cloud_zookeeper
4.  https://zookeeper.apache.org/doc/r3.7.0/zookeeperProgrammers.html#sc_zkDataModel_znodes
5.  [Client-Server mutual authentication - Apache ZooKeeper - Apache Software Foundation](https://cwiki.apache.org/confluence/display/ZOOKEEPER/Client-Server+mutual+authentication)
6. https://doc.knowstreaming.com/product/1-brief-introduction

