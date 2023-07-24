# SpringCloud集成zookeeper与kafka

SpinrgCloud 集成 zookeeper、kafka，采用 SASL 加密协议，集群部署

# 一、准备工作

## 1、项目clone(dev分支)

https://github.com/WhiteStart/zookeeper-kafka 



## 2、环境准备

### 统一docker版本

- 本示例采用的环境是ubuntu16.04,该版本对应的最新版docker版本为20.10.7

```
# docker 卸载
apt-get autoremove docker docker-ce docker-engine  docker.io  containerd runc
dpkg -l |grep ^rc|awk '{print $2}' |sudo xargs dpkg -P 
apt-get autoremove docker-ce-*
rm -rf /etc/systemd/system/docker.service.d
rm -rf /var/lib/docker

# docker 安装
sudo apt-get update
sudo apt-get install ca-certificates curl gnupg
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

echo \
  "deb [arch="$(dpkg --print-architecture)" signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  "$(. /etc/os-release && echo "$VERSION_CODENAME")" stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt-get update
sudo apt-get install docker-ce docker-ce-cli containerd.io
```

- 以上命令失效则查看文档：[Install Docker Engine on Ubuntu | Docker Documentation](https://docs.docker.com/engine/install/ubuntu/)
- docker可下载的版本与ubuntu的版本也有关系，如果服务器之间版本差异较大，比如ubuntu16.04与ubuntu22.04，则需要下载deb包来统一版本
  - [Index of linux/ubuntu/dists/bionic/pool/stable/](https://download.docker.com/linux/ubuntu/dists/bionic/pool/stable)
  -  sudo dpkg -i containerd.io_1.4.11-1_amd64.deb 
  - sudo dpkg -i docker-ce-cli_20.10.7~3-0~ubuntu-bionic_amd64.deb 
  - sudo dpkg -i docker-ce_20.10.7~3-0~ubuntu-bionic_amd64.deb



## 3、安全组

docker swarm 通过同一网段下的私网地址通信

开放端口时需要开放相应【私网】源IP地址

- docker swarm 相关端口

  - 2377 TCP

  - 5789 TCP【阿里云的4789端口无法使用，因此改为5789】

  - 7946 TCP、UDP

https://docs.docker.com/engine/swarm/swarm-tutorial/

- zookeeper相关端口

  - 2181、2182、2183

  - 2888、2889、2389

  - 3888、3889、3890

- kafka相关端口
  - 9092-9010



# 二、docker部署zk-kafka分布式集群

docker-compose/分布式集群文件夹下

```bash
# 初始化swarm后，会生成一个加入命令
docker swarm init --data-path-port 5789
# 加入worker节点，在其他需要加入集群的若干机器上输入该命令
docker swarm join --token <swarm-token> <manager-ip>:<manager-port>
# 创建overlay(用于跨机器通信)网络
docker network create --driver overlay --attachable zookeeper-kafka
# 查看swarm集群状态,包含下面所需的 NODE_ID
docker node ls
# 为节点增加标签[ master, node01, node02 ] 为docker-compose.yml设置好的，用于控制zk-kafka生成的节点
docker node update --label-add role=master NODE_ID
# 对于 worker1 节点
docker node update --label-add role=node01 NODE_ID
# 对于 worker2 节点
docker node update --label-add role=node02 NODE_ID

# 部署 zookeeper-kafka 集群
docker stack deploy -c docker-compose.yml zookeeper-kafka
# 部署 UI 界面
docker stack deploy -c docker-compose-ui.yml --with-registry-auth zookeeper-kafka
# 删除容器
docker rm -f $(docker ps -aq)
```

swarm集群会自动重启服务，如果需要退出，每个节点上使用如下命令

```bash
docker swarm leave --force
```



# **三、UI 使用及 SpringCloud 测试**

可视化界面地址 => ip:80

根据如下图配置

```
{
  "security.protocol": "SASL_PLAINTEXT",
  "sasl.mechanism": "SCRAM-SHA-256",
  "sasl.jaas.config": "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"user\" password=\"password\";"
}
```

![img](https://wdcdn.qpic.cn/MTY4ODg1NTczNDQ5MjQ2Mw_744972_BjYGe9cEfccmAj1K_1689563553?w=1414&h=919&type=image/png)



## 1、微服务启动

示例项目中的provider、consumer暂时没有特定的含义

provider中定义了zk监听器，因此先运行

consumer可视作陆续部署的微服务，后运行

### 1.1 运行Provider

- 添加JVM参数

- JVM参数中的conf文件包含了zk-kafka使用SASL协议的账户密码

```
-Djava.security.auth.login.config=provider/src/main/resources/jaas.conf
```

![img](https://wdcdn.qpic.cn/MTY4ODg1NTczNDQ5MjQ2Mw_917592_2XVLIKtVoxqk7S1S_1689564003?w=1043&h=667&type=image/png)



### 1.2 运行Consumer

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



## 2、zookeeper 测试

- 启动ProviderApplication, ConsumerApplication1, ConsumerApplication2

因使用的know streaming框架Zookeeper没有配置ACL入口，先使用命令行观察zk。

```
docker exec -it zookeeper1 /bin/bash
cd opt/bitnami/zookeeper/bin ; ./zkCli.sh -server 127.0.0.1
```

provder、consumer1、consumer2成功注册

![img](https://wdcdn.qpic.cn/MTY4ODg1NTczNDQ5MjQ2Mw_736006_Fp1v3VnXFKPr2YJC_1689564595?w=1135&h=143&type=image/png)

![img](https://wdcdn.qpic.cn/MTY4ODg1NTczNDQ5MjQ2Mw_351547_7jzaFneO8Xs5ZqNK_1689564717?w=805&h=159&type=image/png)

- 停止ConsumerApplication2

![img](https://wdcdn.qpic.cn/MTY4ODg1NTczNDQ5MjQ2Mw_318963_mG4jCxLivojNF1_U_1689564897?w=305&h=44&type=image/png)

![img](https://wdcdn.qpic.cn/MTY4ODg1NTczNDQ5MjQ2Mw_835961__iWJrKvEPm3VvK19_1689564906?w=850&h=51&type=image/png)



## 3、kafka 测试

测试接口

localhost:4000/send

![img](https://wdcdn.qpic.cn/MTY4ODg1NTczNDQ5MjQ2Mw_604435_vDYn8HQ0hxVTxFaB_1689565088?w=1719&h=810&type=image/png)



参考：

1. https://hub.docker.com/r/bitnami/zookeeper
2. https://hub.docker.com/r/bitnami/kafka
3.  https://docs.spring.io/spring-cloud-zookeeper/docs/current/reference/html/#_spring_cloud_zookeeper
4.  https://zookeeper.apache.org/doc/r3.7.0/zookeeperProgrammers.html#sc_zkDataModel_znodes
5. [Client-Server mutual authentication - Apache ZooKeeper - Apache Software Foundation](https://cwiki.apache.org/confluence/display/ZOOKEEPER/Client-Server+mutual+authentication)
6.  https://doc.knowstreaming.com/product/1-brief-introduction
7.  [Getting started with swarm mode | Docker Documentation](https://docs.docker.com/engine/swarm/swarm-tutorial/)



# **四、docker部署zk-kafka【伪】集群**

docker-compose/伪集群文件夹下，运行如下命令

```bash
# 启动可视化界面
docker-compose -f docker-compose-ui-pseudo-cluster.yml up -d
# 启动zk-kafka集群
docker-compose -f docker-compose-SASL-pseudo-cluster.yml up -d
```



# 五、zookeeper 基础入门

## 1. docker部署伪集群

### 1.1  部署

```bash
docker pull zookeeper:3.7.1
```

- docker-compose.yml

创建docker-compose.yml，并在该目录下直接

docker compose up -d

即可快速在docker中创建3个zookeeper节点

```bash
version: '3.1'

networks:
  zk-net:

services:
  zoo1:
    image: zookeeper:3.7.1
    container_name: zoo1
    restart: always
    hostname: zoo1
    ports:
      - 2181:2181
    environment:
      ZOO_MY_ID: 1
      ZOO_SERVERS: server.1=zoo1:2888:3888;2181 server.2=zoo2:2888:3888;2181 server.3=zoo3:2888:3888;2181
    networks:
      - zk-net

  zoo2:
    image: zookeeper:3.7.1
    container_name: zoo2
    restart: always
    hostname: zoo2
    ports:
      - 2182:2181
    environment:
      ZOO_MY_ID: 2
      ZOO_SERVERS: server.1=zoo1:2888:3888;2181 server.2=zoo2:2888:3888;2181 server.3=zoo3:2888:3888;2181
    networks:
      - zk-net

  zoo3:
    image: zookeeper:3.7.1
    container_name: zoo3
    restart: always
    hostname: zoo3
    ports:
      - 2183:2181
    environment:
      ZOO_MY_ID: 3
      ZOO_SERVERS: server.1=zoo1:2888:3888;2181 server.2=zoo2:2888:3888;2181 server.3=zoo3:2888:3888;2181
    networks:
      - zk-net
```

### 1.2 操作

输入以下命令进入节点1操作

```bash
docker exec -it zoo1 /bin/bash
cd bin
# 进入端口为2181的节点
./zkCli.sh -server 127.0.0.1:2181
# 查看节点状态
# Mode: foller/leader
./zkServer.sh status
```



## 2. 单机部署伪集群

[Apache ZooKeeper](https://zookeeper.apache.org/releases.html) 

在**conf/zoo.cfg**中创建配置文件

```
# ZooKeeper 使用的基本时间单位（以毫秒为单位）。它用于进行心跳，最小会话超时将是tickTime 的两倍。
tickTime=2000
# 存储内存数据库快照的位置，除非另有指定，否则存储数据库更新的事务日志。
dataDir=/var/lib/zookeeper
# 监听客户端连接的端口
clientPort=2181
```

![img](https://wdcdn.qpic.cn/MTY4ODg1NTczNDQ5MjQ2Mw_552962_Kg_QO35BmDRhyXlw_1688547477?w=739&h=347&type=image/png)

- 对每个节点修改zoo.cfg

zookeeper-1/conf/zoo.cfg

```
# 基本时间单位，以毫秒为单位。它用于控制心跳和超时时间
tickTime=2000
# 客户端连接到ZooKeeper服务器所需的最长时间
initLimit=10
# 服务器之间可以存在的最大时间差
# 如果5个tickTime（10秒）内无法完成数据同步，就会失败
syncLimit=5
# 指定了ZooKeeper服务器保存数据快照和事务日志的目录路径
dataDir=/Users/huangminzhi/Desktop/zookeeper-cluster/zookeeper-1/data
clientPort=2181
# 2881 领导选举端口，用于在集群中选举leader
# 3881 数据通信端口，用于节点之间的数据通信
server.1=127.0.0.1:2881:3881
server.2=127.0.0.1:2882:3882
server.3=127.0.0.1:2883:3883
```

 

zookeeper-2/conf/zoo.cfg

```
tickTime=2000
initLimit=10
syncLimit=5
dataDir=/Users/huangminzhi/Desktop/zookeeper-cluster/zookeeper-2/data
clientPort=2182
server.1=127.0.0.1:2881:3881
server.2=127.0.0.1:2882:3882
server.3=127.0.0.1:2883:3883
```

 

zookeeper-3/conf/zoo.cfg

```
tickTime=2000
initLimit=10
syncLimit=5
dataDir=/Users/huangminzhi/Desktop/zookeeper-cluster/zookeeper-2/data
clientPort=2183
server.1=127.0.0.1:2881:3881
server.2=127.0.0.1:2882:3882
server.3=127.0.0.1:2883:3883
```



在每个对应的bin目录下分别启动

```bash
# 启动客户端
./zkServer.sh start
# 启动并连接服务端
./zkCli.sh -server 127.0.0.1:218
```

```bash
./zkServer.sh start
./zkCli.sh -server 127.0.0.1:2182
```

```bash
./zkServer.sh start
./zkCli.sh -server 127.0.0.1:2183
```



查看节点状态

```bash
./zkServer.sh status
```

![img](https://wdcdn.qpic.cn/MTY4ODg1NTczNDQ5MjQ2Mw_325122_4R23exZBYnN6zS-x_1688547708?w=563&h=138&type=image/png)

![img](https://wdcdn.qpic.cn/MTY4ODg1NTczNDQ5MjQ2Mw_354725_8BACVuqxkL3aGwhH_1688547773?w=558&h=102&type=image/png)

以上为zookeeper命令行的基本操作示例，在Spring中，使用curator封装api





## 3. 概念与基本命令

- ZooKeeper是一个树形目录服务，每个节点称为ZNode，保存自己的数据和节点信息

![img](https://wdcdn.qpic.cn/MTY4ODg1NTczNDQ5MjQ2Mw_445520_kWKKZm5o3gvmVN2Z_1688542722?w=250&h=139&type=image/png)

节点分为四类：

- 持久化节点
- 临时节点
  - -e
- 持久化顺序节点
  - -s
- 临时顺序节点
  - -es



### **服务端**

#### 1. 启动

```bash
./zkServer.sh start
```

#### 2. 停止

```bash
./zkServer.sh stop
```

### **客户端**

#### 1. 启动

```bash
./zkCli.sh -server 127.0.0.1:2181
```

#### 2. 查看当前节点的子节点

```bash
ls /
```

![img](https://wdcdn.qpic.cn/MTY4ODg1NTczNDQ5MjQ2Mw_813694_wEs_0Si9aNs5dRMy_1688541931?w=391&h=121&type=image/png)

#### 3.     **创建节点**

```bash
create /test abc
```

![img](https://wdcdn.qpic.cn/MTY4ODg1NTczNDQ5MjQ2Mw_962828_zwzVaE2wNB0GAHMc_1688542264?w=286&h=35&type=image/png)

test下创建新的节点

```bash
create /test/{newNode} {value}
```



#### 4. **操作节点**

- 获取节点
  - get /test
- 修改节点
  - set /test jinhetech
- 删除节点
  - delete /test

![img](https://wdcdn.qpic.cn/MTY4ODg1NTczNDQ5MjQ2Mw_750604_Id_qdXL-Nxl-VeIq_1688542370?w=394&h=186&type=image/png)



### **权限配置**

Zookeeper 的权限由[scheme : id :permissions]三部分组成，其中 Schemes 和 Permissions 内置的可选项分别如下：



**Permissions 可选项**：

- **CREATE(C)**：允许创建子节点；
-  **READ(R)**：允许从节点获取数据并列出其子节点；
- **WRITE(W)**：允许为节点设置数据；
- **DELETE(D)**：允许删除子节点；
- **ADMIN(A)**：允许为节点设置权限。



**Schemes 可选项**：

- **world**：
- 默认模式，所有客户端都拥有指定的权限，只有一个 id 选项，就是 anyone
  - 组合写法为 world:anyone:[permissons]
- **auth**：
  - 只有经过认证的用户才拥有指定的权限。
- 通常组合写法为 auth:user:password:[permissons]
  - 需要先进行登录，之后采用 auth 模式设置权限时，user 和 password 都将使用登录的用户名和密码；
- **digest**：
  - 只有经过认证的用户才拥有指定的权限。
  - 通常组合写法为 auth:user:BASE64(SHA1(password)):[permissons]
- 密码必须通过 SHA1 和 BASE64 进行双重加密；
- **ip**：
  - 限制只有特定 IP 的客户端才拥有指定的权限。
  - 通常组成写法为 ip:182.168.0.168:[permissions]；
- **super**：
  - 代表超级管理员，拥有所有的权限，需要修改 Zookeeper 启动脚本进行配置。



#### 使用auth模式设置权限

auth在网络中明文传输，方便本地测试

digest加密传输，不方便测试但安全

```bash
[zk: 127.0.0.1:2181(CONNECTED) 9] addauth digest hmz:hmz
[zk: 127.0.0.1:2181(CONNECTED) 10] create /test
Created /test
[zk: 127.0.0.1:2181(CONNECTED) 11] ls /test
[]
[zk: 127.0.0.1:2181(CONNECTED) 12] setAcl /test auth:hmz:hmz:cdrwa
[zk: 127.0.0.1:2181(CONNECTED) 13] getAcl /test
'digest,'hmz:PO61JaA/O42zoY4FMVwuAR7HSAE=
: cdrwa
```



#### 使用ip模式配置权限

```bash
create /ip_test 
setAcl ip:127.0.0.1:r
```



# 六、kafka 基础入门

Kafka 安装包官方下载地址：[Apache Kafka](http://kafka.apache.org/downloads) ，这里下载的是当前最新版3.5.0

## 1. 配置文件

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



## 2.  启动集群

由于关闭命令行，kafka shut down，这里采用了后台启动方式

```bash
nohup bin/kafka-server-start.sh config/server-1.properties &
nohup bin/kafka-server-start.sh config/server-2.properties &
nohup bin/kafka-server-start.sh config/server-3.properties &
```



## 3.  测试

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
