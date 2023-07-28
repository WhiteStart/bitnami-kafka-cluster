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

