docker-compose/伪集群文件夹下，运行如下命令

```bash
# 启动可视化界面
docker-compose -f docker-compose-ui-pseudo-cluster.yml up -d
# 启动zk-kafka集群
docker-compose -f docker-compose-SASL-pseudo-cluster.yml up -d
```

