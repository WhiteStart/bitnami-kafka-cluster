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

