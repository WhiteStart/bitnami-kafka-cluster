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

