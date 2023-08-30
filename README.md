# SpringCloud集成zookeeper与kafka

Spring Cloud 集成 zookeeper、kafka，采用 SASL 加密协议，分布式集群部署

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



在三台机器上暴露端口供grafana监控

- --kafka.server=kafka1:9092
- --kafka.server=kafka2:9092
- --kafka.server=kafka3:9092

```bash
docker run -d --name kafka-exporter1 --network zookeeper-kafka -p 9308:9308 docker.io/bitnami/kafka-exporter --kafka.server=kafka1:9092 --kafka.version=3.4.1 --sasl.enabled --sasl.username=user --sasl.password=GiAszivMBB --sasl.mechanism=scram-sha256
```



debug命令参考

- 集群中服务未正常启动时debug

```bash
# 查看swarm集群中的容器信息
docker service ls
# 查看某个特定容器的信息，
docker service ps {id}
```

-  集群重启

```bash
# 查看所有 stack 【stack表示由多个服务组成的一个应用程序】
docker stack ls
# 删除 zookeeper-kafka 这一 overlay 中的所有容器
docker stack rm zookeeper-kafka
# 删除完后再使用docker stack deploy -c 部署，不用再加入 swarm 
```

-  离开集群网络

```bash
# 离开swarm集群
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



参考：

1. https://hub.docker.com/r/bitnami/zookeeper
2. https://hub.docker.com/r/bitnami/kafka
3.  https://docs.spring.io/spring-cloud-zookeeper/docs/current/reference/html/#_spring_cloud_zookeeper
4.  https://zookeeper.apache.org/doc/r3.7.0/zookeeperProgrammers.html#sc_zkDataModel_znodes
5. [Client-Server mutual authentication - Apache ZooKeeper - Apache Software Foundation](https://cwiki.apache.org/confluence/display/ZOOKEEPER/Client-Server+mutual+authentication)
6.  https://doc.knowstreaming.com/product/1-brief-introduction
7.  [Getting started with swarm mode | Docker Documentation](https://docs.docker.com/engine/swarm/swarm-tutorial/)

