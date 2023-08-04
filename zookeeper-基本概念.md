
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

