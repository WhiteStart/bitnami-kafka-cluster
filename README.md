# **zookeeper**

## 1. 概念

 ● ZooKeeper是一个树形目录服务，每个节点称为ZNode，保存自己的数据和节点信息

![img](https://wdcdn.qpic.cn/MTY4ODg1NTczNDQ5MjQ2Mw_445520_kWKKZm5o3gvmVN2Z_1688542722?w=250&h=139&type=image/png)

节点分为四类：

- 持久化节点

- 临时节点
  -  -e

- 持久化顺序节点
  - -s

- 临时顺序节点
  - -es



## 2. 下载

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



## **3.基本命令**

### **服务端**

#### 1. 启动

```
./zkServer.sh start
```

#### 2. 停止

```
./zkServer.sh stop
```

### **客户端**

#### 1. 启动

```
./zkCli.sh -server 127.0.0.1:2181
```

#### 2. 查看当前节点的子节点

```
ls /
```

![img](https://wdcdn.qpic.cn/MTY4ODg1NTczNDQ5MjQ2Mw_813694_wEs_0Si9aNs5dRMy_1688541931?w=391&h=121&type=image/png)

####  3. 创建节点

```
create /test jinhe
```

![img](https://wdcdn.qpic.cn/MTY4ODg1NTczNDQ5MjQ2Mw_962828_zwzVaE2wNB0GAHMc_1688542264?w=286&h=35&type=image/png)



#### 4. 操作节点

-  获取节点

  ```
  get /test
  ```

-  修改节点

  ```
  set /test jinhetech
  ```

- 删除节点

  ```
  delete /test
  ```

![img](https://wdcdn.qpic.cn/MTY4ODg1NTczNDQ5MjQ2Mw_750604_Id_qdXL-Nxl-VeIq_1688542370?w=394&h=186&type=image/png)





## 4. 集群部署

- 部署3个节点

![img](https://wdcdn.qpic.cn/MTY4ODg1NTczNDQ5MjQ2Mw_552962_Kg_QO35BmDRhyXlw_1688547477?w=739&h=347&type=image/png)

- 修改zoo.cfg

修改对应的dataDir和clientPort

dataDir=.../zookeeper-2/data

clientPort=2182



dataDir=.../zookeeper-3/data

clientPort=2183

```
tickTime=2000
initLimit=10
syncLimit=5
dataDir=/Users/huangminzhi/Desktop/zookeeper-cluster/zookeeper-1/data
clientPort=2181
server.1=127.0.0.1:2881:3881
server.2=127.0.0.1:2882:3882
server.3=127.0.0.1:2883:3883
```



查看节点状态

```
./zkServer.sh status
```

![img](https://wdcdn.qpic.cn/MTY4ODg1NTczNDQ5MjQ2Mw_325122_4R23exZBYnN6zS-x_1688547708?w=563&h=138&type=image/png)

![img](https://wdcdn.qpic.cn/MTY4ODg1NTczNDQ5MjQ2Mw_354725_8BACVuqxkL3aGwhH_1688547773?w=558&h=102&type=image/png)