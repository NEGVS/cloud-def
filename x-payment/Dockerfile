#拉取 OpenJDK 作为基础镜像
FROM openjdk:17-jdk
#FROM openjdk:11-jre-slim

#作者
MAINTAINER abcd

#设置工作目录
WORKDIR /abcd


#将 Jar 包拷贝到镜像中--ADD: 将打包的可执行jar包复制到镜像中并重命名（用 COPY 也可），ADD demo-0.0.1-SNAPSHOT.jar demo.jar将demo-0.0.1-SNAPSHOT.jar 复制到镜像中并重命名为 demo.jar
COPY ./x-payment-0.0.1-SNAPSHOT.jar aliPay.jar

#暴露应用端口
EXPOSE 8082
#启动 Spring Boot 应用
ENTRYPOINT ["java", "-jar", "aliPay.jar"]




#
#FROM openjdk:17 #jdk版本
#
#RUN mkdir /app
#
#COPY canteen-0.0.1-SNAPSHOT.jar  /app/
#
#CMD java -jar  /app/canteen-0.0.1-SNAPSHOT.jar
#
#EXPOSE 8088 # 暴露端口
