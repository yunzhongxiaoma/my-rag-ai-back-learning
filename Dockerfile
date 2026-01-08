# 多阶段构建的Dockerfile
# 阶段1: 构建阶段
FROM maven:3.9.9-openjdk-17-slim AS builder

# 设置工作目录
WORKDIR /app

# 复制pom.xml和源代码
COPY pom.xml .
COPY src ./src

# 构建应用（跳过测试以加快构建速度）
RUN mvn clean package -DskipTests

# 阶段2: 运行阶段
FROM openjdk:17-jdk-slim

# 设置维护者信息
LABEL maintainer="RAG AI Learning Project"
LABEL description="RAG AI Backend with Spring Boot and Milvus"
LABEL version="1.0.0"

# 创建应用用户（安全最佳实践）
RUN groupadd -r appuser && useradd -r -g appuser appuser

# 设置工作目录
WORKDIR /app

# 创建日志目录
RUN mkdir -p /app/logs && chown -R appuser:appuser /app

# 从构建阶段复制jar文件
COPY --from=builder /app/target/my-rag-ai-back-learning-*.jar app.jar

# 复制配置文件
COPY --from=builder /app/src/main/resources/application.yml ./config/
COPY --from=builder /app/src/main/resources/sql/ ./sql/

# 设置文件权限
RUN chown -R appuser:appuser /app

# 切换到非root用户
USER appuser

# 暴露端口
EXPOSE 8989

# 设置JVM参数
ENV JAVA_OPTS="-server -Xms512m -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -Xloggc:/app/logs/gc.log"

# 设置Spring Boot配置
ENV SPRING_PROFILES_ACTIVE=docker

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8989/actuator/health || exit 1

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]