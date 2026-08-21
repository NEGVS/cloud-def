docker compose build
↓
构建 ES 镜像
↓
安装 analysis-ik
↓
andy/elasticsearch:8.12.0-ik
↓
docker compose up -d

以后无论容器删除多少次：

docker compose down
docker compose up -d

IK 都不会丢。


docker compose build --no-cache elasticsearch

docker compose up -d elasticsearch



# andy_mac@Andy-FandeMacBook-Pro-547 x-productB % docker compose build --no-cache elasticsearch

## 问题很明确

现在不是 Elasticsearch 的问题，而是 **Docker Compose 找不到 Dockerfile**。

报错：

```text
failed to read dockerfile:
open .../Dockerfile: no such file or directory
```

说明你的 `docker-compose.yml` 里面应该写了：

```yaml
build:
  context: .
  dockerfile: Dockerfile
```

那么 Docker 会在：

```text
x-productB/
└── Dockerfile
```

寻找文件。

但目前这个文件不存在。

---

## 你的目录应该这样放

你现在终端所在：

```text
x-productB
```

所以执行：

```bash
pwd
```

应该类似：

```text
.../x-productB
```

然后：

```bash
ls -la
```

应该至少看到：

```text
Dockerfile
docker-compose.yml
```

推荐最终目录：

```text
x-productB/
├── Dockerfile
├── docker-compose.yml
├── .dockerignore
├── pom.xml
├── src/
│   ├── main/
│   └── test/
└── ...
```

---

## 直接创建 Dockerfile

你现在就在 `x-productB` 目录，所以直接执行：

```bash
cat > Dockerfile <<'EOF'
FROM docker.elastic.co/elasticsearch/elasticsearch:8.12.0

RUN bin/elasticsearch-plugin install --batch \
    https://get.infini.cloud/elasticsearch/analysis-ik/8.12.0
EOF
```

然后检查：

```bash
cat Dockerfile
```

应该显示：

```dockerfile
FROM docker.elastic.co/elasticsearch/elasticsearch:8.12.0

RUN bin/elasticsearch-plugin install --batch \
    https://get.infini.cloud/elasticsearch/analysis-ik/8.12.0
```

再检查：

```bash
ls -lh Dockerfile
```

---

## 你的 docker-compose.yml

确保 Elasticsearch 部分是：

```yaml
services:

  elasticsearch:
    build:
      context: .
      dockerfile: Dockerfile

    image: x-productb-elasticsearch:8.12.0-ik

    container_name: elasticsearch-recruitment

    restart: unless-stopped

    environment:
      - discovery.type=single-node
      - ES_JAVA_OPTS=-Xms1g -Xmx1g
      - xpack.security.enabled=false
      - xpack.security.http.ssl.enabled=false

    ports:
      - "9200:9200"
      - "9300:9300"

    volumes:
      - es_data:/usr/share/elasticsearch/data

    networks:
      - recruitment-net

    healthcheck:
      test:
        [
          "CMD-SHELL",
          "curl -fs http://localhost:9200/_cluster/health || exit 1"
        ]
      interval: 10s
      timeout: 5s
      retries: 20
      start_period: 30s
```

最后的：

```yaml
networks:
```

不能单独结束。

完整 compose 的底部应该是：

```yaml
volumes:
  es_data:
    driver: local

networks:
  recruitment-net:
    driver: bridge
```

---

## 先检查 Compose 配置

在重新 build 之前，我建议你先执行：

```bash
docker compose config
```

如果 YAML 没问题，它会把最终解析后的配置打印出来。

重点检查：

```yaml
build:
  context: .
  dockerfile: Dockerfile
```

---

## 然后重新构建

```bash
docker compose build --no-cache elasticsearch
```

这次应该从：

```text
[elasticsearch 1/2] FROM docker.elastic.co/elasticsearch/elasticsearch:8.12.0
```

开始。

然后：

```text
[elasticsearch 2/2] RUN bin/elasticsearch-plugin install --batch ...
```

如果网络正常，应该能够从：

```text
https://get.infini.cloud/elasticsearch/analysis-ik/8.12.0
```

下载约 4.6 MB 的 IK 插件。

---

## 构建成功以后不要急着启动 Spring Boot

先：

```bash
docker compose up -d elasticsearch
```

然后：

```bash
docker ps
```

再检查 IK：

```bash
docker exec elasticsearch-recruitment \
  bin/elasticsearch-plugin list
```

必须看到：

```text
analysis-ik
```

然后再验证：

```bash
curl -X POST \
  "http://localhost:9200/_analyze" \
  -H "Content-Type: application/json" \
  -d '{
    "analyzer": "ik_smart",
    "text": "Java高级开发工程师"
  }'
```

如果这里成功，你之前 Spring Boot 的：

```text
analyzer [ik_smart] has not been configured
```

就解决了。

## 关于你刚才的 `networks:`

如果你是想问为什么我给你的 compose 最后是：

```yaml
networks:
  recruitment-net:
    driver: bridge
```

这是因为：

```text
Spring Boot
     |
     | localhost:9200
     v
Elasticsearch
```

如果 **Spring Boot 在你的 Mac 上直接运行**，那么它访问 Docker 映射出来的：

```text
localhost:9200
```

是正确的。

而：

```text
recruitment-net
```

主要用于：

```text
Kibana
   |
   | http://elasticsearch:9200
   v
Elasticsearch
```

也就是说，**你目前的 Spring Boot 不需要加入这个 Docker network**，因为 Spring Boot 是在 Mac 宿主机运行的。

你当前这个错误最简单的解决步骤就是：

```bash
cat > Dockerfile <<'EOF'
FROM docker.elastic.co/elasticsearch/elasticsearch:8.12.0

RUN bin/elasticsearch-plugin install --batch \
    https://get.infini.cloud/elasticsearch/analysis-ik/8.12.0
EOF

docker compose build --no-cache elasticsearch
```

先执行这两步。
