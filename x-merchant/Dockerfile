FROM openjdk:17-jdk-slim
WORKDIR /app22
COPY target/x-merchant-0.0.1-SNAPSHOT.jar app22.jar
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "app22.jar"]
#说明：在项目根目录创建 Dockerfile，用于构建 Spring Boot 应用的镜像。
 #- 基础镜像：使用 openjdk:17-jdk-slim，轻量级且支持 Java 17，与 Spring Boot 3.2.9 兼容。
 #- 工作目录：设置 /app 为工作目录。
 #- 复制 JAR：将 x-merchant-0.0.1-SNAPSHOT.jar 复制到容器并重命名为 app.jar。
 #- 暴露端口：声明容器暴露 8087 端口。
 #- 入口点：运行 java -jar app.jar 启动应用