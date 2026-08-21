FROM docker.elastic.co/elasticsearch/elasticsearch:8.12.0

# 安装 IK 中文分词器
RUN bin/elasticsearch-plugin install --batch \
    https://get.infini.cloud/elasticsearch/analysis-ik/8.12.0
